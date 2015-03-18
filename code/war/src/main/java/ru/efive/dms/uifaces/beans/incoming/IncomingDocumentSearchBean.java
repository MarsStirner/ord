package ru.efive.dms.uifaces.beans.incoming;

import com.google.common.collect.ImmutableList;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.efive.dms.dao.IncomingDocumentDAOImpl;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentSearchBean;
import ru.efive.dms.uifaces.beans.dialogs.*;
import ru.entity.model.crm.Contragent;
import ru.entity.model.document.DeliveryType;
import ru.entity.model.document.DocumentForm;
import ru.entity.model.document.IncomingDocument;
import ru.entity.model.document.OfficeKeepingVolume;
import ru.entity.model.user.User;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.efive.dms.uifaces.beans.utils.MessageHolder.MSG_CANT_DO_SEARCH;
import static ru.efive.dms.util.ApplicationDAONames.INCOMING_DOCUMENT_FORM_DAO;
import static ru.efive.dms.util.DocumentSearchMapKeys.*;

@ManagedBean(name = "incoming_search")
@ViewScoped
public class IncomingDocumentSearchBean extends AbstractDocumentSearchBean<IncomingDocument> {
    private static final Logger logger = LoggerFactory.getLogger("SEARCH");

    @Inject
    @Named("sessionManagement")
    private transient SessionManagementBean sessionManagement;
    private String removeAuthor;

    /**
     * Выполнить поиск с текущим фильтром
     *
     * @return Список документов, удовлетворяющих поиску
     */
    @Override
    public List<IncomingDocument> performSearch() {
        logger.info("INCOMING: Perform Search with map : {}", filters);
        try {
            searchResults = sessionManagement.getDAO(IncomingDocumentDAOImpl.class, INCOMING_DOCUMENT_FORM_DAO)
                    .findAllDocumentsByUser(filters, null, sessionManagement.getLoggedUser(), false, false);
        } catch (Exception e) {
            logger.error("INCOMING: Error while search", e);
            FacesContext.getCurrentInstance().addMessage(null, MSG_CANT_DO_SEARCH);
        }
        return searchResults;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////// Диалоговые окошки  /////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //Выбора автора ////////////////////////////////////////////////////////////////////////////////////////////////////
    public void chooseAuthors() {
        final Map<String, List<String>> params = new HashMap<String, List<String>>();
        params.put(UserDialogHolder.DIALOG_TITLE_GET_PARAM_KEY, ImmutableList.of(MultipleUserDialogHolder
                .DIALOG_TITLE_VALUE_AUTHOR));
        final List<User> preselected = getAuthors();
        if (preselected != null) {
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(MultipleUserDialogHolder
                    .DIALOG_SESSION_KEY, preselected);
        }
        RequestContext.getCurrentInstance().openDialog("/dialogs/selectMultipleUserDialog.xhtml", AbstractDialog.getViewParams(), params);
    }

    public void onAuthorsChosen(SelectEvent event) {
        final List<User> selected = (List<User>) event.getObject();
        if (selected != null) {
            setAuthors(selected);
        } else {
            filters.remove(AUTHORS_KEY);
        }
        logger.info("Choose authors From Dialog \'{}\'", selected != null ? selected : "#NOTSET");
    }

    // Выбора руководителя /////////////////////////////////////////////////////////////////////////////////////////////
    public void chooseController() {
        final Map<String, List<String>> params = new HashMap<String, List<String>>();
        params.put(UserDialogHolder.DIALOG_TITLE_GET_PARAM_KEY, ImmutableList.of(UserDialogHolder
                .DIALOG_TITLE_VALUE_CONTROLLER));
        final User preselected = getController();
        if (preselected != null) {
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(UserDialogHolder
                    .DIALOG_SESSION_KEY, preselected);
        }
        RequestContext.getCurrentInstance().openDialog("/dialogs/selectUserDialog.xhtml", AbstractDialog.getViewParams(), params);
    }

    public void onControllerChosen(SelectEvent event) {
        final User selected = (User) event.getObject();
        if (selected != null) {
            setController(selected);
        } else {
            filters.remove(CONTROLLER_KEY);
        }
        logger.info("Choose controller From Dialog \'{}\'", selected != null ? selected.getDescription() : "#NOTSET");
    }

    // Выбора контрагента //////////////////////////////////////////////////////////////////////////////////////////////
    public void chooseContragent() {
        final Contragent preselected = getContragent();
        if (preselected != null) {
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ContragentDialogHolder
                    .DIALOG_SESSION_KEY, preselected);
        }
        RequestContext.getCurrentInstance().openDialog("/dialogs/selectContragentDialog.xhtml", AbstractDialog.getViewParams(), null);
    }

