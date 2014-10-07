package ru.efive.dms.uifaces.beans.incoming;


import java.util.List;

import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import ru.efive.sql.entity.user.User;
import ru.efive.dms.dao.IncomingDocumentDAOImpl;
import ru.efive.dms.data.IncomingDocument;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.util.ApplicationHelper;
import ru.efive.uifaces.bean.AbstractDocumentListHolderBean;

@Named("in_documents_by_expired_date")
@ViewScoped
public class IncomingDocumentsByExpiredDate extends AbstractDocumentListHolderBean<IncomingDocument> {
    @Override
    protected Pagination initPagination() {
        return new Pagination(0, getTotalCount(), 100);
    }

    @Override
    protected int getTotalCount() {
        int result = 0;
        try {
            User user = sessionManagement.getLoggedUser();
           // user = sessionManagement.getDAO(UserDAOHibernate.class, ApplicationHelper.USER_DAO).findByLoginAndPassword(user.getLogin(), user.getPassword());
            return new Long(sessionManagement.getDAO(IncomingDocumentDAOImpl.class, ApplicationHelper.INCOMING_DOCUMENT_FORM_DAO).countControlledDocumentsByUser(filter,
                    user, false)).intValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    protected List<IncomingDocument> loadDocuments() {
        return null;
    }

    @Override
    protected Sorting initSorting() {
        return new Sorting("registrationDate", true);
    }



    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    private String filter;

    @Inject
    @Named("sessionManagement")
    private transient SessionManagementBean sessionManagement;

    private static final long serialVersionUID = 8535420074467871583L;
}