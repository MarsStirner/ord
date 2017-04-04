package ru.efive.dao.alfresco.test;

import ru.efive.dao.alfresco.AlfrescoDAO;
import ru.efive.dao.alfresco.AlfrescoNode;

import java.util.List;

public class GetNodeTest {

    public GetNodeTest() {

    }

    //@Test
    public void testGetAndDeleteMethods() throws Exception {
        AlfrescoDAO<AlfrescoNode> dao = new AlfrescoDAO<>();
        dao.initClass(AlfrescoNode.class);
        dao.setLogin(AlfrescoHelper.TEST_RUNTIME_USERNAME);
        dao.setPassword(AlfrescoHelper.TEST_RUNTIME_PASSWORD);
        dao.setServerUrl(AlfrescoHelper.TEST_RUNTIME_URL);
        dao.setPath(AlfrescoHelper.TEST_RUNTIME_PATH);
        dao.connect();
        System.out.println("Retrieving data list");
        List<AlfrescoNode> list = dao.getDataList();
        String id = "";
        for (AlfrescoNode node : list) {
            System.out.println("Node: id - " + node.getId() + ", diplay name - " + node.getDisplayName());
            if (id == null || id.equals("")) {
                id = node.getId();
            }
        }
        if (id != null && !id.equals("")) {
            System.out.println("Retrieving node with id - " + id);
            AlfrescoNode node = dao.getDataById(id);
            if (node != null && node.getId() != null && !node.getId().equals("")) {
                System.out.println("Testing delete node method");
                //dao.deleteData(node);
            }
        }
        dao.disconnect();
    }

}