    public void onContragentChosen(SelectEvent event) {
        final Contragent selected = (Contragent) event.getObject();
        if (selected != null) {
            setContragent(selected);
        } else {
            filters.remove(CONTRAGENT_KEY);
        }
        logger.info("Choose contragent From Dialog \'{}\'", selected != null ? selected.getDescription() : "#NOTSET");
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
        final List<User> selected = (List<User>) event.getObject();
        if (selected != null && !selected.isEmpty()) {
            setExecutors(selected);
        } else {
            filters.remove(EXECUTORS_KEY);
        }
        logger.info("Choose executors From Dialog \'{}\'", selected != null ? selected : "#NOTSET");
    }

    // Выбора адресатов /////////////////////////////////////////////////////////////////////////////////////////////
    public void chooseRecipients() {
        final Map<String, List<String>> params = new HashMap<String, List<String>>();
        params.put(MultipleUserDialogHolder.DIALOG_TITLE_GET_PARAM_KEY, ImmutableList.of(MultipleUserDialogHolder
                .DIALOG_TITLE_VALUE_RECIPIENTS));
        final List<User> preselected = getRecipients();
        if (preselected != null && !preselected.isEmpty()) {
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(MultipleUserDialogHolder
                    .DIALOG_SESSION_KEY, preselected);
        }
        RequestContext.getCurrentInstance().openDialog("/dialogs/selectMultipleUserDialog.xhtml", AbstractDialog.getViewParams(), params);
    }

    public void onRecipientsChosen(SelectEvent event) {
        final List<User> selected = (List<User>) event.getObject();
        if (selected != null && !selected.isEmpty()) {
            setRecipients(selected);
        } else {
            filters.remove(RECIPIENTS_KEY);
        }
        logger.info("Choose recipients From Dialog \'{}\'", selected != null ? selected : "#NOTSET");
    }

    // Выбора томов дел /////////////////////////////////////////////////////////////////////////////////////////////
    public void chooseOfficeKeepingVolume() {
        final OfficeKeepingVolume preselected = getOfficeKeepingVolume();
        if (preselected != null) {
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put
                    (OfficeKeepingVolumeDialogHolder.DIALOG_SESSION_KEY, preselected);
        }
        RequestContext.getCurrentInstance().openDialog("/dialogs/selectOfficeKeepingVolumeDialog.xhtml", AbstractDialog.getViewParams(), null);
    }

