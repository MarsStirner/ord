package ru.efive.dms.uifaces.beans.internal;

import ru.efive.dms.dao.InternalDocumentDAOImpl;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentLazyDataModelBean;
import ru.efive.dms.uifaces.lazyDataModel.documents.LazyDataModelForPersonalDraftsInternalDocument;
import ru.entity.model.document.InternalDocument;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;

import static ru.efive.dms.util.ApplicationDAONames.INTERNAL_DOCUMENT_FORM_DAO;

@Named("personal_internal_documents")
@ViewScoped
public class PersonalInternalDocumentsListHolder  extends AbstractDocumentLazyDataModelBean<InternalDocument> implements Serializable {

    private static final long serialVersionUID = 853542007446781235L;
    private InternalDocumentDAOImpl dao;
    @Inject
    @Named("sessionManagement")
    private transient SessionManagementBean sessionManagement;

    @PostConstruct
    public void init() {
        dao = sessionManagement.getDAO(InternalDocumentDAOImpl.class, INTERNAL_DOCUMENT_FORM_DAO);
        setLazyModel(new LazyDataModelForPersonalDraftsInternalDocument(dao, sessionManagement.getAuthData()));
    }
}