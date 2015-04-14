package ru.efive.dms.uifaces.beans.outgoing;

import com.google.common.collect.ImmutableMap;
import ru.efive.dms.dao.OutgoingDocumentDAOImpl;
import ru.efive.dms.dao.ViewFactDaoImpl;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentLazyDataModelBean;
import ru.efive.dms.uifaces.lazyDataModel.documents.LazyDataModelForOutgoingDocument;
import ru.entity.model.document.OutgoingDocument;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;

import static ru.efive.dms.util.ApplicationDAONames.OUTGOING_DOCUMENT_FORM_DAO;
import static ru.efive.dms.util.ApplicationDAONames.VIEW_FACT_DAO;

@ManagedBean(name = "outDocumentsOnAgreement")
@ViewScoped
public class OutgoingDocumentsOnAgreementHolder extends AbstractDocumentLazyDataModelBean<OutgoingDocument> implements Serializable {

    private static final long serialVersionUID = 8535420074467871583L;
    private OutgoingDocumentDAOImpl dao;
    private ViewFactDaoImpl viewFactDao;
    @Inject
    @Named("sessionManagement")
    private transient SessionManagementBean sessionManagement;

    @PostConstruct
    public void init() {
        dao = sessionManagement.getDAO(OutgoingDocumentDAOImpl.class, OUTGOING_DOCUMENT_FORM_DAO);
        viewFactDao = sessionManagement.getDAO(ViewFactDaoImpl.class, VIEW_FACT_DAO);
        final LazyDataModelForOutgoingDocument lazyDM = new LazyDataModelForOutgoingDocument(dao, viewFactDao, sessionManagement.getAuthData());
        lazyDM.setFilters(ImmutableMap.of("statusId", (Object)"3"));
        setLazyModel(lazyDM);
    }
}