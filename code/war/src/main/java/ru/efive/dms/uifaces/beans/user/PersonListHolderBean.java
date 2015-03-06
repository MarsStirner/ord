package ru.efive.dms.uifaces.beans.user;

import org.apache.commons.lang.StringUtils;
import org.primefaces.context.RequestContext;
import org.primefaces.model.LazyDataModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.efive.dms.uifaces.beans.IndexManagementBean;
import ru.efive.sql.dao.user.UserDAOHibernate;
import ru.entity.model.user.User;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.Map;

import static ru.efive.dms.util.ApplicationDAONames.USER_DAO;

/**
 * Author: Upatov Egor <br>
 * Date: 19.11.2014, 16:30 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
@ManagedBean(name="personList")
@ViewScoped
public class PersonListHolderBean implements Serializable {
    private static final Logger logger = LoggerFactory.getLogger("DIALOG");

    @EJB(name = "indexManagement")
    private IndexManagementBean indexManagementBean;

    private LazyDataModelForUser lazyModel;

    private User selectedUser;

    private String title = "Выберите пользователя";

    @PostConstruct
    public void init(){
        logger.info("Initialize new PersonSelectDialog");
        final UserDAOHibernate userDao = (UserDAOHibernate) indexManagementBean.getContext().getBean(USER_DAO);
        final Map<String, String> requestParameterMap = FacesContext.getCurrentInstance().getExternalContext()
                .getRequestParameterMap();
        logger.debug("With requestParams = {}", requestParameterMap);
        final String personId = requestParameterMap.get("PERSON_ID");
        if(StringUtils.isNotEmpty(personId)){
            try{
                final User preselected = userDao.get(Integer.valueOf(personId));
                if(preselected != null){
                    setSelectedUser(preselected);
                }
            } catch (NumberFormatException e){
                logger.error("CANT parse \'{}\' to Integer", personId);
            } catch (Exception e){
                logger.error("CANT findPerson with id=\'{}\'", personId);
            }
        }
        final String title = requestParameterMap.get("TITLE");
        if(StringUtils.isNotEmpty(title)){
            if("CONTROLLER_TITLE".equalsIgnoreCase(title)) {
                setTitle("Выберите руководителя");
            } else  if("AUTHOR_TITLE".equalsIgnoreCase(title)) {
                setTitle("Выберите автора");
            }
        }
        lazyModel = new LazyDataModelForUser(userDao);
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public User getSelectedUser() {
        return selectedUser;
    }

    public void setSelectedUser(User selectedUser) {
        this.selectedUser = selectedUser;
    }

    public void closeDialog(boolean withResult){
        RequestContext.getCurrentInstance().closeDialog(withResult ? selectedUser : null);
    }
}