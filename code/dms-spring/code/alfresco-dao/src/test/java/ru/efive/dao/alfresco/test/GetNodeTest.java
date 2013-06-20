package ru.efive.dao.alfresco.test;

import java.util.List;

import org.junit.Test;

import ru.efive.dao.alfresco.AlfrescoDAO;
import ru.efive.dao.alfresco.AlfrescoNode;

public class GetNodeTest {
	
	public GetNodeTest() {
		
	}
	
	//@Test
	public void testGetAndDeleteMethods() throws Exception {
		AlfrescoDAO<AlfrescoNode> dao = new AlfrescoDAO<AlfrescoNode>(AlfrescoNode.class);
		dao.setUserName(AlfrescoHelper.TEST_RUNTIME_USERNAME);
		dao.setPassword(AlfrescoHelper.TEST_RUNTIME_PASSWORD);
		dao.connect();
		System.out.println("Retrieving data list");
		List<AlfrescoNode> list = dao.getDataList();
		String id = "";
		for (AlfrescoNode node:list) {
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