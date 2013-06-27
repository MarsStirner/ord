package ru.efive.uifaces.beans;

import java.util.List;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import ru.efive.uifaces.dao.TestDocumentDao;
import ru.efive.uifaces.data.TestDocument;

/**
 *
 *
 * @author Denis Kotegov
 */
@ManagedBean(name="documents")
@SessionScoped
public class TestDocumentListBean {
    
    public List<TestDocument> getAllDocuments() {
        return TestDocumentDao.getAll();
    }
    
}
