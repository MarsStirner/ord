package ru.efive.dms.uifaces.beans.dialogs;


import com.github.javaplugs.jsf.SpringScopeView;
import org.primefaces.model.LazyDataModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.lazyDataModel.referencebook.ContragentLazyDataModel;
import ru.entity.model.referenceBook.Contragent;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import org.springframework.stereotype.Controller;
import java.util.Map;

/**
 * Author: Upatov Egor <br>
 * Date: 19.11.2014, 16:30 <br>
 * Company: Korus Consulting IT <br>
 * Description: Бин, который обслуживает диалоги по выбору контраегентов<br>
 */
@Controller("contragentDialog")
@SpringScopeView
public class ContragentDialogHolder extends AbstractDialog<Contragent> {


    public static final String DIALOG_SESSION_KEY = "DIALOG_CONTRAGENT";

    @Autowired
    @Qualifier("contragentLDM")
    private ContragentLazyDataModel lazyModel;


    @PostConstruct
    public void init() {
        logger.info("Initialize new ContragentSelectDialog");
        final Map<String, String> requestParameterMap = FacesContext.getCurrentInstance().getExternalContext()
                .getRequestParameterMap();
        logger.debug("With requestParams = {}", requestParameterMap);
        initializePreSelected();
        setTitle(initializeTitle(requestParameterMap));
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
        final Contragent preselected = (Contragent) getFromExternalContext(DIALOG_SESSION_KEY);
        if (preselected != null) {
            setSelected(preselected);
        }
    }
}