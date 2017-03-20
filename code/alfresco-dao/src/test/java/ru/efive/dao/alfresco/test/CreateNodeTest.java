package ru.efive.dao.alfresco.test;

import java.util.ArrayList;
import java.util.List;

import ru.efive.dao.alfresco.AlfrescoDAO;
import ru.efive.dao.alfresco.AlfrescoNode;

public class CreateNodeTest {

    public CreateNodeTest() {

    }

    //@Test
    public void testCreateAndUpdateMethod() throws Exception {
        AlfrescoDAO<AlfrescoNode> dao = new AlfrescoDAO<>();
        dao.initClass(AlfrescoNode.class);
        dao.setLogin(AlfrescoHelper.TEST_RUNTIME_USERNAME);
        dao.setPassword(AlfrescoHelper.TEST_RUNTIME_PASSWORD);
        dao.setServerUrl(AlfrescoHelper.TEST_RUNTIME_URL);
        dao.setPath(AlfrescoHelper.TEST_RUNTIME_PATH);
        dao.connect();
        AlfrescoNode alfrescoNode = new AlfrescoNode();
        alfrescoNode.setDisplayName("001");
        List<String> path = new ArrayList<>();
        path.add("E5_dms_space");
        alfrescoNode.setPath(path);
        //dao.createData(alfrescoNode);
        //InputStream is = new java.io.FileInputStream("c:/temp/dfc.properties");
        //dao.createContent(alfrescoNode, is);
        System.out.println("Trying to update node.");
        alfrescoNode.setDisplayName("002");
        //dao.updateData(alfrescoNode);
        System.out.println("Successfully created and updated node.");
        dao.disconnect();
        //is.close();
    }

}