package ru.efive.dms.uifaces.beans.dialogs;

import com.github.javaplugs.jsf.SpringScopeView;
import org.primefaces.model.LazyDataModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.lazyDataModel.LazyDataModelForRegion;
import ru.entity.model.referenceBook.Region;

import org.springframework.stereotype.Controller;
import java.util.Map;

@Controller("regionDialog")
@SpringScopeView
public class RegionDialogHolder extends AbstractDialog<Region> {

    public static final String DIALOG_SESSION_KEY = "DIALOG_REGION";

    @Autowired
    @Qualifier("regionLDM")
    private LazyDataModelForRegion lazyModel;

    /**
     * Установить шапку по переданному ключу
     *
     * @param requestParameterMap карта get параметров
     * @return Строка с Заголовком
     */
    @Override
    public String initializeTitle(final Map<String, String> requestParameterMap) {
        return "Выберите регион";
    }

    /**
     * Выбрать заренее заданного пользователя из сессии по ключу диалога
     */
    @Override
    public void initializePreSelected() {
        final Region preselected = (Region) getFromExternalContext(DIALOG_SESSION_KEY);
        if (preselected != null) {
            setSelected(preselected);
        }
    }

    public LazyDataModel<Region> getLazyModel() {
        return lazyModel;
    }

    public String getFilter() {
        return lazyModel.getFilter();
    }

    public void setFilter(String filter) {
        lazyModel.setFilter(filter);
    }
}