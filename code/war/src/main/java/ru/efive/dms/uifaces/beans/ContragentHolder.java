package ru.efive.dms.uifaces.beans;

import ru.efive.crm.dao.ContragentDAOHibernate;
import ru.efive.dms.dao.IncomingDocumentDAOImpl;
import ru.efive.dms.dao.OutgoingDocumentDAOImpl;
import ru.efive.dms.dao.RequestDocumentDAOImpl;
import ru.efive.uifaces.bean.AbstractDocumentHolderBean;
import ru.efive.uifaces.bean.FromStringConverter;
import ru.efive.uifaces.bean.ModalWindowHolderBean;
import ru.entity.model.crm.Contragent;
import ru.entity.model.document.IncomingDocument;
import ru.entity.model.document.Nomenclature;
import ru.entity.model.document.OutgoingDocument;
import ru.entity.model.document.RequestDocument;

import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.efive.dms.util.ApplicationDAONames.*;

@Named("contragent")
@ConversationScoped
public class ContragentHolder extends AbstractDocumentHolderBean<Contragent, Integer> implements Serializable {

    @Override
    public String delete() {
        super.delete();
        if (this.getDocument().isDeleted()) {
            try {
                FacesContext.getCurrentInstance().getExternalContext().redirect("delete_contragent.xhtml");
            } catch (Exception e) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_ERROR, "Невозможно удалить документ", ""));
                e.printStackTrace();
            }
            return "";
        } else {
            return "";
        }
    }

    @Override
    protected boolean deleteDocument() {
        boolean result = false;
        try {
            Contragent contragent = getDocument();
            boolean hasDocuments = false;
            Map<String, Object> in_map = new HashMap<String, Object>();
            in_map.put("contragent", contragent);
            List<IncomingDocument> incomingDocuments = sessionManagement.getDAO(IncomingDocumentDAOImpl.class, INCOMING_DOCUMENT_FORM_DAO).findAllDocuments(in_map, false, true, 0, -1);
            if (incomingDocuments.size() == 0) {
                List<RequestDocument> requestDocuments = sessionManagement.getDAO(RequestDocumentDAOImpl.class, REQUEST_DOCUMENT_FORM_DAO).findAllDocuments(in_map, false, true, 0, -1);
                if (requestDocuments.size() == 0) {
                    in_map.clear();
                    List<Contragent> recipients = new ArrayList<Contragent>();
                    recipients.add(contragent);
                    in_map.put("recipientContragents", recipients);
                    List<OutgoingDocument> outgoingDocuments = sessionManagement.getDAO(OutgoingDocumentDAOImpl.class, OUTGOING_DOCUMENT_FORM_DAO).findAllDocuments(in_map, false, true, 0, -1);
                    if (outgoingDocuments.size() != 0) {
                        hasDocuments = true;
                    }
                } else {
                    hasDocuments = true;
                }
            } else {
                hasDocuments = true;
            }
            if (!hasDocuments) {
                contragent.setDeleted(true);
                contragent = sessionManagement.getDAO(ContragentDAOHibernate.class, CONTRAGENT_DAO).save(contragent);

                if (contragent == null) {
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                            FacesMessage.SEVERITY_ERROR, "Невозможно удалить контрагента.", ""));
                }
            } else {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_ERROR, "Невозможно удалить контрагента, так как к нему привязаны документы..", ""));
            }
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Внутренняя ошибка.", ""));
        }
        return result;
    }

    @Override
    protected Integer getDocumentId() {
        return getDocument() == null ? null : getDocument().getId();
    }

    @Override
    protected FromStringConverter<Integer> getIdConverter() {
        return FromStringConverter.INTEGER_CONVERTER;
    }

    @Override
    protected void initDocument(Integer id) {
        try {
            setDocument(sessionManagement.getDAO(ContragentDAOHibernate.class, CONTRAGENT_DAO).get(id));
            if (getDocument() == null) {
                setState(STATE_NOT_FOUND);
            }
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Внутренняя ошибка.", ""));
            e.printStackTrace();
        }
    }

    @Override
    protected void initNewDocument() {
        Contragent contragent = new Contragent();
        contragent.setDeleted(false);

        setDocument(contragent);
    }

    @Override
    protected boolean saveDocument() {
        boolean result = false;
        try {
            Contragent contragent = sessionManagement.getDAO(ContragentDAOHibernate.class, CONTRAGENT_DAO).save(getDocument());
            if (contragent == null) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_ERROR,
                        "Документ не может быть сохранен. Попробуйте повторить позже.", ""));
            }
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Внутренняя ошибка.", ""));
        }
        return result;
    }

    @Override
    protected boolean saveNewDocument() {
        boolean result = false;
        try {
            Contragent contragent = sessionManagement.getDAO(ContragentDAOHibernate.class, CONTRAGENT_DAO).save(getDocument());
            if (contragent == null) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_ERROR,
                        "Документ не может быть сохранен. Попробуйте повторить позже.", ""));
            }
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Внутренняя ошибка.", ""));
        }
        return result;
    }


    public class NomenclatureSelectModal extends ModalWindowHolderBean {

        public Nomenclature getValue() {
            return value;
        }

        public void setValue(Nomenclature value) {
            this.value = value;
        }

        public boolean selected(Nomenclature value) {
            return this.value != null && this.value.getValue().equals(value.getValue());
        }

        public void select(Nomenclature value) {
            this.value = value;
        }

        @Override
        protected void doSave() {
            super.doSave();
            getDocument().setNomenclature(getValue());
        }

        @Override
        protected void doShow() {
            super.doShow();
        }

        @Override
        protected void doHide() {
            super.doHide();
            value = null;
        }

        private Nomenclature value;
        private static final long serialVersionUID = 3204083909477490577L;
    }

    public NomenclatureSelectModal getNomenclatureIndexSelectModal() {
        return nomenclatureSelectModal;
    }


    @Override
    protected String doAfterCreate() {
        contragentList.markNeedRefresh();
        return super.doAfterCreate();
    }

    @Override
    protected String doAfterEdit() {
        contragentList.markNeedRefresh();
        return super.doAfterEdit();
    }

    @Override
    protected String doAfterDelete() {
        contragentList.markNeedRefresh();
        return super.doAfterDelete();
    }

    @Override
    protected String doAfterSave() {
        contragentList.markNeedRefresh();
        return super.doAfterSave();
    }


    private NomenclatureSelectModal nomenclatureSelectModal = new NomenclatureSelectModal();

    @Inject
    @Named("sessionManagement")
    private transient SessionManagementBean sessionManagement;
    @Inject
    @Named("contragentList")
    private transient ContragentListHolderBean contragentList;

    private static final long serialVersionUID = 4716264614655470705L;
}