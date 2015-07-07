package ru.efive.dms.uifaces.beans.internal;

import com.google.common.collect.ImmutableList;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentSearchBean;
import ru.efive.dms.uifaces.beans.dialogs.AbstractDialog;
import ru.efive.dms.uifaces.beans.dialogs.MultipleUserDialogHolder;
import ru.efive.dms.uifaces.beans.dialogs.UserDialogHolder;
import ru.efive.dms.uifaces.lazyDataModel.documents.LazyDataModelForInternalDocument;
import ru.entity.model.document.InternalDocument;
import ru.entity.model.user.Group;
import ru.entity.model.user.User;
import ru.hitsl.sql.dao.InternalDocumentDAOImpl;
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
import static ru.hitsl.sql.dao.util.ApplicationDAONames.INTERNAL_DOCUMENT_FORM_DAO;
import static ru.hitsl.sql.dao.util.ApplicationDAONames.VIEW_FACT_DAO;
import static ru.hitsl.sql.dao.util.DocumentSearchMapKeys.*;

@Named("internal_search")
@ViewScoped
public class InternalDocumentSearchBean extends AbstractDocumentSearchBean<InternalDocument> {
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
        logger.info("INTERNAL: Perform Search with map : {}", filters);
        try {
            final InternalDocumentDAOImpl dao = sessionManagement.getDAO(InternalDocumentDAOImpl.class, INTERNAL_DOCUMENT_FORM_DAO);
            final ViewFactDaoImpl viewFactDao = sessionManagement.getDAO(ViewFactDaoImpl.class, VIEW_FACT_DAO);
            final LazyDataModelForInternalDocument lazyDataModel = new LazyDataModelForInternalDocument(
                    dao,
                    viewFactDao,
                    sessionManagement.getAuthData()
            );
            lazyDataModel.setFilters(filters);
            setLazyModel(lazyDataModel);
            searchPerformed= true;
        } catch (Exception e) {
            searchPerformed = false;
            logger.error("INTERNAL: Error while search", e);
            FacesContext.getCurrentInstance().addMessage(null, MSG_CANT_DO_SEARCH);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////// Диалоговые окошки  /////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //Выбора автора ////////////////////////////////////////////////////////////////////////////////////////////////////
    //@Inherit

    // Выбора руководителя /////////////////////////////////////////////////////////////////////////////////////////////
    public void chooseController() {
        final Map<String, List<String>> params = new HashMap<String, List<String>>();
        params.put(UserDialogHolder.DIALOG_TITLE_GET_PARAM_KEY, ImmutableList.of(UserDialogHolder.DIALOG_TITLE_VALUE_CONTROLLER));
        params.put(UserDialogHolder.DIALOG_GROUP_KEY, ImmutableList.of(Group.RB_CODE_MANAGERS));
        final User preselected = getController();
        if (preselected != null) {
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(UserDialogHolder.DIALOG_SESSION_KEY, preselected);
        }
        RequestContext.getCurrentInstance().openDialog("/dialogs/selectUserDialog.xhtml", AbstractDialog.getViewOptions(), params);
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
        RequestContext.getCurrentInstance().openDialog("/dialogs/selectUserDialog.xhtml", AbstractDialog.getViewOptions(), params);
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
        RequestContext.getCurrentInstance().openDialog("/dialogs/selectMultipleUserDialog.xhtml", AbstractDialog.getViewOptions(), params);
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

    // Руководитель
    public void setController(final User value) {
        putNotNullToFilters(CONTROLLER_KEY, value);
    }

    public User getController() {
        return (User) filters.get(CONTROLLER_KEY);
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

    // Отвественный исполнитель
    public void setResponsible(final User value) {
        putNotNullToFilters(RESPONSIBLE_KEY, value);
    }

    public User getResponsible() {
        return (User) filters.get(RESPONSIBLE_KEY);
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

}