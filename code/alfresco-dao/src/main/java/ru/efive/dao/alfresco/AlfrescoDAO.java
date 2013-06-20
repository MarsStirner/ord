package ru.efive.dao.alfresco;

import java.io.InputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.webservice.authoring.AuthoringServiceSoapBindingStub;
import org.alfresco.webservice.authoring.CheckoutResult;
import org.alfresco.webservice.content.Content;
import org.alfresco.webservice.content.ContentServiceSoapBindingStub;
import org.alfresco.webservice.repository.QueryResult;
import org.alfresco.webservice.repository.RepositoryServiceSoapBindingStub;
import org.alfresco.webservice.repository.UpdateResult;
import org.alfresco.webservice.types.CML;
import org.alfresco.webservice.types.CMLAddAspect;
import org.alfresco.webservice.types.CMLCreate;
import org.alfresco.webservice.types.CMLDelete;
import org.alfresco.webservice.types.CMLUpdate;
import org.alfresco.webservice.types.ContentFormat;
import org.alfresco.webservice.types.NamedValue;
import org.alfresco.webservice.types.Node;
import org.alfresco.webservice.types.ParentReference;
import org.alfresco.webservice.types.Predicate;
import org.alfresco.webservice.types.Query;
import org.alfresco.webservice.types.Reference;
import org.alfresco.webservice.types.ResultSet;
import org.alfresco.webservice.types.ResultSetRow;
import org.alfresco.webservice.types.Store;
import org.alfresco.webservice.types.Version;
import org.alfresco.webservice.types.VersionHistory;
import org.alfresco.webservice.util.AuthenticationUtils;
import org.alfresco.webservice.util.Constants;
import org.alfresco.webservice.util.ContentUtils;
import org.alfresco.webservice.util.Utils;
import org.alfresco.webservice.util.WebServiceFactory;
import ru.efive.dao.DAO;
import ru.efive.dao.InitializationException;

public class AlfrescoDAO<T extends AlfrescoNode> implements DAO<T> {
    Class<T> class_;

    private String serverUrl;
    private String login;
    private String password;
    private String path;

    protected final Store STORE = new Store(Constants.WORKSPACE_STORE, "SpacesStore");
    protected final Store STORE_LIGHTWEIGHT = new Store("versionStore", "version2Store");

