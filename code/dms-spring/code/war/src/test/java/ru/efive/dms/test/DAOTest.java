package ru.efive.dms.test;

import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import ru.efive.wf.core.dao.EngineDAOImpl;
import ru.efive.wf.core.data.HumanTaskTree;

public class DAOTest {
	
	public DAOTest() {
		
	}
	
	@Test
	public void testDAO() throws Exception {
		//ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("application-context.xml");
		//EngineDAOImpl dao = (EngineDAOImpl) ctx.getBean("engineDao");
		//HumanTaskTree tree = dao.get(HumanTaskTree.class, 1);
		//if (tree != null) tree.loadHumanTaskTree();
		//ctx.close();
	}
}