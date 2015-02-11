package ru.efive.dms.uifaces.beans;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.efive.crm.dao.ContragentDAOHibernate;
import ru.efive.dms.dao.IncomingDocumentDAOImpl;
import ru.efive.dms.dao.OutgoingDocumentDAOImpl;
import ru.efive.dms.dao.RequestDocumentDAOImpl;
import ru.efive.uifaces.bean.AbstractDocumentHolderBean;
import ru.efive.uifaces.bean.FromStringConverter;
import ru.entity.model.crm.Contragent;
import ru.entity.model.document.IncomingDocument;
import ru.entity.model.document.OutgoingDocument;
import ru.entity.model.document.RequestDocument;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.efive.dms.uifaces.beans.utils.MessageHolder.*;
import static ru.efive.dms.util.ApplicationDAONames.*;

@ManagedBean(name="contragent")
@ViewScoped
public class ContragentHolder extends AbstractDocumentHolderBean<Contragent, Integer> implements Serializable {
    private static final Logger logger = LoggerFactory.getLogger("CONTRAGENT");

    @Override
    public String delete() {
        super.delete();
        if (this.getDocument().isDeleted()) {
            try {
                FacesContext.getCurrentInstance().getExternalContext().redirect("delete_contragent.xhtml");
            } catch (Exception e) {
                FacesContext.getCurrentInstance().addMessage(null, MSG_ERROR_ON_DELETE);
                logger.error("Error on delete", e);
            }
            return "";
        } else {
            return "";
        }
    }

    @Override
    protected boolean deleteDocument() {
        try {
            Contragent contragent = getDocument();
            boolean hasDocuments;
            final Map<String, Object> in_map = new HashMap<String, Object>();
            in_map.put("contragent", contragent);
            List<IncomingDocument> incomingDocuments = sessionManagement.getDAO(IncomingDocumentDAOImpl.class, INCOMING_DOCUMENT_FORM_DAO).findAllDocuments(in_map, false, true, 0, -1);
            if (incomingDocuments.isEmpty()) {
                List<RequestDocument> requestDocuments = sessionManagement.getDAO(RequestDocumentDAOImpl.class, REQUEST_DOCUMENT_FORM_DAO).findAllDocuments(in_map, false, true, 0, -1);
                if (requestDocuments.isEmpty()) {
                    in_map.clear();
                    final List<Contragent> recipients = new ArrayList<Contragent>();
                    recipients.add(contragent);
                    in_map.put("recipientContragents", recipients);
                    List<OutgoingDocument> outgoingDocuments = sessionManagement.getDAO(OutgoingDocumentDAOImpl.class, OUTGOING_DOCUMENT_FORM_DAO).findAllDocuments(in_map, false, true, 0, -1);
                    hasDocuments = !outgoingDocuments.isEmpty();
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
                    FacesContext.getCurrentInstance().addMessage(null, MSG_CANT_DELETE);
                }
                return true;
            } else {
                FacesContext.getCurrentInstance().addMessage(null, MSG_CANT_DELETE_CONTRAGENT_DOCUMENTS_EXISTS);
            }
        } catch (Exception e) {
            logger.error("Error on deleteDocument", e);
            FacesContext.getCurrentInstance().addMessage(null, MSG_INTERNAL_ERROR);
        }
        return false;
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
            FacesContext.getCurrentInstance().addMessage(null, MSG_ERROR_ON_INITIALIZE);
            logger.error("initializeError", e);
        }
    }

    @Override
    protected void initNewDocument() {
        final Contragent contragent = new Contragent();
        contragent.setDeleted(false);
        setDocument(contragent);
    }

    @Override
    protected boolean saveDocument() {
        try {
            Contragent contragent = sessionManagement.getDAO(ContragentDAOHibernate.class, CONTRAGENT_DAO).save(getDocument());
            if (contragent == null) {
                FacesContext.getCurrentInstance().addMessage(null, MSG_CANT_SAVE);
                return false;
            }
            return true;
        } catch (Exception e) {
            logger.error("Error on save", e);
            FacesContext.getCurrentInstance().addMessage(null, MSG_ERROR_ON_SAVE);
            return false;
        }
    }

    @Override
    protected boolean saveNewDocument() {
        try {
            final Contragent contragent = sessionManagement.getDAO(ContragentDAOHibernate.class, CONTRAGENT_DAO).save(getDocument());
            if (contragent == null) {
                FacesContext.getCurrentInstance().addMessage(null, MSG_CANT_SAVE);
                return false;
            }
            return true;
        } catch (Exception e) {
            logger.error("Error on save New", e);
            FacesContext.getCurrentInstance().addMessage(null, MSG_ERROR_ON_SAVE_NEW);
            return false;
        }
    }


    @Inject
    @Named("sessionManagement")
    private transient SessionManagementBean sessionManagement;

    private static final long serialVersionUID = 4716264614655470705L;
}