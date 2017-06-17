package ru.efive.dms.uifaces.beans.incoming;

import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentSearchBean;
import ru.efive.dms.uifaces.beans.annotations.ViewScopedController;
import ru.efive.dms.uifaces.beans.dialogs.*;
import ru.efive.dms.util.message.MessageUtils;
import ru.entity.model.document.IncomingDocument;
import ru.entity.model.document.OfficeKeepingVolume;
import ru.entity.model.referenceBook.Contragent;
import ru.entity.model.referenceBook.DeliveryType;
import ru.entity.model.referenceBook.Group;
import ru.entity.model.user.User;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.efive.dms.util.message.MessageHolder.MSG_CANT_DO_SEARCH;
import static ru.hitsl.sql.dao.util.DocumentSearchMapKeys.*;

@ViewScopedController(value = "incomingSearch", transactionManager = "ordTransactionManager")
public class IncomingDocumentSearchBean extends AbstractDocumentSearchBean<IncomingDocument> {
    private static final Logger logger = LoggerFactory.getLogger("SEARCH");

    @Autowired
    public void setLazyModel(@Qualifier("incomingDocumentLDM") IncomingDocumentLazyDataModel lazyModel) {
        this.lazyModel = lazyModel;
    }

    public boolean validate() {
        if (filters.isEmpty()) {
            return true;
        }
        boolean result = true;
        if (!checkDateRange(getStartCreationDate(), getEndCreationDate())) {
            MessageUtils.addMessage("creationDate", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Диапозон дат для фильтрации по дате создания документа задан неверно", null));
            result = false;
        }
        if (!checkDateRange(getStartRegistrationDate(), getEndRegistrationDate())) {
            MessageUtils.addMessage("registrationDate", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Диапозон дат для фильтрации по дате регистрации документа задан неверно", null));
            result = false;
        }

        return result;
    }

    private boolean checkDateRange(LocalDateTime startInterval, LocalDateTime endInterval) {
        return startInterval == null || endInterval == null || startInterval.isBefore(endInterval) || startInterval.isEqual(endInterval);
    }

    /**
     * Выполнить поиск с текущим фильтром
     *
     * @return Список документов, удовлетворяющих поиску
     */
    @Override
    public void performSearch() {
        logger.info("INCOMING: Perform Search with map : {}", filters);
        if (validate()) {
            try {
                getLazyModel().setFilters(filters);
                searchPerformed = true;
            } catch (Exception e) {
                searchPerformed = false;
                logger.error("INCOMING: Error while search", e);
                MessageUtils.addMessage(MSG_CANT_DO_SEARCH);
            }
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////// Диалоговые окошки  /////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //Выбора автора ////////////////////////////////////////////////////////////////////////////////////////////////////
    //@Inherit

    // Выбора руководителя /////////////////////////////////////////////////////////////////////////////////////////////
    public void chooseController() {
        final Map<String, List<String>> params = new HashMap<>();
        params.put(UserDialogHolder.DIALOG_TITLE_GET_PARAM_KEY, Collections.singletonList(UserDialogHolder.DIALOG_TITLE_VALUE_CONTROLLER));
        params.put(UserDialogHolder.DIALOG_GROUP_KEY, Collections.singletonList(Group.RB_CODE_MANAGERS));
        final User preselected = getController();
        if (preselected != null) {
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(UserDialogHolder.DIALOG_SESSION_KEY, preselected);
        }
        RequestContext.getCurrentInstance().openDialog("/dialogs/selectUserDialog.xhtml", AbstractDialog.getViewOptions(), params);
    }

    public void onControllerChosen(SelectEvent event) {
        final AbstractDialog.DialogResult result = (AbstractDialog.DialogResult) event.getObject();
        logger.info("Choose controller: {}", result);
        if (AbstractDialog.Button.CONFIRM.equals(result.getButton())) {
            final User selected = (User) result.getResult();
            if (selected != null) {
                setController(selected);
            } else {
                filters.remove(CONTROLLER);
            }
        }
    }

    // Выбора контрагента //////////////////////////////////////////////////////////////////////////////////////////////
    public void chooseContragent() {
        final Contragent preselected = getContragent();
        if (preselected != null) {
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ContragentDialogHolder.DIALOG_SESSION_KEY, preselected);
        }
        RequestContext.getCurrentInstance().openDialog("/dialogs/selectContragentDialog.xhtml", AbstractDialog.getViewOptions(), null);
    }

    public void onContragentChosen(SelectEvent event) {
        final AbstractDialog.DialogResult result = (AbstractDialog.DialogResult) event.getObject();
        logger.info("Choose contragent: {}", result);
        if (AbstractDialog.Button.CONFIRM.equals(result.getButton())) {
            final Contragent selected = (Contragent) result.getResult();
            if (selected != null) {
                setContragent(selected);
            } else {
                filters.remove(CONTRAGENT);
            }
        }
    }

    // Выбора исполнителей /////////////////////////////////////////////////////////////////////////////////////////////
    public void chooseExecutors() {
        final Map<String, List<String>> params = new HashMap<>();
        params.put(MultipleUserDialogHolder.DIALOG_TITLE_GET_PARAM_KEY, Collections.singletonList(MultipleUserDialogHolder.DIALOG_TITLE_VALUE_EXECUTORS));
        final List<User> preselected = getExecutors();
        if (preselected != null && !preselected.isEmpty()) {
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(MultipleUserDialogHolder.DIALOG_SESSION_KEY, preselected);
        }
        RequestContext.getCurrentInstance().openDialog("/dialogs/selectMultipleUserDialog.xhtml", AbstractDialog.getViewOptions(), params);
    }

    public void onExecutorsChosen(SelectEvent event) {
        final AbstractDialog.DialogResult result = (AbstractDialog.DialogResult) event.getObject();
        logger.info("Choose executors: {}", result);
        if (AbstractDialog.Button.CONFIRM.equals(result.getButton())) {
            final List<User> selected = (List<User>) result.getResult();
            if (selected != null && !selected.isEmpty()) {
                setExecutors(selected);
            } else {
                filters.remove(EXECUTORS);
            }
        }
    }

    // Выбора адресатов /////////////////////////////////////////////////////////////////////////////////////////////
    public void chooseRecipients() {
        final Map<String, List<String>> params = new HashMap<>();
        params.put(MultipleUserDialogHolder.DIALOG_TITLE_GET_PARAM_KEY, Collections.singletonList(MultipleUserDialogHolder.DIALOG_TITLE_VALUE_RECIPIENTS));
        final List<User> preselected = getRecipients();
        if (preselected != null && !preselected.isEmpty()) {
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(MultipleUserDialogHolder.DIALOG_SESSION_KEY, preselected);
        }
        RequestContext.getCurrentInstance().openDialog("/dialogs/selectMultipleUserDialog.xhtml", AbstractDialog.getViewOptions(), params);
    }

    public void onRecipientsChosen(SelectEvent event) {
        final AbstractDialog.DialogResult result = (AbstractDialog.DialogResult) event.getObject();
        logger.info("Choose recipients: {}", result);
        if (AbstractDialog.Button.CONFIRM.equals(result.getButton())) {
            final List<User> selected = (List<User>) result.getResult();
            if (selected != null && !selected.isEmpty()) {
                setRecipients(selected);
            } else {
                filters.remove(RECIPIENTS);
            }
        }
    }

    // Выбора томов дел /////////////////////////////////////////////////////////////////////////////////////////////
    public void chooseOfficeKeepingVolume() {
        final OfficeKeepingVolume preselected = getOfficeKeepingVolume();
        if (preselected != null) {
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(OfficeKeepingVolumeDialogHolder.DIALOG_SESSION_KEY, preselected);
        }
        RequestContext.getCurrentInstance().openDialog("/dialogs/selectOfficeKeepingVolumeDialog.xhtml", AbstractDialog.getViewOptions(), null);
    }

    public void onOfficeKeepingVolumeChosen(SelectEvent event) {
        final AbstractDialog.DialogResult result = (AbstractDialog.DialogResult) event.getObject();
        logger.info("Choose officeKeepingVolume: {}", result);
        if (AbstractDialog.Button.CONFIRM.equals(result.getButton())) {
            final OfficeKeepingVolume selected = (OfficeKeepingVolume) result.getResult();
            if (selected != null) {
                setOfficeKeepingVolume(selected);
            } else {
                filters.remove(OFFICE_KEEPING_VOLUME);
            }
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Параметры поиска ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public String getReceivedDocumentNumber() {
        return (String) filters.get(RECEIVED_DOCUMENT_NUMBER);
    }

    // Регистрационный номер
    public void setReceivedDocumentNumber(final String value) {
        putNotNullToFilters(RECEIVED_DOCUMENT_NUMBER, value);
    }

    public User getController() {
        return (User) filters.get(CONTROLLER);
    }

    // Руководитель
    public void setController(final User value) {
        putNotNullToFilters(CONTROLLER, value);
    }

    public LocalDateTime getStartDeliveryDate() {
        return (LocalDateTime) filters.get(DELIVERY_DATE_START);
    }

    // Дата доставки ОТ
    public void setStartDeliveryDate(LocalDateTime value) {
        putNotNullToFilters(DELIVERY_DATE_START, value);
    }

    public LocalDateTime getEndDeliveryDate() {
        return (LocalDateTime) filters.get(DELIVERY_DATE_END);
    }

    // Дата доставки ДО
    public void setEndDeliveryDate(LocalDateTime value) {
        putNotNullToFilters(DELIVERY_DATE_END, value);
    }

    public LocalDateTime getStartExecutionDate() {
        return (LocalDateTime) filters.get(EXECUTION_DATE_START);
    }

    // Срок исполнения ОТ
    public void setStartExecutionDate(LocalDateTime value) {
        putNotNullToFilters(EXECUTION_DATE_START, value);
    }

    public LocalDateTime getEndExecutionDate() {
        return (LocalDateTime) filters.get(EXECUTION_DATE_END);
    }

    // Срок исполнения ДО
    public void setEndExecutionDate(LocalDateTime value) {
        putNotNullToFilters(EXECUTION_DATE_END, value);
    }

    public LocalDateTime getStartReceivedDocumentDate() {
        return (LocalDateTime) filters.get(RECEIVED_DATE_START);
    }

    // Дата поступившего ОТ
    public void setStartReceivedDocumentDate(LocalDateTime value) {
        putNotNullToFilters(RECEIVED_DATE_START, value);
    }

    public LocalDateTime getEndReceivedDocumentDate() {
        return (LocalDateTime) filters.get(RECEIVED_DATE_END);
    }

    // дата поступившего ДО
    public void setEndReceivedDocumentDate(LocalDateTime value) {
        putNotNullToFilters(RECEIVED_DATE_END, value);
    }

    public DeliveryType getDeliveryType() {
        return (DeliveryType) filters.get(DELIVERY_TYPE);
    }

    // Тип доставки
    public void setDeliveryType(DeliveryType value) {
        putNotNullToFilters(DELIVERY_TYPE, value);
    }

    public Contragent getContragent() {
        return (Contragent) filters.get(CONTRAGENT);
    }

    // Контрагент
    public void setContragent(Contragent value) {
        putNotNullToFilters(CONTRAGENT, value);
    }

    public List<User> getExecutors() {
        return (List<User>) filters.get(EXECUTORS);
    }

    // Исполнители
    public void setExecutors(List<User> value) {
        putNotNullToFilters(EXECUTORS, value);
    }

    public void removeExecutor(User executor) {
        final List<User> executors = getExecutors();
        executors.remove(executor);
        if (executors.isEmpty()) {
            filters.remove(EXECUTORS);
        }
    }

    public List<User> getRecipients() {
        return (List<User>) filters.get(RECIPIENTS);
    }

    // Адресаты
    public void setRecipients(List<User> value) {
        putNotNullToFilters(RECIPIENTS, value);
    }

    public void removeRecipient(User recipient) {
        final List<User> recipients = getRecipients();
        recipients.remove(recipient);
        if (recipients.isEmpty()) {
            filters.remove(RECIPIENTS);
        }
    }

    public OfficeKeepingVolume getOfficeKeepingVolume() {
        return (OfficeKeepingVolume) filters.get(OFFICE_KEEPING_VOLUME);
    }

    // том дела
    public void setOfficeKeepingVolume(OfficeKeepingVolume value) {
        putNotNullToFilters(OFFICE_KEEPING_VOLUME, value);
    }
}