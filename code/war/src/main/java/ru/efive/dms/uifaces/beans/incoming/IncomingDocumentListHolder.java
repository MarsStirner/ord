package ru.efive.dms.uifaces.beans.incoming;

import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentLazyDataModelBean;
import ru.efive.dms.uifaces.lazyDataModel.documents.LazyDataModelForIncomingDocument;
import ru.entity.model.document.IncomingDocument;
import ru.hitsl.sql.dao.IncomingDocumentDAOImpl;
import ru.hitsl.sql.dao.ViewFactDaoImpl;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;

import static ru.hitsl.sql.dao.util.ApplicationDAONames.INCOMING_DOCUMENT_FORM_DAO;
import static ru.hitsl.sql.dao.util.ApplicationDAONames.VIEW_FACT_DAO;


@Named("in_documents")
@ViewScoped
public class IncomingDocumentListHolder extends AbstractDocumentLazyDataModelBean<IncomingDocument> implements Serializable {

    private static final long serialVersionUID = 8535420074467871583L;
    private IncomingDocumentDAOImpl dao;
    private ViewFactDaoImpl viewFactDao;
    @Inject
    @Named("sessionManagement")
    private transient SessionManagementBean sessionManagement;

    @PostConstruct
    public void init() {
        dao = sessionManagement.getDAO(IncomingDocumentDAOImpl.class, INCOMING_DOCUMENT_FORM_DAO);
        viewFactDao = sessionManagement.getDAO(ViewFactDaoImpl.class, VIEW_FACT_DAO);
        setLazyModel(new LazyDataModelForIncomingDocument(dao, viewFactDao, sessionManagement.getAuthData()));
    }
}