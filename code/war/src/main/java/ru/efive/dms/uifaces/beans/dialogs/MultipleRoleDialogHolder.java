package ru.efive.dms.uifaces.beans.dialogs;

import org.apache.commons.lang.StringUtils;
import org.primefaces.context.RequestContext;
import org.primefaces.model.LazyDataModel;
import ru.efive.dms.uifaces.beans.IndexManagementBean;
import ru.efive.dms.uifaces.lazyDataModel.LazyDataModelForRole;
import ru.efive.sql.dao.user.RoleDAOHibernate;
import ru.entity.model.user.Role;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.util.*;

import static ru.efive.dms.util.ApplicationDAONames.ROLE_DAO;

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

    //TODO fix with class extends after change to JSF 2.2
    private List<Role> selection = new ArrayList<Role>();


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
     * Закрыть диалог с результатом
     *
     * @param withResult флаг указывающий передавать ли результат работы диалога
     */
    @Override
    public void closeDialog(boolean withResult) {
        if(selection != null && !selection.isEmpty()){
            RequestContext.getCurrentInstance().closeDialog(withResult ? new HashSet<Role>(selection): null);
        } else {
            RequestContext.getCurrentInstance().closeDialog(null);
        }
    }

    /**
     * Выбрать заранее заднный список пользователей по ключу сессии
     */
    @Override
    public void initializePreSelected() {
        final Set<Role> preselected = (Set<Role>) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(DIALOG_SESSION_KEY);
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove(DIALOG_SESSION_KEY);
        if (preselected != null && !preselected.isEmpty()) {
            setSelection(new ArrayList<Role>(preselected));
        }
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

    public List<Role> getSelection() {
        return selection;
    }


    public void setSelection(List<Role> selected) {
        this.selection = selected;
    }


}