package ru.efive.dms.uifaces.beans.request;

import com.github.javaplugs.jsf.SpringScopeView;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentSearchBean;
import ru.efive.dms.uifaces.beans.dialogs.AbstractDialog;
import ru.efive.dms.uifaces.beans.dialogs.MultipleUserDialogHolder;
import ru.efive.dms.uifaces.beans.dialogs.UserDialogHolder;
import ru.efive.dms.util.message.MessageUtils;
import ru.entity.model.document.RequestDocument;
import ru.entity.model.referenceBook.DeliveryType;
import ru.entity.model.user.User;

import javax.faces.context.FacesContext;
import java.util.*;

import static ru.efive.dms.util.message.MessageHolder.MSG_CANT_DO_SEARCH;
import static ru.hitsl.sql.dao.util.DocumentSearchMapKeys.*;

@Controller("request_search")
@SpringScopeView
public class RequestDocumentSearchBean extends AbstractDocumentSearchBean<RequestDocument> {
    private static final Logger logger = LoggerFactory.getLogger("SEARCH");

    @Autowired
    @Qualifier("requestDocumentLDM")
    private RequestDocumentLazyDataModel lazyDataModel;

    /**
     * Выполнить поиск с текущим фильтром
     *
     * @return Список документов, удовлетворяющих поиску
     */
    @Override
    public void performSearch() {
        logger.info("REQUEST: Perform Search with map : {}", filters);
        try {
            lazyDataModel.setFilters(filters);
            setLazyModel(lazyDataModel);
            searchPerformed = true;
        } catch (Exception e) {
            searchPerformed = false;
            logger.error("REQUEST: Error while search", e);
            MessageUtils.addMessage(MSG_CANT_DO_SEARCH);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////// Диалоговые окошки  /////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // Выбора ответственного исполнителя /////////////////////////////////////////////////////////////////////////////////////////////
    public void chooseResponsible() {
        final Map<String, List<String>> params = new HashMap<>();
        params.put(UserDialogHolder.DIALOG_TITLE_GET_PARAM_KEY, Collections.singletonList(UserDialogHolder.DIALOG_TITLE_VALUE_RESPONSIBLE));
        final User preselected = getResponsible();
        if (preselected != null) {
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(UserDialogHolder.DIALOG_SESSION_KEY, preselected);
        }
        RequestContext.getCurrentInstance().openDialog("/dialogs/selectUserDialog.xhtml", AbstractDialog.getViewOptions(), params);
    }

    public void onResponsibleChosen(SelectEvent event) {
        final AbstractDialog.DialogResult result = (AbstractDialog.DialogResult) event.getObject();
        logger.info("Choose responsible: {}", result);
        if (AbstractDialog.Button.CONFIRM.equals(result.getButton())) {
            final User selected = (User) result.getResult();
            if (selected != null) {
                setResponsible(selected);
            } else {
                filters.remove(RESPONSIBLE);
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


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Параметры поиска ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public Date getStartExecutionDate() {
        return (Date) filters.get(EXECUTION_DATE_START);
    }

    // Срок исполнения ОТ
    public void setStartExecutionDate(Date value) {
        putNotNullToFilters(EXECUTION_DATE_START, value);
    }

    public Date getEndExecutionDate() {
        return (Date) filters.get(EXECUTION_DATE_END);
    }

    // Срок исполнения ДО
    public void setEndExecutionDate(Date value) {
        putNotNullToFilters(EXECUTION_DATE_END, value);
    }

    public Date getStartDeliveryDate() {
        return (Date) filters.get(DELIVERY_DATE_START);
    }

    // Дата доставки ОТ
    public void setStartDeliveryDate(Date value) {
        putNotNullToFilters(DELIVERY_DATE_START, value);
    }

    public Date getEndDeliveryDate() {
        return (Date) filters.get(DELIVERY_DATE_END);
    }

    // Дата доставки ДО
    public void setEndDeliveryDate(Date value) {
        putNotNullToFilters(DELIVERY_DATE_END, value);
    }

    public DeliveryType getDeliveryType() {
        return (DeliveryType) filters.get(DELIVERY_TYPE);
    }

    // Тип доставки
    public void setDeliveryType(DeliveryType value) {
        putNotNullToFilters(DELIVERY_TYPE, value);
    }

    public String getSenderLastName() {
        return (String) filters.get(SENDER_LAST_NAME);
    }

    //Фамилия отправителя
    public void setSenderLastName(String value) {
        putNotNullToFilters(SENDER_LAST_NAME, value);
    }

    public String getSenderFirstName() {
        return (String) filters.get(SENDER_FIRST_NAME);
    }

    //Имя отправителя
    public void setSenderFirstName(String value) {
        putNotNullToFilters(SENDER_FIRST_NAME, value);
    }

    public String getSenderPatrName() {
        return (String) filters.get(SENDER_PATR_NAME);
    }

    //Отчество отправителя
    public void setSenderPatrName(String value) {
        putNotNullToFilters(SENDER_PATR_NAME, value);
    }

    public User getResponsible() {
        return (User) filters.get(RESPONSIBLE);
    }

    // Отвественный исполнитель
    public void setResponsible(final User value) {
        putNotNullToFilters(RESPONSIBLE, value);
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
}