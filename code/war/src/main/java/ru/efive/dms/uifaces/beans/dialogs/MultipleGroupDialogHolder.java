package ru.efive.dms.uifaces.beans.dialogs;

import com.github.javaplugs.jsf.SpringScopeView;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.context.RequestContext;
import org.primefaces.model.LazyDataModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.lazyDataModel.referencebook.GroupLazyDataModel;
import ru.entity.model.referenceBook.Group;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import org.springframework.stereotype.Controller;
import java.util.*;

/**
 * Author: Upatov Egor <br>
 * Date: 19.11.2014, 16:30 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
@Controller("multipleGroupDialog")
@SpringScopeView
public class MultipleGroupDialogHolder extends AbstractDialog<List<Group>> {

    public static final String DIALOG_SESSION_KEY = "DIALOG_GROUP_LIST";
    public static final String DIALOG_TITLE_GET_PARAM_KEY = "DIALOG_TITLE";

    public static final String DIALOG_TITLE_VALUE_RECIPIENTS = "RECIPIENTS_TITLE";

    @Autowired
    @Qualifier("groupLDM")
    private GroupLazyDataModel lazyModel;


    @PostConstruct
    public void init() {
        logger.info("Initialize new MultiplePersonSelectDialog");
        final Map<String, String> requestParameterMap = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        logger.debug("With requestParams = {}", requestParameterMap);
        initializePreSelected();
        setTitle(initializeTitle(requestParameterMap));
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
        if (selected != null && !selected.isEmpty()) {
            result = new DialogResult(Button.CONFIRM, new HashSet<>(selected));
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
        final Set<Group> groupSet = (Set<Group>) getFromExternalContext(DIALOG_SESSION_KEY);
        if (groupSet != null && !groupSet.isEmpty()) {
            setSelected(new ArrayList<>(groupSet));
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