package ru.efive.dms.uifaces.beans.dialogs;

import org.apache.commons.lang.StringUtils;
import org.primefaces.context.RequestContext;
import org.primefaces.model.LazyDataModel;
import ru.efive.dms.uifaces.beans.IndexManagementBean;
import ru.efive.dms.uifaces.lazyDataModel.LazyDataModelForUserInDialogs;
import ru.efive.sql.dao.user.UserDAOHibernate;
import ru.entity.model.user.User;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static ru.efive.dms.util.ApplicationDAONames.USER_DAO;

/**
 * Author: Upatov Egor <br>
 * Date: 19.11.2014, 16:30 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
@ManagedBean(name = "multipleUserDialog")
@ViewScoped
public class MultipleUserDialogHolder extends AbstractDialog<List<User>> implements Serializable {

    public static final String DIALOG_SESSION_KEY = "DIALOG_PERSON_LIST";
    public static final String DIALOG_TITLE_GET_PARAM_KEY = "DIALOG_TITLE";

    public static final String DIALOG_TITLE_VALUE_EXECUTORS = "EXECUTORS_TITLE";
    public static final String DIALOG_TITLE_VALUE_RECIPIENTS = "RECIPIENTS_TITLE";
    public static final String DIALOG_TITLE_VALUE_AUTHOR = "AUTHOR_TITLE";

    @EJB(name = "indexManagement")
    private IndexManagementBean indexManagementBean;

    private LazyDataModelForUserInDialogs lazyModel;

    //TODO fix with class extends after change to JSF 2.2
    private List<User> selection = new ArrayList<User>();


    @PostConstruct
    public void init() {
        logger.info("Initialize new MultiplePersonSelectDialog");
        final UserDAOHibernate userDao = (UserDAOHibernate) indexManagementBean.getContext().getBean(USER_DAO);
        final Map<String, String> requestParameterMap = FacesContext.getCurrentInstance().getExternalContext()
                .getRequestParameterMap();
        logger.debug("With requestParams = {}", requestParameterMap);
        initializePreSelected();
        setTitle(initializeTitle(requestParameterMap));
        lazyModel = new LazyDataModelForUserInDialogs(userDao);
    }

    /**
     * Установить шапку по переданному ключу
     *
     * @param requestParameterMap карта get параметров
     * @return Строка с Заголовком
     */
    @Override
    public String initializeTitle(Map<String, String> requestParameterMap) {
        final String title = requestParameterMap.get(DIALOG_TITLE_GET_PARAM_KEY);
        if (StringUtils.isNotEmpty(title)) {
            if (DIALOG_TITLE_VALUE_EXECUTORS.equalsIgnoreCase(title)) {
                return "Выберите исполнителей";
            } else if (DIALOG_TITLE_VALUE_RECIPIENTS.equalsIgnoreCase(title)) {
                return "Выберите адресатов";
            } else if(DIALOG_TITLE_VALUE_AUTHOR.equalsIgnoreCase(title)){
                return "Выберите авторов";
            }
        }
        return "Выберите пользователей";
    }

    /**
     * Выбрать заранее заднный список пользователей по ключу сессии
     */
    @Override
    public void initializePreSelected() {
        final List<User> personList = (List<User>) FacesContext.getCurrentInstance().getExternalContext()
                .getSessionMap().get(DIALOG_SESSION_KEY);
        if (personList != null) {
            setSelection(personList);
        }
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove(DIALOG_SESSION_KEY);
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

    /**
     * Закрыть диалог с результатом
     *
     * @param withResult флаг указывающий передавать ли результат работы диалога
     */
    @Override
    public void closeDialog(boolean withResult) {
        RequestContext.getCurrentInstance().closeDialog(withResult ? selection : null);
    }


    public List<User> getSelection() {
        return selection;
    }


    public void setSelection(List<User> selected) {
        this.selection = selected;
    }



}