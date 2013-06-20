package ru.efive.dms.data;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.alfresco.webservice.types.NamedValue;
import org.alfresco.webservice.util.Constants;
import org.alfresco.webservice.util.Utils;

import ru.efive.dao.alfresco.AlfrescoNode;
import ru.efive.dao.alfresco.Revision;
import ru.efive.dms.util.ApplicationHelper;

/**
 * Вложение
 *
 * @author Alexey Vagizov
 */
public class Attachment extends AlfrescoNode {

    /**
     * Конструктор по умолчанию
     */
    public Attachment() {
        super();
        setNamespace(ApplicationHelper.NAMESPACE);
        setNamespacePrefix(ApplicationHelper.NAMESPACE_PREFIX);
        setNodeType(ApplicationHelper.TYPE_FILE);
        List<String> path = new ArrayList<String>();
        path.add(ApplicationHelper.STORE_NAME);
        path.add("Attachments");
        setPath(path);
    }

    public void setAuthorId(int authorId) {
        this.authorId = authorId;
    }

    public int getAuthorId() {
        return authorId;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getCreated() {
        return created;
    }

    public Revision getCurrentRevision() {
        if (isVersionable() && getRevisions().size() > 0) {
            return getRevisions().get(0);
        } else return new Revision("1.0", getAuthorId(), getFileName());
    }

    @Override
    public boolean setNodeProperties(NamedValue[] nodeProperties) {
        boolean result = false;
        try {
            for (NamedValue namedValue : nodeProperties) {
                if (namedValue.getName().endsWith(Constants.PROP_NAME)) {
                    setDisplayName(namedValue.getValue());
                }
                /*else if (namedValue.getName().endsWith(Constants.PROP_CREATED)) {
                        setCreated(new java.text.SimpleDateFormat("YYYY-MM-DD'T'hh:mm:ss.mmm+hh:mm").parse(namedValue.getValue()));
                    }*/
                else if (namedValue.getName().endsWith("parentId")) {
                    setParentId(namedValue.getValue());
                } else if (namedValue.getName().endsWith("authorId")) {
                    String value = namedValue.getValue();
                    if (value != null && !value.equals("")) {
                        setAuthorId(Integer.parseInt(value));
                    }
                } else if (namedValue.getName().endsWith("fileName")) {
                    String fileName = URLDecoder.decode(namedValue.getValue(), "UTF-8");
                    setFileName(fileName);
                }
            }
            result = true;
        } catch (Exception e) {
            result = false;
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public NamedValue[] getNodeProperties() {
        try {
            if (getDisplayName() == null || getDisplayName().equals("")) {
                setDisplayName(new String((new SimpleDateFormat("ddMMyyyy_HHmmss").format(getCreated()) + "-" + getParentId()).getBytes(), "UTF-8"));
            }
            String fileName = URLEncoder.encode(getFileName(), "UTF-8");
            System.out.println(fileName);
            NamedValue namedValue[] = {Utils.createNamedValue(Constants.PROP_NAME, getDisplayName()),
                    Utils.createNamedValue(Constants.createQNameString(getNamespace(), "parentId"), getParentId()),
                    Utils.createNamedValue(Constants.createQNameString(getNamespace(), "authorId"), Integer.toString(getAuthorId())),
                    Utils.createNamedValue(Constants.createQNameString(getNamespace(), "fileName"), fileName)};
            return namedValue;
        } catch (Exception e) {
            e.printStackTrace();
            return new NamedValue[]{};
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result
                + ((getId() == null) ? 0 : getId().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        Attachment other = (Attachment) obj;
        if (getId() == null) {
            if (other.getId() != null)
                return false;
        } else if (!getId().equals(other.getId()))
            return false;
        return true;
    }


    private int authorId;
    private String parentId;
    private String fileName;
    private String displayName;
    private Date created;

    private static final long serialVersionUID = -2722742909627205138L;
}