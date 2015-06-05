package ru.efive.dms.uifaces.beans.dialogs;

import org.apache.commons.lang.StringUtils;
import org.primefaces.context.RequestContext;
import org.primefaces.model.LazyDataModel;
import ru.efive.dms.uifaces.beans.IndexManagementBean;
import ru.efive.dms.uifaces.lazyDataModel.LazyDataModelForGroup;
import ru.entity.model.user.Group;
import ru.hitsl.sql.dao.user.GroupDAOHibernate;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.util.*;

import static ru.hitsl.sql.dao.util.ApplicationDAONames.GROUP_DAO;

/**
 * Author: Upatov Egor <br>
 * Date: 19.11.2014, 16:30 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
@Named("multipleGroupDialog")
@ViewScoped
public class MultipleGroupDialogHolder extends AbstractDialog<List<Group>> {

    public static final String DIALOG_SESSION_KEY = "DIALOG_GROUP_LIST";
    public static final String DIALOG_TITLE_GET_PARAM_KEY = "DIALOG_TITLE";

    public static final String DIALOG_TITLE_VALUE_RECIPIENTS = "RECIPIENTS_TITLE";

    @EJB(name = "indexManagement")
    private IndexManagementBean indexManagementBean;

    private LazyDataModelForGroup lazyModel;


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
     */
    @Override
    public void confirmSelection() {
        final DialogResult result;
        if(selected != null && !selected.isEmpty()) {
           result= new DialogResult(Button.CONFIRM, new HashSet<Group>(selected));
        } else {
            result = new DialogResult(Button.CONFIRM, null);
        }
        logger.debug("DIALOG_BTN_CONFIRM:  {}", result);
        RequestContext.getCurrentInstance().closeDialog(result);
    }

    /**
     * Выбрать заранее заднный список пользователей по ключу сессии
     */
    @Override
    public void initializePreSelected() {
        final Set<Group> groupSet = (Set<Group>) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(DIALOG_SESSION_KEY);
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove(DIALOG_SESSION_KEY);
        if (groupSet != null && !groupSet.isEmpty()) {
            setSelected(new ArrayList<Group>(groupSet));
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


}