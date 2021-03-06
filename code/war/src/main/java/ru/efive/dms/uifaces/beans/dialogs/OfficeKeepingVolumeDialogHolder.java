package ru.efive.dms.uifaces.beans.dialogs;


import org.primefaces.model.LazyDataModel;
import ru.efive.dms.uifaces.beans.IndexManagementBean;
import ru.efive.dms.uifaces.lazyDataModel.LazyDataModelForOfficeKeepingVolume;
import ru.entity.model.document.OfficeKeepingVolume;
import ru.hitsl.sql.dao.OfficeKeepingVolumeDAOImpl;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.util.Map;

import static ru.hitsl.sql.dao.util.ApplicationDAONames.OFFICE_KEEPING_VOLUME_DAO;

/**
 * Author: Upatov Egor <br>
 * Date: 12.03.2015, 17:07 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
@Named("officeKeepingVolumeDialog")
@ViewScoped
public class OfficeKeepingVolumeDialogHolder extends AbstractDialog<OfficeKeepingVolume> {

    public static final String DIALOG_SESSION_KEY = "DIALOG_OFFICE_KEEPING_VOLUME";

    @EJB(name = "indexManagement")
    private IndexManagementBean indexManagementBean;

    private LazyDataModelForOfficeKeepingVolume lazyModel;


    @PostConstruct
    public void init() {
        logger.info("Initialize new officeKeepingVolumeSelectDialog");
        final OfficeKeepingVolumeDAOImpl dao = (OfficeKeepingVolumeDAOImpl) indexManagementBean.getContext().getBean
                (OFFICE_KEEPING_VOLUME_DAO);
        final Map<String, String> requestParameterMap = FacesContext.getCurrentInstance().getExternalContext()
                .getRequestParameterMap();
        logger.debug("With requestParams = {}", requestParameterMap);
        initializePreSelected();
        setTitle(initializeTitle(requestParameterMap));
        lazyModel = new LazyDataModelForOfficeKeepingVolume(dao);
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
