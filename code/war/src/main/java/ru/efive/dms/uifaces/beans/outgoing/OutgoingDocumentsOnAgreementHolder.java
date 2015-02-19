package ru.efive.dms.uifaces.beans.outgoing;

import ru.efive.dms.dao.OutgoingDocumentDAOImpl;
import ru.efive.dms.dao.ViewFactDaoImpl;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.uifaces.bean.AbstractDocumentListHolderBean;
import ru.efive.uifaces.bean.Pagination;
import ru.entity.model.document.OutgoingDocument;
import ru.entity.model.enums.DocumentStatus;
import ru.entity.model.user.User;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.efive.dms.util.ApplicationDAONames.OUTGOING_DOCUMENT_FORM_DAO;
import static ru.efive.dms.util.ApplicationDAONames.VIEW_FACT_DAO;

@ManagedBean(name="outDocumentsOnAgreement")
@ViewScoped
public class OutgoingDocumentsOnAgreementHolder extends AbstractDocumentListHolderBean<OutgoingDocument> {
    private static final long serialVersionUID = 8535420074467871583L;
    @Override
    protected Pagination initPagination() {
        return new Pagination(0, getTotalCount(), 50);
        //Ранжирование по страницам по-умолчанию  (50 на страницу, начиная с нуля)
    }

    @Override
    protected Sorting initSorting() {
        return new Sorting("registrationDate", false);
        //Сортировка по-умолчанию (дата регистрации документа)
    }

    @Override
    protected int getTotalCount() {
        if (!sessionManagement.isSubstitution()) {
            //Без замещения
            return new Long(
                    sessionManagement.getDAO(OutgoingDocumentDAOImpl.class, OUTGOING_DOCUMENT_FORM_DAO)
                            .countAllDocumentsByUser(filters, filter, sessionManagement.getLoggedUser(), false, false)
            ).intValue();
        }  else {
            // С замещением
            final List<User> userList = new ArrayList<User>(sessionManagement.getSubstitutedUsers());
            userList.add(sessionManagement.getLoggedUser());
            return new Long(sessionManagement.getDAO(OutgoingDocumentDAOImpl.class, OUTGOING_DOCUMENT_FORM_DAO).countAllDocumentsByUserList(filters, filter, userList, false, false)).intValue();
        }
    }

    @Override
    protected List<OutgoingDocument> loadDocuments() {
        final List<OutgoingDocument> resultList;
        if (!sessionManagement.isSubstitution()) {
            // Без замещения
            resultList = sessionManagement.getDAO(OutgoingDocumentDAOImpl.class, OUTGOING_DOCUMENT_FORM_DAO)
                    .findAllDocumentsByUser(filters, filter, sessionManagement.getLoggedUser(), false, false, getPagination().getOffset(), getPagination().getPageSize(), getSorting().getColumnId(), getSorting().isAsc());
        } else {
            // С замещением
            final List<User> userList = new ArrayList<User>(sessionManagement.getSubstitutedUsers());
            userList.add(sessionManagement.getLoggedUser());
            resultList = sessionManagement.getDAO(OutgoingDocumentDAOImpl.class, OUTGOING_DOCUMENT_FORM_DAO)
                    .findAllDocumentsByUserList(filters, filter, userList, false, false, getPagination().getOffset(), getPagination().getPageSize(), getSorting().getColumnId(), getSorting().isAsc());
        }
        if(!resultList.isEmpty()){
            sessionManagement.getDAO(ViewFactDaoImpl.class, VIEW_FACT_DAO).applyViewFlagsOnOutgoingDocumentList(resultList, sessionManagement.getLoggedUser());
        }
        return resultList;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }


    @Inject
    @Named("sessionManagement")
    private transient SessionManagementBean sessionManagement;

    private Map<String, Object> filters = new HashMap<String, Object>();

    //Initialize block
    {
        filters.put("statusId", DocumentStatus.AGREEMENT_3.getId());
    }

    private String filter;
}