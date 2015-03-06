package ru.efive.dms.uifaces.beans.incoming;

import com.google.common.collect.ImmutableList;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.efive.dms.dao.IncomingDocumentDAOImpl;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentSearchBean;
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

    /**
     * Выполнить поиск с текущим фильтром
     *
     * @return Список документов, удовлетворяющих поиску
     */
    @Override
    public List<IncomingDocument> performSearch() {
        logger.info("Perform Search with map : {}", filters);
        try {
            searchResults = sessionManagement.getDAO(IncomingDocumentDAOImpl.class, INCOMING_DOCUMENT_FORM_DAO)
                    .findAllDocumentsByUser(filters, null, sessionManagement.getLoggedUser(), false, false);

        } catch (Exception e) {
            logger.error("Error wile search INCOMING", e);
            FacesContext.getCurrentInstance().addMessage(null, MSG_CANT_DO_SEARCH);
        }
        return searchResults;
    }
    /////////////////////////////// Диалоговые окошки
    public void chooseAuthor() {
        final Map<String, List<String>> params = new HashMap<String, List<String>>();
        if(getAuthor() != null) {
            params.put("PERSON_ID", ImmutableList.of(String.valueOf(getAuthor().getId())));
        }
        params.put("TITLE", ImmutableList.of("AUTHOR_TITLE"));
        RequestContext.getCurrentInstance().openDialog("/dialogs/selectUserDialog.xhtml", null, params);
    }

    public void onAuthorChosen(SelectEvent event) {
        final User author = (User) event.getObject();
        if(author != null){
            setAuthor(author);
        }
        logger.info("Choose author From Dialog \'{}\'", author !=null ? author.getDescription() : "#NOTSET");
    }

    public void chooseController() {
        final Map<String, List<String>> params = new HashMap<String, List<String>>();
        if(getController() != null) {
            params.put("PERSON_ID", ImmutableList.of(String.valueOf(getController().getId())));
        }
        params.put("TITLE", ImmutableList.of("CONTROLLER_TITLE"));
        RequestContext.getCurrentInstance().openDialog("/dialogs/selectUserDialog.xhtml", null, params);
    }

    public void onControllerChosen(SelectEvent event) {
        final User controller = (User) event.getObject();
        if(controller != null){
            setController(controller);
        }
        logger.info("Choose controller From Dialog \'{}\'", controller != null ? controller.getDescription() : "#NOTSET");
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // Статус
    public void setStatus(final String value) {
        putNotNullToFilters(STATUS_KEY, value);
    }

    public String getStatus() {
        return (String) filters.get(STATUS_KEY);
    }

    // Автор
    public void setAuthor(final User value) {
        putNotNullToFilters(AUTHOR_KEY, value);
    }

    public User getAuthor() {
        return (User) filters.get(AUTHOR_KEY);
    }

    // Руководитель
    public void setController(final User value) {
        putNotNullToFilters(CONTROLLER_KEY, value);
    }

    public User getController() {
        return (User) filters.get(CONTROLLER_KEY);
    }

    //  Вид документа
    public void setForm(final DocumentForm value) {
        putNotNullToFilters(FORM_KEY, value);
    }

    public DocumentForm getForm() {
        return (DocumentForm) filters.get(FORM_KEY);
    }

    // Регистрационный номер
    public void setRegistrationNumber(final String value) {
        putNotNullToFilters(REGISTRATION_NUMBER_KEY, value);
    }

    public String getRegistrationNumber() {
        return (String) filters.get(REGISTRATION_NUMBER_KEY);
    }

    // Номер поступившего
    public void setReceivedDocumentNumber(final String value) {
        putNotNullToFilters(RECEIVED_DOCUMENT_NUMBER_KEY, value);
    }

    public String getReceivedDocumentNumber() {
        return (String) filters.get(RECEIVED_DOCUMENT_NUMBER_KEY);
    }

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

    // Адресаты
    public void setRecipients(List<User> value) {
        putNotNullToFilters(RECIPIENTS_KEY, value);
    }

    public List<User> getRecipients() {
        return (List<User>) filters.get(RECIPIENTS_KEY);
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