    public void onOfficeKeepingVolumeChosen(SelectEvent event) {
        final OfficeKeepingVolume selected = (OfficeKeepingVolume) event.getObject();
        if (selected != null) {
            setOfficeKeepingVolume(selected);
        } else {
            filters.remove(OFFICE_KEEPING_VOLUME_KEY);
        }
        logger.info("Choose officeKeepingVolume From Dialog \'{}\'", selected != null ? selected : "#NOTSET");
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Параметры поиска ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Статус
    public void setStatus(final String value) {
        putNotNullToFilters(STATUS_KEY, value);
    }

    public String getStatus() {
        return (String) filters.get(STATUS_KEY);
    }

    //  Вид документа
    public void setForm(final DocumentForm value) {
        putNotNullToFilters(FORM_KEY, value);
    }

    public DocumentForm getForm() {
        return (DocumentForm) filters.get(FORM_KEY);
    }

    // Автор
    public void setAuthors(final List<User> value) {
        putNotNullToFilters(AUTHORS_KEY, value);
    }

    public List<User> getAuthors() {
        return (List<User>) filters.get(AUTHORS_KEY);
    }

    public void removeAuthor(User author) {
        final List<User> authors = getAuthors();
        authors.remove(author);
        if(authors.isEmpty()){
            filters.remove(AUTHORS_KEY);
        }
    }

    // Руководитель
    public void setController(final User value) {
        putNotNullToFilters(CONTROLLER_KEY, value);
    }

    public User getController() {
        return (User) filters.get(CONTROLLER_KEY);
    }

    // Регистрационный номер
    public void setRegistrationNumber(final String value) {
        putNotNullToFilters(REGISTRATION_NUMBER_KEY, value);
    }

    public String getRegistrationNumber() {
        return (String) filters.get(REGISTRATION_NUMBER_KEY);
    }
    /*  в форме не используется
    // Номер поступившего
    public void setReceivedDocumentNumber(final String value) {
        putNotNullToFilters(RECEIVED_DOCUMENT_NUMBER_KEY, value);
    }

    public String getReceivedDocumentNumber() {
        return (String) filters.get(RECEIVED_DOCUMENT_NUMBER_KEY);
    }
    */
    // Дата создания ОТ
    public void setStartCreationDate(Date value) {
        putNotNullToFilters(START_CREATION_DATE_KEY, value);
    }

    public Date getStartCreationDate() {
        return (Date) filters.get(START_CREATION_DATE_KEY);
    }

    // Дата создания ДО
    public void setEndCreationDate(Date value) {
        putNotNullToFilters(END_CREATION_DATE_KEY, value);
    }

    public Date getEndCreationDate() {
        return (Date) filters.get(END_CREATION_DATE_KEY);
    }

    // Дата регистрации ОТ
    public void setStartRegistrationDate(Date value) {
        putNotNullToFilters(START_REGISTRATION_DATE_KEY, value);
    }

    public Date getStartRegistrationDate() {
        return (Date) filters.get(START_REGISTRATION_DATE_KEY);
    }

    // Дата регистрации ДО
    public void setEndRegistrationDate(Date value) {
        putNotNullToFilters(END_REGISTRATION_DATE_KEY, value);
    }

    public Date getEndRegistrationDate() {
        return (Date) filters.get(END_REGISTRATION_DATE_KEY);
    }

    // Дата доставки ОТ
    public void setStartDeliveryDate(Date value) {
        putNotNullToFilters(START_DELIVERY_DATE_KEY, value);
    }

    public Date getStartDeliveryDate() {
        return (Date) filters.get(START_DELIVERY_DATE_KEY);
    }

    // Дата доставки ДО
    public void setEndDeliveryDate(Date value) {
        putNotNullToFilters(END_DELIVERY_DATE_KEY, value);
    }

    public Date getEndDeliveryDate() {
        return (Date) filters.get(END_DELIVERY_DATE_KEY);
    }

    // Срок исполнения ОТ
    public void setStartExecutionDate(Date value) {
        putNotNullToFilters(START_EXECUTION_DATE_KEY, value);
    }

    public Date getStartExecutionDate() {
        return (Date) filters.get(START_EXECUTION_DATE_KEY);
    }

    // Срок исполнения ДО
    public void setEndExecutionDate(Date value) {
        putNotNullToFilters(END_EXECUTION_DATE_KEY, value);
    }

    public Date getEndExecutionDate() {
        return (Date) filters.get(END_EXECUTION_DATE_KEY);
    }

    // Дата поступившего ОТ
    public void setStartReceivedDocumentDate(Date value) {
        putNotNullToFilters(START_RECEIVED_DATE_KEY, value);
    }

    public Date getStartReceivedDocumentDate() {
        return (Date) filters.get(START_RECEIVED_DATE_KEY);
    }

    // дата поступившего ДО
    public void setEndReceivedDocumentDate(Date value) {
        putNotNullToFilters(END_RECEIVED_DATE_KEY, value);
    }

    public Date getEndReceivedDocumentDate() {
        return (Date) filters.get(END_RECEIVED_DATE_KEY);
    }

    // Тип доставки
    public void setDeliveryType(DeliveryType value) {
        putNotNullToFilters(DELIVERY_TYPE_KEY, value);
    }

    public DeliveryType getDeliveryType() {
        return (DeliveryType) filters.get(DELIVERY_TYPE_KEY);
    }

    // Контрагент
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

    // Адресаты
    public void setRecipients(List<User> value) {
        putNotNullToFilters(RECIPIENTS_KEY, value);
    }

    public List<User> getRecipients() {
        return (List<User>) filters.get(RECIPIENTS_KEY);
    }

    public void removeRecipient(User recipient) {
        final List<User> recipients = getRecipients();
        recipients.remove(recipient);
        if(recipients.isEmpty()){
            filters.remove(RECIPIENTS_KEY);
        }
    }

    // том дела
    public void setOfficeKeepingVolume(OfficeKeepingVolume value) {
        putNotNullToFilters(OFFICE_KEEPING_VOLUME_KEY, value);
    }

    public OfficeKeepingVolume getOfficeKeepingVolume() {
        return (OfficeKeepingVolume) filters.get(OFFICE_KEEPING_VOLUME_KEY);
    }

    // Краткое содержание
    public void setShortDescription(final String value) {
        putNotNullToFilters(SHORT_DESCRIPTION_KEY, value);
    }

    public String getShortDescription() {
        return (String) filters.get(SHORT_DESCRIPTION_KEY);
    }


}