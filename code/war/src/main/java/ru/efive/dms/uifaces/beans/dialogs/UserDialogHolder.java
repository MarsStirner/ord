package ru.efive.dms.uifaces.beans.dialogs;

import org.apache.commons.lang3.StringUtils;
import org.primefaces.model.LazyDataModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.annotations.ViewScopedController;
import ru.entity.model.referenceBook.Group;
import ru.entity.model.user.User;
import ru.hitsl.sql.dao.interfaces.referencebook.GroupDao;

import javax.faces.context.FacesContext;
import java.util.Map;

/**
 * Author: Upatov Egor <br>
 * Date: 19.11.2014, 16:30 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
@ViewScopedController("userDialog")
public class UserDialogHolder extends AbstractDialog<User> {

    public static final String DIALOG_SESSION_KEY = "DIALOG_PERSON";
    public static final String DIALOG_TITLE_GET_PARAM_KEY = "DIALOG_TITLE";
    public static final String DIALOG_GROUP_KEY = "DIALOG_GROUP_CODE";

    public static final String DIALOG_TITLE_VALUE_CONTROLLER = "CONTROLLER_TITLE";
    public static final String DIALOG_TITLE_VALUE_AUTHOR = "AUTHOR_TITLE";
    public static final String DIALOG_TITLE_VALUE_RESPONSIBLE = "RESPONSIBLE_TITLE";
    public static final String DIALOG_TITLE_VALUE_PERSON_SUBSTITUTION = "PERSON_SUBSTITUTION_TITLE";
    public static final String DIALOG_TITLE_VALUE_PERSON_SUBSTITUTOR = "PERSON_SUBSTITUTOR_TITLE";


    @Autowired
    @Qualifier("userDialogLDM")
    private UserInDialogLazyDataModel lazyModel;


    public void init() {
        logger.info("Initialize new UserSelectDialog");
        final Map<String, String> requestParameterMap = FacesContext.getCurrentInstance().getExternalContext()
                .getRequestParameterMap();
        logger.debug("With requestParams = {}", requestParameterMap);
        initializePreSelected();
        setTitle(initializeTitle(requestParameterMap));
        lazyModel.initializeGroup(requestParameterMap.get(DIALOG_GROUP_KEY));
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
            } else if (DIALOG_TITLE_VALUE_RESPONSIBLE.equalsIgnoreCase(title)) {
                return "Выберите ответственного исполнителя";
            } else if (DIALOG_TITLE_VALUE_PERSON_SUBSTITUTION.equalsIgnoreCase(title)) {
                return "Выберите замещаемого пользователя";
            } else if (DIALOG_TITLE_VALUE_PERSON_SUBSTITUTOR.equalsIgnoreCase(title)) {
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
        final User preselected = (User) getFromExternalContext(DIALOG_SESSION_KEY);
        if (preselected != null) {
            setSelected(preselected);
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