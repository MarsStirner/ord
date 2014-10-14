package ru.efive.dms.uifaces.beans;

import ru.efive.dms.dao.PaperCopyDocumentDAOImpl;
import ru.efive.uifaces.bean.AbstractDocumentListHolderBean;
import ru.entity.model.document.PaperCopyDocument;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

import static ru.efive.dms.util.ApplicationDAONames.PAPER_COPY_DOCUMENT_FORM_DAO;

@Named("paperCopyDocuments")
@SessionScoped
public class PaperCopyDocumentsHolderBean extends AbstractDocumentListHolderBean<PaperCopyDocument> {

    @Override
    protected Pagination initPagination() {
        return new Pagination(0, getTotalCount(), 100);
    }

    @Override
    protected int getTotalCount() {
        int in_result;
        in_result = new Long(sessionManagement.getDAO(PaperCopyDocumentDAOImpl.class, PAPER_COPY_DOCUMENT_FORM_DAO).countAllDocuments(filters, filter, false, false)).intValue();
        return in_result;
    }

    @Override
    protected List<PaperCopyDocument> loadDocuments() {
        List<PaperCopyDocument> result = new ArrayList<PaperCopyDocument>(new HashSet<PaperCopyDocument>(sessionManagement.getDAO(PaperCopyDocumentDAOImpl.class, PAPER_COPY_DOCUMENT_FORM_DAO).findAllDocuments(filters, filter, false, false)));
        return result;
    }

    @Override
    public List<PaperCopyDocument> getDocuments() {
        return super.getDocuments();
    }

    public List<PaperCopyDocument> getAllDocumentsByParentDocumentId(String parentId) {
        List<PaperCopyDocument> result = new ArrayList<PaperCopyDocument>();
        try {
            if (parentId != null && !parentId.isEmpty()) {
                result = sessionManagement.getDAO(PaperCopyDocumentDAOImpl.class, PAPER_COPY_DOCUMENT_FORM_DAO).findAllDocumentsByParentId(parentId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public void setFilters(Map<String, Object> filters) {
        this.filters = filters;
    }

    public Map<String, Object> getFilters() {
        return filters;
    }

    private String filter;
    static private Map<String, Object> filters = new HashMap<String, Object>();

    static {
        filters.put("registrationNumber", "%");
    }

    @Inject
    @Named("sessionManagement")
    SessionManagementBean sessionManagement = new SessionManagementBean();

    private static final long serialVersionUID = 1426067769816981240L;
}