package ru.efive.dms.config;

import org.apache.jackrabbit.commons.JcrUtils;
import ru.hitsl.sql.dao.util.AuthorizationData;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Reception;
import javax.jcr.*;
import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static javax.jcr.nodetype.NodeType.MIX_VERSIONABLE;


public class JCR {

    private Repository repository;

    @PostConstruct
    public void init(){
        try {
            this.repository= JcrUtils.getRepository("http://localhost:8080/jackrabbit/server");
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
    }


    public Node importFile (Node folderNode, String fileName, Value content, String mimeType,
                            String encoding) throws RepositoryException, IOException
    {
        //create the file node - see section 6.7.22.6 of the spec
        Node fileNode = folderNode.addNode (fileName, "nt:file");

        //create the mandatory child node - jcr:content
        Node resNode = fileNode.addNode ("jcr:content", "nt:resource");
        resNode.setProperty ("jcr:mimeType", mimeType);
        resNode.setProperty ("jcr:encoding", encoding);
        resNode.setProperty ("jcr:data", content);
        resNode.addMixin(MIX_VERSIONABLE);
        return fileNode;
    }

    public void createAttachment(String uniqueId, String fileName, LocalDateTime now, AuthorizationData authData, String contentType, byte[] fileContent) throws RepositoryException, IOException {
        Session session = repository.login(new SimpleCredentials("admin", "admin".toCharArray()));
        Node documentNode = getOrCreateNode(session.getRootNode(), uniqueId);

        ValueFactory factory = session.getValueFactory();
        InputStream is = new ByteArrayInputStream(fileContent);
        Binary binary = factory.createBinary(is);
        Value value = factory.createValue(binary);

        importFile(documentNode, fileName, value, contentType, "UTF-8");

        session.save();
    }

    private Node getOrCreateNode(Node rootNode, String uniqueId) throws RepositoryException {
        return rootNode.hasNode(uniqueId) ? rootNode.getNode(uniqueId) : rootNode.addNode(uniqueId);
    }

    public List<Node> getAttachments(String uniqueId) {
        try {
            Session session = repository.login(new SimpleCredentials("admin", "admin".toCharArray()));
            if (!session.getRootNode().hasNode(uniqueId)) {
                return Collections.emptyList();
            }
            final List<Node> result = new ArrayList<>();
            Node node = session.getRootNode().getNode(uniqueId);
            NodeIterator nodeIterator = node.getNodes();
            while (nodeIterator.hasNext()) {
                result.add((Node) nodeIterator.next());
                //node.getNode("jcr:content").getProperty("jcr:data");
            }
            return result;
        } catch(Exception e){
            return Collections.emptyList();
        }
    }
}
