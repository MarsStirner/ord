package ru.efive.dms.uifaces.beans.dialogs;


import org.primefaces.model.LazyDataModel;
import ru.efive.crm.dao.ContragentDAOHibernate;
import ru.efive.dms.uifaces.beans.IndexManagementBean;
import ru.efive.dms.uifaces.lazyDataModel.LazyDataModelForContragent;
import ru.entity.model.crm.Contragent;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.util.Map;

import static ru.efive.dms.util.ApplicationDAONames.CONTRAGENT_DAO;

/**
 * Author: Upatov Egor <br>
 * Date: 19.11.2014, 16:30 <br>
 * Company: Korus Consulting IT <br>
 * Description: Бин, который обслуживает диалоги по выбору контраегентов<br>
 */
@Named("contragentDialog")
@ViewScoped
public class ContragentDialogHolder extends AbstractDialog<Contragent> {


    public static final String DIALOG_SESSION_KEY = "DIALOG_CONTRAGENT";

    @EJB(name = "indexManagement")
    private IndexManagementBean indexManagementBean;

    private LazyDataModelForContragent lazyModel;


    @PostConstruct
    public void init() {
        logger.info("Initialize new ContragentSelectDialog");
        final ContragentDAOHibernate dao = (ContragentDAOHibernate) indexManagementBean.getContext().getBean
                (CONTRAGENT_DAO);
        final Map<String, String> requestParameterMap = FacesContext.getCurrentInstance().getExternalContext()
                .getRequestParameterMap();
        logger.debug("With requestParams = {}", requestParameterMap);
        initializePreSelected();
        setTitle(initializeTitle(requestParameterMap));
        lazyModel = new LazyDataModelForContragent(dao);
    }

    public LazyDataModel<Contragent> getLazyModel() {
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
        return "Выберите корреспондента";
    }

    /**
     * Установить заранее заданные значения
     */
    @Override
    public void initializePreSelected() {
        final Contragent preselected = (Contragent) FacesContext.getCurrentInstance().getExternalContext()
                .getSessionMap().get(DIALOG_SESSION_KEY);
        if (preselected != null) {
            setSelected(preselected);
        }
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove(DIALOG_SESSION_KEY);
    }
}