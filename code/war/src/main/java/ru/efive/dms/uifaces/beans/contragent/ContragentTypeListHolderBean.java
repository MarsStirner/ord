package ru.efive.dms.uifaces.beans.contragent;

import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentLazyDataModelBean;
import ru.efive.dms.uifaces.lazyDataModel.referencebook.LazyDataModelForContragentType;
import ru.entity.model.referenceBook.ContragentType;
import ru.hitsl.sql.dao.referenceBook.ContragentTypeDAOImpl;
import ru.hitsl.sql.dao.util.ApplicationDAONames;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * Author: Upatov Egor <br>
 * Date: 12.02.2015, 15:25 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
@Named("contragentTypeList")
@ViewScoped
public class ContragentTypeListHolderBean extends AbstractDocumentLazyDataModelBean<ContragentType>{
    private ContragentTypeDAOImpl dao;
    @Inject
    @Named("sessionManagement")
    private transient SessionManagementBean sessionManagement;

    @PostConstruct
    public void init() {
        dao = sessionManagement.getDAO(ContragentTypeDAOImpl.class, ApplicationDAONames.RB_CONTRAGENT_TYPE_DAO);
        setLazyModel(new LazyDataModelForContragentType(dao));
    }

}