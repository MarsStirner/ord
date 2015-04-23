package ru.efive.dms.uifaces.beans.dialogs;

import org.apache.commons.lang.StringUtils;
import org.primefaces.context.RequestContext;
import org.primefaces.model.LazyDataModel;
import ru.efive.dms.uifaces.beans.IndexManagementBean;
import ru.efive.dms.uifaces.lazyDataModel.LazyDataModelForGroup;
import ru.efive.sql.dao.user.GroupDAOHibernate;
import ru.entity.model.user.Group;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.*;

import static ru.efive.dms.util.ApplicationDAONames.GROUP_DAO;

/**
 * Author: Upatov Egor <br>
 * Date: 19.11.2014, 16:30 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
@ManagedBean(name = "multipleGroupDialog")
@ViewScoped
public class MultipleGroupDialogHolder extends AbstractDialog<Set<Group>> implements Serializable {

    public static final String DIALOG_SESSION_KEY = "DIALOG_GROUP_LIST";
    public static final String DIALOG_TITLE_GET_PARAM_KEY = "DIALOG_TITLE";

    public static final String DIALOG_TITLE_VALUE_RECIPIENTS = "RECIPIENTS_TITLE";

    @EJB(name = "indexManagement")
    private IndexManagementBean indexManagementBean;

    private LazyDataModelForGroup lazyModel;

    //TODO fix with class extends after change to JSF 2.2
    private List<Group> selection = new ArrayList<Group>();


    @PostConstruct
    public void init() {
        logger.info("Initialize new MultiplePersonSelectDialog");
        final GroupDAOHibernate groupDao = (GroupDAOHibernate) indexManagementBean.getContext().getBean(GROUP_DAO);
        final Map<String, String> requestParameterMap = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        logger.debug("With requestParams = {}", requestParameterMap);
        initializePreSelected();
        setTitle(initializeTitle(requestParameterMap));
        lazyModel = new LazyDataModelForGroup(groupDao);
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
            if (DIALOG_TITLE_VALUE_RECIPIENTS.equalsIgnoreCase(title)) {
                return "Выберите группы - адресаты";
            }
        }
        return "Выберите группы";
    }

    /**
     * Закрыть диалог с результатом
     *
     * @param withResult флаг указывающий передавать ли результат работы диалога
     */
    @Override
    public void closeDialog(boolean withResult) {
        if(selection != null && !selection.isEmpty()){
            RequestContext.getCurrentInstance().closeDialog(withResult ? new HashSet<Group>(selection) : null);
        } else {
            RequestContext.getCurrentInstance().closeDialog(null);
        }
    }

    /**
     * Выбрать заранее заднный список пользователей по ключу сессии
     */
    @Override
    public void initializePreSelected() {
        final Set<Group> groupSet = (Set<Group>) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(DIALOG_SESSION_KEY);
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove(DIALOG_SESSION_KEY);
        if (groupSet != null && !groupSet.isEmpty()) {
            setSelection(new ArrayList<Group>(groupSet));
        }
    }

    public LazyDataModel<Group> getLazyModel() {
        return lazyModel;
    }

    public String getFilter() {
        return lazyModel.getFilter();
    }

    public void setFilter(String filter) {
        lazyModel.setFilter(filter);
    }

    public List<Group> getSelection() {
        return selection;
    }


    public void setSelection(List<Group> selected) {
        this.selection = selected;
    }


}