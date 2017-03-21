package ru.efive.dms.uifaces.beans.dialogs;

import org.apache.commons.lang3.StringUtils;
import org.primefaces.model.LazyDataModel;
import ru.efive.dms.uifaces.beans.IndexManagementBean;
import ru.efive.dms.uifaces.lazyDataModel.dialogs.LazyDataModelForUserInDialogs;
import ru.entity.model.user.User;
import ru.hitsl.sql.dao.user.UserDAOHibernate;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.util.List;
import java.util.Map;

import static ru.hitsl.sql.dao.util.ApplicationDAONames.USER_DAO;

/**
 * Author: Upatov Egor <br>
 * Date: 19.11.2014, 16:30 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
@Named("multipleUserDialog")
@ViewScoped
public class MultipleUserDialogHolder extends AbstractDialog<List<User>> {

    public static final String DIALOG_SESSION_KEY = "DIALOG_PERSON_LIST";
    public static final String DIALOG_TITLE_GET_PARAM_KEY = "DIALOG_TITLE";

    public static final String DIALOG_TITLE_VALUE_EXECUTORS = "EXECUTORS_TITLE";
    public static final String DIALOG_TITLE_VALUE_RECIPIENTS = "RECIPIENTS_TITLE";
    public static final String DIALOG_TITLE_VALUE_AUTHOR = "AUTHOR_TITLE";
    public static final String DIALOG_TITLE_VALUE_PERSON_READERS = "PERSON_READERS_TITLE";
    public static final String DIALOG_TITLE_VALUE_PERSON_EDITORS = "PERSON_EDITORS_TITLE";

    @EJB(name = "indexManagement")
    private IndexManagementBean indexManagementBean;

    private LazyDataModelForUserInDialogs lazyModel;



    @PostConstruct
    public void init() {
        logger.info("Initialize new MultiplePersonSelectDialog");
        final UserDAOHibernate userDao = (UserDAOHibernate) indexManagementBean.getContext().getBean(USER_DAO);
        final Map<String, String> requestParameterMap = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
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
            } else if (DIALOG_TITLE_VALUE_AUTHOR.equalsIgnoreCase(title)) {
                return "Выберите авторов";
            } else if (DIALOG_TITLE_VALUE_PERSON_READERS.equalsIgnoreCase(title)) {
                return "Выберите пользователей-читателей";
            } else if (DIALOG_TITLE_VALUE_PERSON_EDITORS.equalsIgnoreCase(title)) {
                return "Выберите пользователей-редакторов";
            }
        }
        return "Выберите пользователей";
    }

    /**
     * Выбрать заранее заднный список пользователей по ключу сессии
     */
    @Override
    public void initializePreSelected() {
        final List<User> personList = (List<User>) getFromExternalContext(DIALOG_SESSION_KEY);
        if (personList != null) {
            setSelected(personList);
        }
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