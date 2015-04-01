package ru.efive.dms.uifaces.beans.incoming;

import ru.efive.dms.dao.IncomingDocumentDAOImpl;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentLazyDataModelBean;
import ru.efive.dms.uifaces.lazyDataModel.documents.LazyDataModelForPersonalDraftsIncomingDocument;
import ru.entity.model.document.IncomingDocument;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;

import static ru.efive.dms.util.ApplicationDAONames.INCOMING_DOCUMENT_FORM_DAO;

@ManagedBean(name = "personal_in_documents")
@ViewScoped
public class PersonalIncomingDocumentListHolder extends AbstractDocumentLazyDataModelBean<IncomingDocument> implements Serializable {

    private static final long serialVersionUID = 853542007446781235L;
    private IncomingDocumentDAOImpl dao;
    @Inject
    @Named("sessionManagement")
    private transient SessionManagementBean sessionManagement;

    @PostConstruct
    public void init() {
        dao = sessionManagement.getDAO(IncomingDocumentDAOImpl.class, INCOMING_DOCUMENT_FORM_DAO);
        setLazyModel(new LazyDataModelForPersonalDraftsIncomingDocument(dao, sessionManagement.getAuthData()));
    }
}