package ru.efive.dms.uifaces.beans.request;

import com.google.common.collect.ImmutableList;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.efive.dms.dao.RequestDocumentDAOImpl;
import ru.efive.dms.dao.ViewFactDaoImpl;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentSearchBean;
import ru.efive.dms.uifaces.beans.dialogs.AbstractDialog;
import ru.efive.dms.uifaces.beans.dialogs.MultipleUserDialogHolder;
import ru.efive.dms.uifaces.beans.dialogs.UserDialogHolder;
import ru.efive.dms.uifaces.lazyDataModel.documents.LazyDataModelForRequestDocument;
import ru.entity.model.document.DeliveryType;
import ru.entity.model.document.RequestDocument;
import ru.entity.model.user.User;

import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.efive.dms.uifaces.beans.utils.MessageHolder.MSG_CANT_DO_SEARCH;
import static ru.efive.dms.util.ApplicationDAONames.REQUEST_DOCUMENT_FORM_DAO;
import static ru.efive.dms.util.ApplicationDAONames.VIEW_FACT_DAO;
import static ru.efive.dms.util.DocumentSearchMapKeys.*;

@Named("request_search")
@ViewScoped
public class RequestDocumentSearchBean extends AbstractDocumentSearchBean<RequestDocument> {
    private static final Logger logger = LoggerFactory.getLogger("SEARCH");
    @Inject
    @Named("sessionManagement")
    private transient SessionManagementBean sessionManagement;

