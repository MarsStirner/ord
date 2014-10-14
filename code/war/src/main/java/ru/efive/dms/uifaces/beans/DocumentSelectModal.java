package ru.efive.dms.uifaces.beans;

import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;

import ru.entity.model.document.IncomingDocument;
import ru.entity.model.document.RequestDocument;
import ru.efive.dms.uifaces.beans.incoming.IncomingDocumentListHolder;
import ru.efive.dms.uifaces.beans.request.RequestDocumentListHolder;
import ru.efive.uifaces.bean.ModalWindowHolderBean;

public class DocumentSelectModal extends ModalWindowHolderBean {
    //--------------------------------------------------------------------------------------------------------------
    public IncomingDocumentListHolder getIncomingDocuments() {
        if (incomingDocuments == null) {
            FacesContext context = FacesContext.getCurrentInstance();
            incomingDocuments = (IncomingDocumentListHolder) context.getApplication().evaluateExpressionGet(context, "#{in_documents}", IncomingDocumentListHolder.class);
        }
        return incomingDocuments;
    }

    public IncomingDocument getIncomingDocument() {
        return incomingDocument;
    }

    public void setIncomingDocument(IncomingDocument incomingDocument) {
        this.incomingDocument = incomingDocument;
    }

    //--------------------------------------------------------------------------------------------------------------
    public RequestDocumentListHolder getRequestDocuments() {
        if (requestDocuments == null) {
            FacesContext context = FacesContext.getCurrentInstance();
            requestDocuments = (RequestDocumentListHolder) context.getApplication().evaluateExpressionGet(context, "#{request_documents}", RequestDocumentListHolder.class);
        }
        return requestDocuments;
    }

    public RequestDocument getRequestDocument() {
        return requestDocument;
    }

    public void setRequestDocument(RequestDocument requestDocument) {
        this.requestDocument = requestDocument;
    }
    //--------------------------------------------------------------------------------------------------------------

    public List<String> getViewsType() {
        List<String> in_results = new ArrayList<String>();
        in_results.add("Входящие документы");
        in_results.add("Обращения граждан");
        return in_results;
    }

    public String getViewType() {
        return (viewType == null) ? "" : viewType;
    }

    public void setViewType(String viewType) {
        this.viewType = viewType;
    }

    //--------------------------------------------------------------------------------------------------------------
    public void select(Object object) {

        if (object.getClass().equals(IncomingDocument.class)) {
            System.out.println("IncomingDoc");
            this.incomingDocument = (IncomingDocument) object;
        } else if (object.getClass().equals(RequestDocument.class)) {
            System.out.println("RequestDoc");
            this.requestDocument = (RequestDocument) object;
        } else if (object.getClass().equals(String.class)) {
            System.out.println("viewType");
            this.viewType = (String) object;
        }
    }

    public boolean selected(Object object) {
        if (object.getClass().equals(IncomingDocument.class)) {
            return (this.incomingDocument == null) ? false : this.incomingDocument.equals((IncomingDocument) object);
        } else if (object.getClass().equals(RequestDocument.class)) {
            return (this.requestDocument == null) ? false : this.requestDocument.equals((RequestDocument) object);
        } else if (object.getClass().equals(String.class)) {
            return (this.viewType == null) ? false : this.viewType.equals((String) object);
        } else {
            return false;
        }
    }

    @Override
    protected void doSave() {
        super.doSave();
    }

    @Override
    protected void doShow() {
        super.doShow();
    }

    @Override
    protected void doHide() {
        super.doHide();
    }

    public void setViewTypesAlreadySelected(boolean isViewTypesAlreadySelected) {
        this.isViewTypesAlreadySelected = isViewTypesAlreadySelected;
    }

    public boolean isViewTypesAlreadySelected() {
        return isViewTypesAlreadySelected;
    }

    private String viewType;
    private IncomingDocumentListHolder incomingDocuments;
    private IncomingDocument incomingDocument;

    private RequestDocumentListHolder requestDocuments;
    private RequestDocument requestDocument;

    private boolean isViewTypesAlreadySelected = false;

    private static final long serialVersionUID = 8811191935540534357L;

}