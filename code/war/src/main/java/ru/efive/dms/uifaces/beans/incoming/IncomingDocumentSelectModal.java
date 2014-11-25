package ru.efive.dms.uifaces.beans.incoming;

import ru.efive.uifaces.bean.ModalWindowHolderBean;
import ru.entity.model.document.IncomingDocument;

import javax.faces.context.FacesContext;

public class IncomingDocumentSelectModal extends ModalWindowHolderBean {

    public IncomingDocumentListHolder getIncomingDocumentList() {
        if (incomingDocumentList == null) {
            FacesContext context = FacesContext.getCurrentInstance();
            incomingDocumentList = context.getApplication().evaluateExpressionGet(context, "#{in_documents}", IncomingDocumentListHolder.class);
        }
        return incomingDocumentList;
    }

    public IncomingDocument getIncomingDocument() {
        return incomingDocument;
    }

    public void setIncomingDocument(IncomingDocument incomingDocument) {
        this.incomingDocument = incomingDocument;
    }

    public void select(IncomingDocument incomingDocument) {
        this.incomingDocument = incomingDocument;
    }

    public boolean selected(IncomingDocument incomingDocument) {
        return this.incomingDocument != null && this.incomingDocument.equals(incomingDocument);
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

    private IncomingDocumentListHolder incomingDocumentList;

    private IncomingDocument incomingDocument;

    private static final long serialVersionUID = 8811191935540534357L;

}