    /**
     * Выполнить поиск с текущим фильтром
     *
     * @return Список документов, удовлетворяющих поиску
     */
    @Override
    public void performSearch() {
        logger.info("REQUEST: Perform Search with map : {}", filters);
        try {
            final RequestDocumentDAOImpl dao = sessionManagement.getDAO(RequestDocumentDAOImpl.class, REQUEST_DOCUMENT_FORM_DAO);
            final ViewFactDaoImpl viewFactDao = sessionManagement.getDAO(ViewFactDaoImpl.class, VIEW_FACT_DAO);
            final LazyDataModelForRequestDocument lazyDataModelForIncomingDocument = new LazyDataModelForRequestDocument(
                    dao, viewFactDao, sessionManagement.getAuthData()
            );
            lazyDataModelForIncomingDocument.setFilters(filters);
            setLazyModel(lazyDataModelForIncomingDocument);
            searchPerformed = true;
        } catch (Exception e) {
            logger.error("REQUEST: Error while search", e);
            FacesContext.getCurrentInstance().addMessage(null, MSG_CANT_DO_SEARCH);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////// Диалоговые окошки  /////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // Выбора ответственного исполнителя /////////////////////////////////////////////////////////////////////////////////////////////
    public void chooseResponsible() {
        final Map<String, List<String>> params = new HashMap<String, List<String>>();
        params.put(UserDialogHolder.DIALOG_TITLE_GET_PARAM_KEY, ImmutableList.of(UserDialogHolder
                                                                                         .DIALOG_TITLE_VALUE_RESPONSIBLE));
        final User preselected = getResponsible();
        if (preselected != null) {
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(UserDialogHolder
                                                                                               .DIALOG_SESSION_KEY, preselected);
        }
        RequestContext.getCurrentInstance().openDialog("/dialogs/selectUserDialog.xhtml", AbstractDialog.getViewParams(), params);
    }

    public void onResponsibleChosen(SelectEvent event) {
        final AbstractDialog.DialogResult result = (AbstractDialog.DialogResult) event.getObject();
        logger.info("Choose responsible: {}", result);
        if(AbstractDialog.Button.CONFIRM.equals(result.getButton())) {
            final User selected = (User) result.getResult();
            if (selected != null) {
                setResponsible(selected);
            } else {
                filters.remove(RESPONSIBLE_KEY);
            }
        }
    }

    // Выбора адресатов /////////////////////////////////////////////////////////////////////////////////////////////
    public void chooseRecipients() {
        final Map<String, List<String>> params = new HashMap<String, List<String>>();
        params.put(MultipleUserDialogHolder.DIALOG_TITLE_GET_PARAM_KEY, ImmutableList.of(MultipleUserDialogHolder.DIALOG_TITLE_VALUE_RECIPIENTS));
        final List<User> preselected = getRecipients();
        if (preselected != null && !preselected.isEmpty()) {
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(MultipleUserDialogHolder.DIALOG_SESSION_KEY, preselected);
        }
        RequestContext.getCurrentInstance().openDialog("/dialogs/selectMultipleUserDialog.xhtml", AbstractDialog.getViewParams(), params);
    }

    public void onRecipientsChosen(SelectEvent event) {
        final AbstractDialog.DialogResult result = (AbstractDialog.DialogResult) event.getObject();
        logger.info("Choose recipients: {}", result);
        if(AbstractDialog.Button.CONFIRM.equals(result.getButton())) {
            final List<User> selected = (List<User>) result.getResult();
            if (selected != null && !selected.isEmpty()) {
                setRecipients(selected);
            } else {
                filters.remove(RECIPIENTS_KEY);
            }
        }
    }



    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Параметры поиска ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public Date getStartExecutionDate() {
        return (Date) filters.get(START_EXECUTION_DATE_KEY);
    }

    // Срок исполнения ОТ
    public void setStartExecutionDate(Date value) {
        putNotNullToFilters(START_EXECUTION_DATE_KEY, value);
    }

    public Date getEndExecutionDate() {
        return (Date) filters.get(END_EXECUTION_DATE_KEY);
    }

    // Срок исполнения ДО
    public void setEndExecutionDate(Date value) {
        putNotNullToFilters(END_EXECUTION_DATE_KEY, value);
    }

    public Date getStartDeliveryDate() {
        return (Date) filters.get(START_DELIVERY_DATE_KEY);
    }

    // Дата доставки ОТ
    public void setStartDeliveryDate(Date value) {
        putNotNullToFilters(START_DELIVERY_DATE_KEY, value);
    }

    public Date getEndDeliveryDate() {
        return (Date) filters.get(END_DELIVERY_DATE_KEY);
    }

    // Дата доставки ДО
    public void setEndDeliveryDate(Date value) {
        putNotNullToFilters(END_DELIVERY_DATE_KEY, value);
    }

    public DeliveryType getDeliveryType() {
        return (DeliveryType) filters.get(DELIVERY_TYPE_KEY);
    }

    // Тип доставки
    public void setDeliveryType(DeliveryType value) {
        putNotNullToFilters(DELIVERY_TYPE_KEY, value);
    }

    public String getSenderLastName() {
        return (String) filters.get(SENDER_LAST_NAME_KEY);
    }

    //Фамилия отправителя
    public void setSenderLastName(String value) {
        putNotNullToFilters(SENDER_LAST_NAME_KEY, value);
    }

    public String getSenderFirstName() {
        return (String) filters.get(SENDER_FIRST_NAME_KEY);
    }

    //Имя отправителя
    public void setSenderFirstName(String value) {
        putNotNullToFilters(SENDER_FIRST_NAME_KEY, value);
    }

    public String getSenderPatrName() {
        return (String) filters.get(SENDER_PATR_NAME_KEY);
    }

    //Отчество отправителя
    public void setSenderPatrName(String value) {
        putNotNullToFilters(SENDER_PATR_NAME_KEY, value);
    }

    public User getResponsible() {
        return (User) filters.get(RESPONSIBLE_KEY);
    }

    // Отвественный исполнитель
    public void setResponsible(final User value) {
        putNotNullToFilters(RESPONSIBLE_KEY, value);
    }

    public List<User> getRecipients() {
        return (List<User>) filters.get(RECIPIENTS_KEY);
    }

    // Адресаты
    public void setRecipients(List<User> value) {
        putNotNullToFilters(RECIPIENTS_KEY, value);
    }

    public void removeRecipient(User recipient) {
        final List<User> recipients = getRecipients();
        recipients.remove(recipient);
        if (recipients.isEmpty()) {
            filters.remove(RECIPIENTS_KEY);
        }
    }
}