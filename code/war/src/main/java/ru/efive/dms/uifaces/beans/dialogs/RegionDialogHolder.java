package ru.efive.dms.uifaces.beans.dialogs;

import org.primefaces.model.LazyDataModel;
import ru.efive.dms.dao.RegionDAOImpl;
import ru.efive.dms.uifaces.beans.IndexManagementBean;
import ru.efive.dms.uifaces.lazyDataModel.LazyDataModelForRegion;
import ru.entity.model.document.Region;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.Map;

import static ru.efive.dms.util.ApplicationDAONames.REGION_DAO;

@ManagedBean(name = "regionDialog")
@ViewScoped
public class RegionDialogHolder extends AbstractDialog<Region> implements Serializable {

    public static final String DIALOG_SESSION_KEY = "DIALOG_REGION";


    @EJB(name = "indexManagement")
    private IndexManagementBean indexManagementBean;

    private LazyDataModelForRegion lazyModel;

    @PostConstruct
    public void init() {
        logger.info("Initialize new UserSelectDialog");
        final RegionDAOImpl dao = (RegionDAOImpl) indexManagementBean.getContext().getBean(REGION_DAO);
        final Map<String, String> requestParameterMap = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        logger.debug("With requestParams = {}", requestParameterMap);
        initializePreSelected();
        setTitle(initializeTitle(requestParameterMap));
        lazyModel = new LazyDataModelForRegion(dao);
    }

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
        final Region preselected = (Region) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(DIALOG_SESSION_KEY);
        if (preselected != null) {
            setSelected(preselected);
        }
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove(DIALOG_SESSION_KEY);
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