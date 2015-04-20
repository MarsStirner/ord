package ru.efive.dms.uifaces.beans.contragent;

import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentLazyDataModelBean;
import ru.efive.dms.uifaces.lazyDataModel.referencebook.LazyDataModelForContragentType;
import ru.efive.dms.util.ApplicationDAONames;
import ru.efive.sql.dao.RbContragentTypeDAOImpl;
import ru.entity.model.crm.ContragentType;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * Author: Upatov Egor <br>
 * Date: 12.02.2015, 15:25 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
@ManagedBean(name = "contragentTypeList")
@ViewScoped
public class ContragentTypeListHolderBean extends AbstractDocumentLazyDataModelBean<ContragentType>{
    private RbContragentTypeDAOImpl dao;
    @Inject
    @Named("sessionManagement")
    private transient SessionManagementBean sessionManagement;

    @PostConstruct
    public void init() {
        dao = sessionManagement.getDAO(RbContragentTypeDAOImpl.class, ApplicationDAONames.RB_CONTRAGENT_TYPE_DAO);
        setLazyModel(new LazyDataModelForContragentType(dao));
    }

}