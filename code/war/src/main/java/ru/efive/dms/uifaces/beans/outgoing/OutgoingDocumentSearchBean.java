package ru.efive.dms.uifaces.beans.outgoing;

import com.google.common.collect.ImmutableList;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentSearchBean;
import ru.efive.dms.uifaces.beans.dialogs.AbstractDialog;
import ru.efive.dms.uifaces.beans.dialogs.ContragentDialogHolder;
import ru.efive.dms.uifaces.beans.dialogs.MultipleUserDialogHolder;
import ru.efive.dms.uifaces.beans.dialogs.UserDialogHolder;
import ru.efive.dms.uifaces.lazyDataModel.documents.LazyDataModelForOutgoingDocument;
import ru.entity.model.document.OutgoingDocument;
import ru.entity.model.referenceBook.Contragent;
import ru.entity.model.referenceBook.DeliveryType;
import ru.entity.model.user.Group;
import ru.entity.model.user.User;
import ru.hitsl.sql.dao.OutgoingDocumentDAOImpl;
import ru.hitsl.sql.dao.ViewFactDaoImpl;

import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.efive.dms.uifaces.beans.utils.MessageHolder.MSG_CANT_DO_SEARCH;
import static ru.hitsl.sql.dao.util.ApplicationDAONames.OUTGOING_DOCUMENT_FORM_DAO;
import static ru.hitsl.sql.dao.util.ApplicationDAONames.VIEW_FACT_DAO;
import static ru.hitsl.sql.dao.util.DocumentSearchMapKeys.*;

@Named("outgoing_search")
@ViewScoped
public class OutgoingDocumentSearchBean extends AbstractDocumentSearchBean<OutgoingDocument> {
    private static final Logger logger = LoggerFactory.getLogger("SEARCH");

    @Inject
    @Named("sessionManagement")
    private transient SessionManagementBean sessionManagement;

