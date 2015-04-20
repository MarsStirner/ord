package ru.efive.dms.uifaces.beans;

import ru.efive.dms.dao.RecordBookDocumentDAOImpl;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentLazyDataModelBean;
import ru.efive.dms.uifaces.lazyDataModel.documents.LazyDataModelForRecordBookDocument;
import ru.entity.model.document.RecordBookDocument;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import static ru.efive.dms.util.ApplicationDAONames.RECORD_BOOK_DAO;

@ManagedBean(name = "record_book_documents")
@ViewScoped
public class RecordBookDocumentListHolder extends AbstractDocumentLazyDataModelBean<RecordBookDocument> {

      private RecordBookDocumentDAOImpl dao;
    @Inject
    @Named("sessionManagement")
    private transient SessionManagementBean sessionManagement;

    @PostConstruct
    public void init() {
        dao = sessionManagement.getDAO(RecordBookDocumentDAOImpl.class, RECORD_BOOK_DAO);
        setLazyModel(new LazyDataModelForRecordBookDocument(dao, sessionManagement.getAuthData()));
    }
}