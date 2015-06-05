package ru.efive.dms.uifaces.beans.dialogs;

import org.apache.commons.lang.StringUtils;
import org.primefaces.context.RequestContext;
import org.primefaces.model.LazyDataModel;
import ru.efive.dms.uifaces.beans.IndexManagementBean;
import ru.efive.dms.uifaces.lazyDataModel.LazyDataModelForRole;
import ru.entity.model.user.Role;
import ru.hitsl.sql.dao.user.RoleDAOHibernate;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.util.*;

import static ru.hitsl.sql.dao.util.ApplicationDAONames.ROLE_DAO;

/**
 * Author: Upatov Egor <br>
 * Date: 19.11.2014, 16:30 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
@Named("multipleRoleDialog")
@ViewScoped
public class MultipleRoleDialogHolder extends AbstractDialog<List<Role>> {

    public static final String DIALOG_SESSION_KEY = "DIALOG_ROLE_LIST";
    public static final String DIALOG_TITLE_GET_PARAM_KEY = "DIALOG_TITLE";

    public static final String DIALOG_TITLE_VALUE_READERS = "READERS_TITLE";
    public static final String DIALOG_TITLE_VALUE_EDITORS = "EDITORS_TITLE";

    @EJB(name = "indexManagement")
    private IndexManagementBean indexManagementBean;

    private LazyDataModelForRole lazyModel;


    @PostConstruct
    public void init() {
        logger.info("Initialize new MultiplePersonSelectDialog");
        final RoleDAOHibernate groupDao = (RoleDAOHibernate) indexManagementBean.getContext().getBean(ROLE_DAO);
        final Map<String, String> requestParameterMap = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        logger.debug("With requestParams = {}", requestParameterMap);
        initializePreSelected();
        setTitle(initializeTitle(requestParameterMap));
        lazyModel = new LazyDataModelForRole(groupDao);
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
            if (DIALOG_TITLE_VALUE_READERS.equalsIgnoreCase(title)) {
                return "Выберите роли - читатели";
            } else if (DIALOG_TITLE_VALUE_EDITORS.equalsIgnoreCase(title)){
                return "Выберите роли - редакторы";
            }
        }
        return "Выберите роли";
    }


    /**
     * Выбрать заранее заднный список пользователей по ключу сессии
     */
    @Override
    public void initializePreSelected() {
        final Set<Role> preselected = (Set<Role>) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(DIALOG_SESSION_KEY);
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove(DIALOG_SESSION_KEY);
        if (preselected != null && !preselected.isEmpty()) {
            setSelected(new ArrayList<Role>(preselected));
        }
    }

    /**
     * Закрыть диалог с результатом
     */
    @Override
    public void confirmSelection() {
        final DialogResult result;
        if(selected != null && !selected.isEmpty()) {
            result= new DialogResult(Button.CONFIRM, new HashSet<Role>(selected));
        } else {
            result = new DialogResult(Button.CONFIRM, null);
        }
        logger.debug("DIALOG_BTN_CONFIRM:  {}", result);
        RequestContext.getCurrentInstance().closeDialog(result);
    }

    public LazyDataModel<Role> getLazyModel() {
        return lazyModel;
    }

    public String getFilter() {
        return lazyModel.getFilter();
    }

    public void setFilter(String filter) {
        lazyModel.setFilter(filter);
    }

}