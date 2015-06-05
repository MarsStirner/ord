package ru.efive.dms.uifaces.beans.contragent;

import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentLazyDataModelBean;
import ru.efive.dms.uifaces.lazyDataModel.LazyDataModelForContragent;
import ru.entity.model.referenceBook.Contragent;
import ru.hitsl.sql.dao.ContragentDAOHibernate;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import static ru.hitsl.sql.dao.util.ApplicationDAONames.CONTRAGENT_DAO;

@Named("contragents")
@ViewScoped
public class ContragentListHolderBean extends AbstractDocumentLazyDataModelBean<Contragent>{

    private ContragentDAOHibernate dao;
    @Inject
    @Named("sessionManagement")
    private transient SessionManagementBean sessionManagement;

    @PostConstruct
    public void init() {
        dao = sessionManagement.getDAO(ContragentDAOHibernate.class, CONTRAGENT_DAO);
        setLazyModel(new LazyDataModelForContragent(dao));
    }

}