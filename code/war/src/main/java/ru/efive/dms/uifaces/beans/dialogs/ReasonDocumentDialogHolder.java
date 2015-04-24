package ru.efive.dms.uifaces.beans.dialogs;

import org.apache.commons.lang.StringUtils;
import org.primefaces.context.RequestContext;
import ru.efive.dms.dao.IncomingDocumentDAOImpl;
import ru.efive.dms.dao.RequestDocumentDAOImpl;
import ru.efive.dms.uifaces.beans.IndexManagementBean;
import ru.entity.model.document.IncomingDocument;
import ru.entity.model.document.RequestDocument;
import ru.util.ApplicationHelper;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import static ru.efive.dms.util.ApplicationDAONames.INCOMING_DOCUMENT_FORM_DAO;
import static ru.efive.dms.util.ApplicationDAONames.REQUEST_DOCUMENT_FORM_DAO;

/**
 * Author: Upatov Egor <br>
 * Date: 23.04.2015, 17:45 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
@ManagedBean(name = "reasonDocumentDialog")
@ViewScoped
public class ReasonDocumentDialogHolder {

    @EJB(name = "indexManagement")
    private IndexManagementBean indexManagementBean;


    public static final String DIALOG_SESSION_KEY = "DIALOG_REASON_DOCUMENT";

    private boolean isIncoming = true;
    private IncomingDocument incomingSelection;
    private RequestDocument requestSelection;

    @PostConstruct
    public void init() {
        initializePreSelected();
    }

    /**
     * Выбрать заранее заднный список пользователей по ключу сессии
     */
    public void initializePreSelected() {
        final String preselected = (String) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(DIALOG_SESSION_KEY);
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove(DIALOG_SESSION_KEY);
        if (StringUtils.isNotEmpty(preselected)) {
            final Integer rootDocumentId = ApplicationHelper.getIdFromUniqueIdString(preselected);
            if (preselected.contains("incoming_")) {
                incomingSelection = ((IncomingDocumentDAOImpl)indexManagementBean.getContext().getBean(INCOMING_DOCUMENT_FORM_DAO))
                        .getItemByIdForSimpleView(rootDocumentId);
            } else if (preselected.contains("request_")) {
                requestSelection = ((RequestDocumentDAOImpl)indexManagementBean.getContext().getBean(REQUEST_DOCUMENT_FORM_DAO))
                        .getItemByIdForSimpleView(rootDocumentId);
            }
        }
    }

    public boolean isIncoming() {
        return isIncoming;
    }

    public void setIsIncoming(final boolean isIncoming) {
        this.isIncoming = isIncoming;
        if (isIncoming) {
            requestSelection = null;
        } else {
            incomingSelection = null;
        }
    }

    public String getTitle() {
        return "Выбор документа основания";
    }

    public void chooseIncoming() {
        isIncoming = true;
    }

    public void chooseRequest() {
        isIncoming = false;
    }

    public IncomingDocument getIncomingSelection() {
        return incomingSelection;
    }

    public void setIncomingSelection(IncomingDocument incomingSelection) {
        this.incomingSelection = incomingSelection;
    }

    public RequestDocument getRequestSelection() {
        return requestSelection;
    }

    public void setRequestSelection(RequestDocument requestSelection) {
        this.requestSelection = requestSelection;
    }

    /**
     * Закрыть диалог с результатом
     *
     * @param withResult флаг указывающий передавать ли результат работы диалога
     */
    public void closeDialog(boolean withResult) {
        String result=null;
        if(isIncoming)     {
            if(incomingSelection != null){
                result = incomingSelection.getUniqueId();
            }
        }  else {
            if(requestSelection != null){
                result = requestSelection.getUniqueId();
            }
        }
        RequestContext.getCurrentInstance().closeDialog(withResult ? result : null);
    }
}