    public AlfrescoDAO() {
        super();
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void initClass(Class<T> class_) throws InitializationException {
        try {
            this.class_ = class_;
        } catch (Exception e) {
            e.printStackTrace();
            throw new InitializationException();
        }
    }

    @Override
    public boolean connect() {
        boolean result = false;
        try {
            WebServiceFactory.setEndpointAddress(serverUrl);
            AuthenticationUtils.startSession(login, password);
            result = true;
        } catch (Exception e) {
            result = false;
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public boolean disconnect() {
        boolean result = false;
        try {
            System.out.println("Disconnecting session");
            AuthenticationUtils.endSession();
            result = true;
        } catch (Exception e) {
            result = false;
            e.printStackTrace();
        }
        return result;
    }

    private ContentFormat getContentFormat() {
        return new ContentFormat("application/octet-stream", "utf-8");
    }

    protected ParentReference getCompanyHome() {
        ParentReference companyHomeParent = new ParentReference(STORE, null, path, Constants.ASSOC_CONTAINS, null);
        return companyHomeParent;
    }

    protected RepositoryServiceSoapBindingStub getRepositoryService() {
        return WebServiceFactory.getRepositoryService();
    }

    protected ContentServiceSoapBindingStub getContentService() {
        return WebServiceFactory.getContentService();
    }

    protected AuthoringServiceSoapBindingStub getAuthoringService() {
        return WebServiceFactory.getAuthoringService();
    }

    protected ParentReference ReferenceToParent(Reference spaceref) {
        ParentReference parent = new ParentReference();

        parent.setStore(STORE);
        parent.setPath(spaceref.getPath());
        parent.setUuid(spaceref.getUuid());
        parent.setAssociationType(Constants.ASSOC_CONTAINS);

        return parent;
    }

    protected String normilizeNodeName(String name) {
        return name.replace(" ", "_");
    }

    protected Reference createSpace(String spacename) throws Exception {
        Reference space = null;
        try {
            System.out.println("Entering space " + spacename);
            space = new Reference(STORE, null, getCompanyHome().getPath() + "/cm:" + normilizeNodeName(spacename));
            getRepositoryService().get(new Predicate(new Reference[]{space}, STORE, null));
        } catch (Exception e1) {
            System.out.println("The space named " + spacename + " does not exist. Creating it.");

            ParentReference companyHome = getCompanyHome();
            companyHome.setChildName(Constants.createQNameString(Constants.NAMESPACE_CONTENT_MODEL, normilizeNodeName(spacename)));

            NamedValue[] properties = new NamedValue[]{Utils.createNamedValue(Constants.PROP_NAME, spacename)};

            CMLCreate create = new CMLCreate(null, companyHome, null, null, null, Constants.TYPE_FOLDER, properties);
            CML cml = new CML();
            cml.setCreate(new CMLCreate[]{create});
            try {
                getRepositoryService().update(cml);
            } catch (Exception e2) {
                System.err.println("Can't create the space.");
                throw e2;
            }
        }
        return space;
    }

    protected Reference createSpace(Reference parentref, String spacename) throws Exception {
        Reference space = null;
        ParentReference parent = ReferenceToParent(parentref);
        try {
            System.out.println("Entering space " + spacename);
            space = new Reference(STORE, null, parent.getPath() + "/cm:" + normilizeNodeName(spacename));
            WebServiceFactory.getRepositoryService().get(new Predicate(new Reference[]{space}, STORE, null));
        } catch (Exception e1) {
            System.out.println("The space named " + spacename + " does not exist. Creating it.");

            parent.setChildName(Constants.createQNameString(Constants.NAMESPACE_CONTENT_MODEL, normilizeNodeName(spacename)));

            NamedValue[] properties = new NamedValue[]{Utils.createNamedValue(Constants.PROP_NAME, spacename)};

            CMLCreate create = new CMLCreate(null, parent, null, null, null, Constants.TYPE_FOLDER, properties);
            CML cml = new CML();
            cml.setCreate(new CMLCreate[]{create});
            try {
                getRepositoryService().update(cml);
            } catch (Exception e2) {
                System.err.println("Can't create the space.");
                throw e2;
            }
        }
        return space;
    }

    protected void makeVersionable(Reference reference) throws Exception {
        Predicate predicate = new Predicate(new Reference[]{reference}, null, null);
        CMLAddAspect addAspect = new CMLAddAspect(Constants.ASPECT_VERSIONABLE, null, predicate, null);
        CML cml = new CML();
        cml.setAddAspect(new CMLAddAspect[]{addAspect});
        getRepositoryService().update(cml);
    }

    protected boolean isVersionable(Reference reference) throws Exception {
        Predicate predicate = new Predicate(new Reference[]{reference}, null, null);
        Node[] nodes = getRepositoryService().get(predicate);
        if (nodes.length > 0) {
            System.out.println("Found node");
            for (String aspect : nodes[0].getAspects()) {
                if (aspect.equals(Constants.ASPECT_VERSIONABLE)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected List<Revision> getVersionHistory(Reference reference) {
        List<Revision> revisions = new ArrayList<Revision>();
        try {
            VersionHistory versionHistory = getAuthoringService().getVersionHistory(reference);
            for (Version version : versionHistory.getVersions()) {
                Revision revision = new Revision();
                revision.setVersion(version.getLabel());
                for (NamedValue namedValue : version.getCommentaries()) {
                    if (namedValue.getName().equals("author")) {
                        revision.setAuthorId(Integer.parseInt(namedValue.getValue()));
                    } else if (namedValue.getName().equals("revisionFileName")) {
                        revision.setFileName(URLDecoder.decode(namedValue.getValue(), "UTF-8"));
                    }
                }
                revisions.add(revision);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return revisions;
    }

    /* </HELPER_METHODS> */


    /* <MAIN_METHODS> */

    @Override
    public List<T> getDataList() {
        List<T> result = new ArrayList<T>();
        try {
            Query query = new Query(Constants.QUERY_LANG_LUCENE, "TYPE:\"" + class_.newInstance().getNamedNodeType() + "\"");
            System.out.println("Prepare query: " + query.getStatement());
            QueryResult queryResult = getRepositoryService().query(STORE, query, false);
            ResultSet resultSet = queryResult.getResultSet();
            ResultSetRow[] rows = resultSet.getRows();
            if (rows != null) {
                System.out.println("Result set is not null, length - " + rows.length);
                for (ResultSetRow row : rows) {
                    T data = class_.newInstance();
                    data.setId(row.getNode().getId());
                    data.setNodeProperties(row.getColumns());
                    Reference nodeReference = getNodeReference(data);
                    if (isVersionable(nodeReference)) {
                        data.setVersionable(true);
                        data.setRevisions(getVersionHistory(nodeReference));
                    }
                    result.add(data);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public List<T> getDataList(String queryString) {
        List<T> result = new ArrayList<T>();
        try {
            Query query = new Query(Constants.QUERY_LANG_LUCENE, queryString);
            System.out.println("Prepare query: " + query.getStatement());
            QueryResult queryResult = getRepositoryService().query(STORE, query, false);
            ResultSet resultSet = queryResult.getResultSet();
            ResultSetRow[] rows = resultSet.getRows();
            if (rows != null) {
                System.out.println("Result set is not null, length - " + rows.length);
                for (ResultSetRow row : rows) {
                    T data = class_.newInstance();
                    data.setId(row.getNode().getId());
                    data.setNodeProperties(row.getColumns());
                    Reference nodeReference = getNodeReference(data);
                    if (isVersionable(nodeReference)) {
                        data.setVersionable(true);
                        data.setRevisions(getVersionHistory(nodeReference));
                    }
                    result.add(data);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public T getDataById(String id) {
        T data = null;
        try {
            Reference nodeReference = new Reference(STORE, id, path);
            Predicate predicate = new Predicate(new Reference[]{nodeReference}, null, null);
            Node[] nodes = getRepositoryService().get(predicate);
            if (nodes.length > 0) {
                System.out.println("Found nodes");
                Node node = nodes[0];
                data = class_.newInstance();
                data.setId(node.getReference().getUuid());
                if (data.getPath() == null || data.getPath().size() == 0) {
                    List<String> path = new ArrayList<String>();
                    path.add(node.getReference().getPath());
                    data.setPath(path);
                }
                for (String aspect : nodes[0].getAspects()) {
                    if (aspect.equals(Constants.ASPECT_VERSIONABLE)) {
                        data.setVersionable(true);
                    }
                }
                if (data.isVersionable()) {
                    data.setRevisions(getVersionHistory(node.getReference()));
                }
                data.setNodeProperties(node.getProperties(), true);
            }
        } catch (Exception e) {
            data = null;
            e.printStackTrace();
        }
        return data;
    }

    @Override
    public boolean createData(T data) {
        boolean result = false;
        try {
            ParentReference parentReference = getCompanyHome();
            for (int i = 0; i < data.getPath().size(); i++) {
                parentReference = ReferenceToParent(createSpace(parentReference, data.getPath().get(i)));
            }
            ParentReference docParent = new ParentReference(STORE, null, parentReference.getPath(), Constants.ASSOC_CONTAINS,
                    Constants.createQNameString(data.getNamespace(), data.getDisplayName()));

            CMLCreate createNode = new CMLCreate(null, docParent, null, null, null,
                    Constants.createQNameString(data.getNamespace(), data.getNodeType()), data.getNodeProperties());
            CML cml = new CML();
            cml.setCreate(new CMLCreate[]{createNode});
            UpdateResult[] results = getRepositoryService().update(cml);

            for (UpdateResult updateResult : results) {
                String sourceId = "none";
                Reference source = updateResult.getSource();
                if (source != null) {
                    sourceId = source.getUuid();
                }
                String destinationId = "none";
                Reference destination = updateResult.getDestination();
                if (destination != null) {
                    destinationId = destination.getUuid();
                }
                System.out.println("Command = " + updateResult.getStatement() + "; Source = " + sourceId + "; Destination = " + destinationId);
                data.setId(destinationId);
            }

            result = true;
        } catch (Exception e) {
            result = false;
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public boolean updateData(T data) {
        boolean result = false;
        try {
            Node node = getNode(data);
            if (node != null) {
                CMLUpdate updateNode = new CMLUpdate(data.getNodeProperties(), new Predicate(new Reference[]{node.getReference()}, null, null), null);
                CML cml = new CML();
                cml.setUpdate(new CMLUpdate[]{updateNode});
                UpdateResult[] results = getRepositoryService().update(cml);

                for (UpdateResult updateResult : results) {
                    String sourceId = "none";
                    Reference source = updateResult.getSource();
                    if (source != null) {
                        sourceId = source.getUuid();
                    }
                    String destinationId = "none";
                    Reference destination = updateResult.getDestination();
                    if (destination != null) {
                        destinationId = destination.getUuid();
                    }
                    System.out.println("Command = " + updateResult.getStatement() + "; Source = " + sourceId + "; Destination = " + destinationId);
                }
                result = true;
            } else {
                System.out.println("Node not found, creating it.");
                result = createData(data);
            }
        } catch (Exception e) {
            result = false;
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public boolean deleteData(T data) {
        boolean result = false;
        try {
            Reference nodeReference = new Reference(STORE, data.getId(), null);
            CMLDelete delete = new CMLDelete(new Predicate(new Reference[]{nodeReference}, null, null));
            CML cml = new CML();
            cml.setDelete(new CMLDelete[]{delete});
            WebServiceFactory.getRepositoryService().update(cml);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }


    public boolean createContent(T data, InputStream inputStream) {
        boolean result = false;
        try {
            if (updateData(data)) {
                Node node = getNode(data);
                if (node != null) {
                    byte[] bytes = ContentUtils.convertToByteArray(inputStream);
                    System.out.println("Content length - " + bytes.length);
                    getContentService().write(node.getReference(), Constants.PROP_CONTENT, bytes, getContentFormat());
                    result = true;
                } else {
                    System.out.println("Failure during node update.");
                }
            } else {
                System.out.println("Unable to get node.");
            }
        } catch (Exception e) {
            result = false;
            e.printStackTrace();
        }
        return result;
    }

    public boolean createContent(T data, byte[] bytes) {
        boolean result = false;
        try {
            if (updateData(data)) {
                Node node = getNode(data);
                if (node != null) {
                    System.out.println("Content length - " + bytes.length);
                    getContentService().write(node.getReference(), Constants.PROP_CONTENT, bytes, getContentFormat());
                    result = true;
                } else {
                    System.out.println("Failure during node update.");
                }
            } else {
                System.out.println("Unable to get node.");
            }
        } catch (Exception e) {
            result = false;
            e.printStackTrace();
        }
        return result;
    }

    public byte[] getContent(T data) {
        byte[] bytes = new byte[]{};
        try {
            Node node = getNode(data);
            if (node != null) {
                Content[] contentArray = getContentService().read(new Predicate(new Reference[]{node.getReference()},
                        null, null), Constants.PROP_CONTENT);
                if (contentArray.length > 0) {
                    System.out.println("found content");
                    Content content = contentArray[0];
                    System.out.println("content size - " + content.getLength());
                    bytes = ContentUtils.convertToByteArray(ContentUtils.getContentAsInputStream(content));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bytes;
    }

    public long getContentSize(T data) {
        long result = 0;
        try {
            Node node = getNode(data);
            if (node != null) {
                Content[] contentArray = getContentService().read(new Predicate(new Reference[]{node.getReference()}, null, null), Constants.PROP_CONTENT);
                if (contentArray.length > 0) {
                    Content content = contentArray[0];
                    result = content.getLength();
                    System.out.println("content size - " + result);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public boolean createVersion(T data, InputStream inputStream, int authorId, String fileName) {
        boolean result = false;
        try {
            Reference nodeReference = getNodeReference(data);
            if (!isVersionable(nodeReference)) {
                makeVersionable(nodeReference);
            }
            Predicate itemsToCheckOut = new Predicate(new Reference[]{nodeReference}, null, null);
            CheckoutResult checkOutResult = getAuthoringService().checkout(itemsToCheckOut, null);
            Reference workingCopyReference = checkOutResult.getWorkingCopies()[0];

            byte[] bytes = ContentUtils.convertToByteArray(inputStream);
            System.out.println("Content length - " + bytes.length);
            getContentService().write(workingCopyReference, Constants.PROP_CONTENT, bytes, getContentFormat());

            Predicate predicate = new Predicate(new Reference[]{workingCopyReference}, null, null);

            NamedValue[] comments = new NamedValue[]{Utils.createNamedValue("author", Integer.toString(authorId)),
                    Utils.createNamedValue("revisionFileName", URLEncoder.encode(fileName, "UTF-8"))};
            getAuthoringService().checkin(predicate, comments, false);
            result = true;
        } catch (Exception e) {
            result = false;
            e.printStackTrace();
        }
        return result;
    }

    public boolean createVersion(T data, byte[] bytes, int authorId, String fileName, boolean majorVersion) {
        boolean result = false;
        try {
            Reference nodeReference = getNodeReference(data);
            if (!isVersionable(nodeReference)) {
                makeVersionable(nodeReference);
            }
            Predicate itemsToCheckOut = new Predicate(new Reference[]{nodeReference}, null, null);
            CheckoutResult checkOutResult = getAuthoringService().checkout(itemsToCheckOut, null);
            Reference workingCopyReference = checkOutResult.getWorkingCopies()[0];

            System.out.println("Content length - " + bytes.length);
            getContentService().write(workingCopyReference, Constants.PROP_CONTENT, bytes, getContentFormat());

            Predicate predicate = new Predicate(new Reference[]{workingCopyReference}, null, null);

            NamedValue[] comments = majorVersion ?
                    new NamedValue[]{Utils.createNamedValue("author", Integer.toString(authorId)),
                            Utils.createNamedValue("revisionFileName", URLEncoder.encode(fileName, "UTF-8")),
                            Utils.createNamedValue("versionType", "MAJOR")} :
                    new NamedValue[]{Utils.createNamedValue("author", Integer.toString(authorId)),
                            Utils.createNamedValue("revisionFileName", URLEncoder.encode(fileName, "UTF-8")),
                            Utils.createNamedValue("versionType", "MINOR")};

            getAuthoringService().checkin(predicate, comments, false);
            result = true;
        } catch (Exception e) {
            result = false;
            e.printStackTrace();
        }
        return result;
    }

    public List<Revision> getRevisions(T data) {
        List<Revision> result = new ArrayList<Revision>();
        try {
            Reference nodeReference = getNodeReference(data);
            if (!isVersionable(nodeReference)) {
                makeVersionable(nodeReference);
            }
            result = getVersionHistory(nodeReference);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public byte[] getContent(T data, String version) {
        byte[] bytes = new byte[]{};
        try {
            Node node = getNode(data);
            if (node != null) {
                VersionHistory versionHistory = getAuthoringService().getVersionHistory(node.getReference());
                Version[] versions = versionHistory.getVersions();

                for (Version nodeVersion : versions) {
                    if (nodeVersion.getLabel().equals(version)) {
                        Reference reference = new Reference(STORE_LIGHTWEIGHT, nodeVersion.getId().getUuid(), nodeVersion.getId().getPath());

                        Content[] contentArray = getContentService().read(new Predicate(new Reference[]{reference}, null, null), Constants.TYPE_CONTENT);
                        if (contentArray.length > 0) {
                            System.out.println("found revision content");
                            Content content = contentArray[0];
                            System.out.println("content size - " + content.getLength());
                            bytes = ContentUtils.convertToByteArray(ContentUtils.getContentAsInputStream(content));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bytes;
    }


    /* </MAIN_METHODS> */


    /* <PRIVATE_METHODS> */

    private Node getNode(T data) throws Exception {
        if (data.getId() == null || data.getId().equals("")) return null;
        ParentReference parentReference = getCompanyHome();
        for (int i = 0; i < data.getPath().size(); i++) {
            System.out.println("entering space " + data.getPath().get(i));
            parentReference = ReferenceToParent(createSpace(parentReference, data.getPath().get(i)));
        }
        Reference nodeReference = new Reference(STORE, data.getId(), parentReference.getPath());
        Predicate predicate = new Predicate(new Reference[]{nodeReference}, null, null);
        Node[] nodes = getRepositoryService().get(predicate);
        if (nodes.length > 0) {
            System.out.println("Found node");
            return nodes[0];
        } else {
            return null;
        }
    }

    private Reference getNodeReference(T data) throws Exception {
        if (data.getId() == null || data.getId().equals("")) return null;
        ParentReference parentReference = getCompanyHome();
        for (int i = 0; i < data.getPath().size(); i++) {
            System.out.println("entering space " + data.getPath().get(i));
            parentReference = ReferenceToParent(createSpace(parentReference, data.getPath().get(i)));
        }
        return new Reference(STORE, data.getId(), parentReference.getPath());
    }
}