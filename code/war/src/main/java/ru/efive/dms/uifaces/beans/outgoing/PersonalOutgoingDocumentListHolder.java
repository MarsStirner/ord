package ru.efive.dms.uifaces.beans.outgoing;

import ru.efive.dms.dao.OutgoingDocumentDAOImpl;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentLazyDataModelBean;
import ru.efive.dms.uifaces.lazyDataModel.documents.LazyDataModelForPersonalDraftsOutgoingDocument;
import ru.entity.model.document.OutgoingDocument;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;

import static ru.efive.dms.util.ApplicationDAONames.OUTGOING_DOCUMENT_FORM_DAO;

@ManagedBean(name = "personal_out_documents")
@ViewScoped
public class PersonalOutgoingDocumentListHolder extends AbstractDocumentLazyDataModelBean<OutgoingDocument> implements Serializable {

    private static final long serialVersionUID = 853542007446781235L;
    private OutgoingDocumentDAOImpl dao;
    @Inject
    @Named("sessionManagement")
    private transient SessionManagementBean sessionManagement;

    @PostConstruct
    public void init() {
        dao = sessionManagement.getDAO(OutgoingDocumentDAOImpl.class, OUTGOING_DOCUMENT_FORM_DAO);
        setLazyModel(new LazyDataModelForPersonalDraftsOutgoingDocument(dao, sessionManagement.getAuthData()));
    }
}
