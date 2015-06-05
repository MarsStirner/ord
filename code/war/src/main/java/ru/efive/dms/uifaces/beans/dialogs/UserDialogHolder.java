package ru.efive.dms.uifaces.beans.dialogs;

import org.apache.commons.lang.StringUtils;
import org.primefaces.model.LazyDataModel;
import ru.efive.dms.uifaces.beans.IndexManagementBean;
import ru.efive.dms.uifaces.lazyDataModel.dialogs.LazyDataModelForUserInDialogs;
import ru.entity.model.user.Group;
import ru.entity.model.user.User;
import ru.hitsl.sql.dao.user.GroupDAOHibernate;
import ru.hitsl.sql.dao.user.UserDAOHibernate;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.util.Map;

import static ru.hitsl.sql.dao.util.ApplicationDAONames.GROUP_DAO;
import static ru.hitsl.sql.dao.util.ApplicationDAONames.USER_DAO;

/**
 * Author: Upatov Egor <br>
 * Date: 19.11.2014, 16:30 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
@Named("userDialog")
@ViewScoped
public class UserDialogHolder extends AbstractDialog<User> {

    public static final String DIALOG_SESSION_KEY = "DIALOG_PERSON";
    public static final String DIALOG_TITLE_GET_PARAM_KEY = "DIALOG_TITLE";
    public static final String DIALOG_GROUP_KEY = "DIALOG_GROUP_CODE";

    public static final String DIALOG_TITLE_VALUE_CONTROLLER = "CONTROLLER_TITLE";
    public static final String DIALOG_TITLE_VALUE_AUTHOR = "AUTHOR_TITLE";
    public static final String DIALOG_TITLE_VALUE_RESPONSIBLE = "RESPONSIBLE_TITLE";
    public static final String DIALOG_TITLE_VALUE_PERSON_SUBSTITUTION = "PERSON_SUBSTITUTION_TITLE";
    public static final String DIALOG_TITLE_VALUE_PERSON_SUBSTITUTOR = "PERSON_SUBSTITUTOR_TITLE";

    @EJB(name = "indexManagement")
    private IndexManagementBean indexManagementBean;

    private LazyDataModelForUserInDialogs lazyModel;

    @PostConstruct
    public void init() {
        logger.info("Initialize new UserSelectDialog");
        final UserDAOHibernate userDao = (UserDAOHibernate) indexManagementBean.getContext().getBean(USER_DAO);
        final Map<String, String> requestParameterMap = FacesContext.getCurrentInstance().getExternalContext()
                .getRequestParameterMap();
        logger.debug("With requestParams = {}", requestParameterMap);
        initializePreSelected();
        setTitle(initializeTitle(requestParameterMap));
        lazyModel = new LazyDataModelForUserInDialogs(userDao);
        initializeGroup(requestParameterMap);
    }

    private void initializeGroup(final Map<String, String> requestParameterMap) {
        final String code = requestParameterMap.get(DIALOG_GROUP_KEY);
        if (StringUtils.isNotEmpty(code)) {
            logger.info("UserSelectDialog: initialized with group code=\'{}\'", code);
            final Group group = ((GroupDAOHibernate)indexManagementBean.getContext().getBean(GROUP_DAO)).getByCode(code);
            if(group != null){
                logger.info("UserSelectDialog: initialized with group [{}]", group);
                lazyModel.setFilterGroup(group);
            } else {
                logger.error("UserSelectDialog: group not founded by code= \'{}\'", code);
            }
        }
    }

    /**
     * Установить шапку по переданному ключу
     *
     * @param requestParameterMap карта get параметров
     * @return Строка с Заголовком
     */
    @Override
    public String initializeTitle(final Map<String, String> requestParameterMap) {
        final String title = requestParameterMap.get(DIALOG_TITLE_GET_PARAM_KEY);
        if (StringUtils.isNotEmpty(title)) {
            if (DIALOG_TITLE_VALUE_CONTROLLER.equalsIgnoreCase(title)) {
                return "Выберите руководителя";
            } else if (DIALOG_TITLE_VALUE_AUTHOR.equalsIgnoreCase(title)) {
                return "Выберите автора";
            } else if(DIALOG_TITLE_VALUE_RESPONSIBLE.equalsIgnoreCase(title)){
                return "Выберите ответственного исполнителя";
            } else if(DIALOG_TITLE_VALUE_PERSON_SUBSTITUTION.equalsIgnoreCase(title)){
                return "Выберите замещаемого пользователя";
            } else if(DIALOG_TITLE_VALUE_PERSON_SUBSTITUTOR.equalsIgnoreCase(title)){
                return "Выберите замещающего пользователя";
            }
        }
        return "Выберите пользователя";
    }

    /**
     * Выбрать заренее заданного пользователя из сессии по ключу диалога
     */
    @Override
    public void initializePreSelected() {
        final User preselected = (User) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(DIALOG_SESSION_KEY);
        if (preselected != null) {
            setSelected(preselected);
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
}