    /**
     * Выполнить поиск с текущим фильтром
     *
     * @return Список документов, удовлетворяющих поиску
     * */
    @Override
    public void performSearch() {
        logger.info("OUTGOING: Perform Search with map : {}", filters);
        try {
            final OutgoingDocumentDAOImpl dao = sessionManagement.getDAO(OutgoingDocumentDAOImpl.class, OUTGOING_DOCUMENT_FORM_DAO);
            final ViewFactDaoImpl viewFactDao = sessionManagement.getDAO(ViewFactDaoImpl.class, VIEW_FACT_DAO);
            final LazyDataModelForOutgoingDocument lazyDataModelForOutgoingDocument = new LazyDataModelForOutgoingDocument(
                    dao,
                    viewFactDao,
                    sessionManagement.getAuthData()
            );
            lazyDataModelForOutgoingDocument.setFilters(filters);
            setLazyModel(lazyDataModelForOutgoingDocument);
            searchPerformed= true;
        } catch (Exception e) {
            searchPerformed = false;
            logger.error("OUTGOING: Error while search", e);
            FacesContext.getCurrentInstance().addMessage(null, MSG_CANT_DO_SEARCH);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////// Диалоговые окошки  /////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    // Выбора руководителя /////////////////////////////////////////////////////////////////////////////////////////////
    public void chooseController() {
        final Map<String, List<String>> params = new HashMap<String, List<String>>();
        params.put(UserDialogHolder.DIALOG_TITLE_GET_PARAM_KEY, ImmutableList.of(UserDialogHolder.DIALOG_TITLE_VALUE_CONTROLLER));
        params.put(UserDialogHolder.DIALOG_GROUP_KEY, ImmutableList.of(Group.RB_CODE_MANAGERS));
        final User preselected = getController();
        if (preselected != null) {
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(UserDialogHolder
                    .DIALOG_SESSION_KEY, preselected);
        }
        RequestContext.getCurrentInstance().openDialog("/dialogs/selectUserDialog.xhtml", AbstractDialog.getViewParams(), params);
    }

    public void onControllerChosen(SelectEvent event) {
        final AbstractDialog.DialogResult result = (AbstractDialog.DialogResult) event.getObject();
        logger.info("Choose controller: {}", result);
        if(AbstractDialog.Button.CONFIRM.equals(result.getButton())) {
            final User selected = (User) result.getResult();
            if (selected != null) {
                setController(selected);
            } else {
                filters.remove(CONTROLLER_KEY);
            }
        }
    }
    // Выбора контрагента //////////////////////////////////////////////////////////////////////////////////////////////
    public void chooseContragent() {
        final Contragent preselected = getContragent();
        if (preselected != null) {
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(
                    ContragentDialogHolder
                    .DIALOG_SESSION_KEY, preselected);
        }
        RequestContext.getCurrentInstance().openDialog("/dialogs/selectContragentDialog.xhtml", AbstractDialog.getViewParams(), null);
    }

    public void onContragentChosen(SelectEvent event) {
        final AbstractDialog.DialogResult result = (AbstractDialog.DialogResult) event.getObject();
        logger.info("Choose contragent: {}", result);
        if(AbstractDialog.Button.CONFIRM.equals(result.getButton())) {
            final Contragent selected = (Contragent) result.getResult();
            if (selected != null) {
                setContragent(selected);
            } else {
                filters.remove(CONTRAGENT_KEY);
            }
        }
    }

    // Выбора исполнителей /////////////////////////////////////////////////////////////////////////////////////////////
    public void chooseExecutors() {
        final Map<String, List<String>> params = new HashMap<String, List<String>>();
        params.put(MultipleUserDialogHolder.DIALOG_TITLE_GET_PARAM_KEY, ImmutableList.of(MultipleUserDialogHolder
                .DIALOG_TITLE_VALUE_EXECUTORS));
        final List<User> preselected = getExecutors();
        if (preselected != null && !preselected.isEmpty()) {
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(MultipleUserDialogHolder
                    .DIALOG_SESSION_KEY, preselected);
        }
        RequestContext.getCurrentInstance().openDialog("/dialogs/selectMultipleUserDialog.xhtml", AbstractDialog.getViewParams(), params);
    }

    public void onExecutorsChosen(SelectEvent event) {
        final AbstractDialog.DialogResult result = (AbstractDialog.DialogResult) event.getObject();
        logger.info("Choose executors: {}", result);
        if(AbstractDialog.Button.CONFIRM.equals(result.getButton())) {
            final List<User> selected = (List<User>) result.getResult();
            if (selected != null && !selected.isEmpty()) {
                setExecutors(selected);
            } else {
                filters.remove(EXECUTORS_KEY);
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Параметры поиска ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // Руководитель
    public void setController(final User value) {
        putNotNullToFilters(CONTROLLER_KEY, value);
    }

    public User getController() {
        return (User) filters.get(CONTROLLER_KEY);
    }

    // Дата подписания ОТ
    public void setStartSignatureDate(Date value) {
        putNotNullToFilters(START_SIGNATURE_DATE_KEY, value);
    }

    public Date getStartSignatureDate() {
        return (Date) filters.get(START_SIGNATURE_DATE_KEY);
    }

    // Дата подписания ДО
    public void setEndSignatureDate(Date value) {
        putNotNullToFilters(END_SIGNATURE_DATE_KEY, value);
    }

    public Date getEndSignatureDate() {
        return (Date) filters.get(END_SIGNATURE_DATE_KEY);
    }
//
//    //Дата отправки ОТ
//    public void setStartSendingDate(Date value) {
//        putNotNullToFilters(START_SENDING_DATE_KEY, value);
//    }
//
//    public Date getStartSendingDate() {
//        return (Date) filters.get(START_SENDING_DATE_KEY);
//    }
//
//    //Дата отправки ДО
//    public void setEndSendingDate(Date value) {
//        putNotNullToFilters(END_SENDING_DATE_KEY, value);
//    }
//
//    public Date getEndSendingDate() {
//        return (Date) filters.get(END_SENDING_DATE_KEY);
//    }

    // Тип доставки
    public void setDeliveryType(DeliveryType value) {
        putNotNullToFilters(DELIVERY_TYPE_KEY, value);
    }

    public DeliveryType getDeliveryType() {
        return (DeliveryType) filters.get(DELIVERY_TYPE_KEY);
    }

    // Контрагент (адресат)
    public void setContragent(Contragent value) {
        putNotNullToFilters(CONTRAGENT_KEY, value);
    }

    public Contragent getContragent() {
        return (Contragent) filters.get(CONTRAGENT_KEY);
    }

    // Исполнители
    public void setExecutors(List<User> value) {
        putNotNullToFilters(EXECUTORS_KEY, value);
    }

    public List<User> getExecutors() {
        return (List<User>) filters.get(EXECUTORS_KEY);
    }

    public void removeExecutor(User executor) {
        final List<User> executors = getExecutors();
        executors.remove(executor);
        if(executors.isEmpty()){
            filters.remove(EXECUTORS_KEY);
        }
    }

}