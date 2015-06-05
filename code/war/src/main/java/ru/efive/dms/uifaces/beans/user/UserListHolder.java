package ru.efive.dms.uifaces.beans.user;

import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentLazyDataModelBean;
import ru.efive.dms.uifaces.lazyDataModel.LazyDataModelForUser;
import ru.entity.model.user.User;
import ru.hitsl.sql.dao.user.UserDAOHibernate;
import ru.hitsl.sql.dao.util.ApplicationDAONames;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * Author: Upatov Egor <br>
 * Date: 16.04.2015, 15:13 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
@Named("users")
@ViewScoped
public class UserListHolder  extends AbstractDocumentLazyDataModelBean<User> {

    private UserDAOHibernate dao;

    @Inject
    @Named("sessionManagement")
    private transient SessionManagementBean sessionManagement;

    @PostConstruct
    public void init() {
        dao = sessionManagement.getDAO(UserDAOHibernate.class, ApplicationDAONames.USER_DAO);
        setLazyModel(new LazyDataModelForUser(dao));
    }

    public void setShowFired(boolean showFired) {
        ((LazyDataModelForUser)getLazyModel()).setShowFired(showFired);
        getLazyModel().setFilter(null);
    }

    public boolean getShowFired() {
        return  ((LazyDataModelForUser)getLazyModel()).isShowFired();
    }

}