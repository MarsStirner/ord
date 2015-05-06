package ru.efive.dms.uifaces.beans;

import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentLazyDataModelBean;
import ru.efive.dms.uifaces.lazyDataModel.LazyDataModelForGroup;
import ru.efive.sql.dao.user.GroupDAOHibernate;
import ru.entity.model.user.Group;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import static ru.efive.dms.util.ApplicationDAONames.GROUP_DAO;

@Named("groups")
@ViewScoped
public class GroupsHolderBean extends AbstractDocumentLazyDataModelBean<Group> {

    private GroupDAOHibernate dao;

    @Inject
    @Named("sessionManagement")
    private transient SessionManagementBean sessionManagement;

    @PostConstruct
    public void init() {
        dao = sessionManagement.getDAO(GroupDAOHibernate.class, GROUP_DAO);
        setLazyModel(new LazyDataModelForGroup(dao));
    }
}
