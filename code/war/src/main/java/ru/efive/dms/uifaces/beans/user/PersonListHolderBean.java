package ru.efive.dms.uifaces.beans.user;

import org.primefaces.model.LazyDataModel;
import ru.efive.dms.uifaces.beans.IndexManagementBean;
import ru.efive.sql.dao.user.UserDAOHibernate;
import ru.entity.model.user.User;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import java.io.Serializable;

import static ru.efive.dms.util.ApplicationDAONames.USER_DAO;

/**
 * Author: Upatov Egor <br>
 * Date: 19.11.2014, 16:30 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
@Named("personList")
@SessionScoped
public class PersonListHolderBean implements Serializable {

    @EJB(name = "indexManagement")
    private IndexManagementBean indexManagementBean;

    private LazyDataModelForUser lazyModel;

    @PostConstruct
    public void init(){
        lazyModel = new LazyDataModelForUser((UserDAOHibernate) indexManagementBean.getContext().getBean(USER_DAO));
    }

    public LazyDataModel<User> getLazyModel() {
        return lazyModel;
    }

    public String getFilter() {
        return lazyModel.getFilter();
    }

    public void setFilter(String filter) {
         lazyModel.setFilter(filter);
    }

}