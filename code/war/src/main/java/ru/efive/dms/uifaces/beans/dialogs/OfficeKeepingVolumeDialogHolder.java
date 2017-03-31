package ru.efive.dms.uifaces.beans.dialogs;


import com.github.javaplugs.jsf.SpringScopeView;
import org.primefaces.model.LazyDataModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.lazyDataModel.LazyDataModelForOfficeKeepingVolume;
import ru.entity.model.document.OfficeKeepingVolume;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import org.springframework.stereotype.Controller;
import java.util.Map;

/**
 * Author: Upatov Egor <br>
 * Date: 12.03.2015, 17:07 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
@Controller("officeKeepingVolumeDialog")
@SpringScopeView
public class OfficeKeepingVolumeDialogHolder extends AbstractDialog<OfficeKeepingVolume> {

    public static final String DIALOG_SESSION_KEY = "DIALOG_OFFICE_KEEPING_VOLUME";



    @Autowired
    @Qualifier("officeKeepingVolumeLDM")
    private LazyDataModelForOfficeKeepingVolume lazyModel;


    @PostConstruct
    public void init() {
        logger.info("Initialize new officeKeepingVolumeSelectDialog");
        final Map<String, String> requestParameterMap = FacesContext.getCurrentInstance().getExternalContext()
                .getRequestParameterMap();
        logger.debug("With requestParams = {}", requestParameterMap);
        initializePreSelected();
        setTitle(initializeTitle(requestParameterMap));
    }

    public LazyDataModel<OfficeKeepingVolume> getLazyModel() {
        return lazyModel;
    }

    public String getFilter() {
        return lazyModel.getFilter();
    }

    public void setFilter(String filter) {
        lazyModel.setFilter(filter);
    }

    /**
     * Установить шапку по переданному ключу
     *
     * @param requestParameterMap карта get параметров
     * @return Строка с Заголовком
     */
    @Override
    public String initializeTitle(Map<String, String> requestParameterMap) {
        return "Выберите том дела";
    }

    /**
     * Установить заранее заданные значения
     */
    @Override
    public void initializePreSelected() {
        final OfficeKeepingVolume preselected = (OfficeKeepingVolume) getFromExternalContext(DIALOG_SESSION_KEY);
        if (preselected != null) {
            setSelected(preselected);
        }
    }
}
