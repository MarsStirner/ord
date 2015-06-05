package ru.efive.dms.uifaces.beans.request;

import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentLazyDataModelBean;
import ru.efive.dms.uifaces.lazyDataModel.documents.LazyDataModelForPersonalDraftsRequestDocument;
import ru.entity.model.document.RequestDocument;
import ru.hitsl.sql.dao.RequestDocumentDAOImpl;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;

import static ru.hitsl.sql.dao.util.ApplicationDAONames.REQUEST_DOCUMENT_FORM_DAO;

@Named("personal_request_documents")
@ViewScoped
public class PersonalRequestDocumentListHolder extends AbstractDocumentLazyDataModelBean<RequestDocument> implements Serializable {

    private static final long serialVersionUID = 853542007446781235L;
    private RequestDocumentDAOImpl dao;
    @Inject
    @Named("sessionManagement")
    private transient SessionManagementBean sessionManagement;

    @PostConstruct
    public void init() {
        dao = sessionManagement.getDAO(RequestDocumentDAOImpl.class, REQUEST_DOCUMENT_FORM_DAO);
        setLazyModel(new LazyDataModelForPersonalDraftsRequestDocument(dao, sessionManagement.getAuthData()));
    }
}