package ru.efive.dao.alfresco;

//import org.alfresco.webservice.types.NamedValue;
//import org.alfresco.webservice.util.Constants;
//import org.alfresco.webservice.util.Utils;

import java.util.ArrayList;
import java.util.List;

public class AlfrescoNode extends ru.efive.dao.Data {

    private static final long serialVersionUID = 8499097170887955380L;
    private List<String> path = new ArrayList<>();
    private String nodeType;
    private String namespace;
    private String namespacePrefix;
    private String displayName;
    private boolean versionable;
    private List<Revision> revisions;

    public AlfrescoNode() {
        nodeType = "content";
        namespace = "http://www.alfresco.org/model/content/1.0";
        namespacePrefix = "cm";
    }

    public List<String> getPath() {
        return path;
    }

    public void setPath(List<String> path) {
        this.path = path;
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getNamespacePrefix() {
        return namespacePrefix;
    }

    public void setNamespacePrefix(String namespacePrefix) {
        this.namespacePrefix = namespacePrefix;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public boolean isVersionable() {
        return versionable;
    }

    public void setVersionable(boolean versionable) {
        this.versionable = versionable;
    }

    public List<Revision> getRevisions() {
        return revisions;
    }

    public void setRevisions(List<Revision> revisions) {
        this.revisions = revisions;
    }

//    public String getNamedNodeType() {
//        return Constants.createQNameString(getNamespace(), getNodeType());
//    }
//
//
//    public boolean setNodeProperties(NamedValue[] nodeProperties) {
//        boolean result;
//        try {
//            for (NamedValue namedValue : nodeProperties) {
//                if (namedValue.getName().endsWith(Constants.PROP_NAME)) {
//                    setDisplayName(namedValue.getValue());
//                }
//            }
//            result = true;
//        } catch (Exception e) {
//            result = false;
//            e.printStackTrace();
//        }
//        return result;
//    }
//
//    public boolean setNodeProperties(NamedValue[] nodeProperties, boolean loadLinked) {
//        return setNodeProperties(nodeProperties);
//    }
//
//    public NamedValue[] getNodeProperties() {
//        return new NamedValue[]{Utils.createNamedValue(Constants.PROP_NAME, getDisplayName())};
//    }
}