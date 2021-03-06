package ru.efive.dms.uifaces.beans.contragent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentHolderBean;
import ru.entity.model.document.IncomingDocument;
import ru.entity.model.document.OutgoingDocument;
import ru.entity.model.document.RequestDocument;
import ru.entity.model.referenceBook.Contragent;
import ru.hitsl.sql.dao.ContragentDAOHibernate;
import ru.hitsl.sql.dao.IncomingDocumentDAOImpl;
import ru.hitsl.sql.dao.OutgoingDocumentDAOImpl;
import ru.hitsl.sql.dao.RequestDocumentDAOImpl;
import ru.hitsl.sql.dao.util.DocumentSearchMapKeys;

import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.efive.dms.uifaces.beans.utils.MessageHolder.*;
import static ru.hitsl.sql.dao.util.ApplicationDAONames.*;

@Named("contragent")
@ViewScoped
public class ContragentHolder extends AbstractDocumentHolderBean<Contragent>  {
    private static final Logger logger = LoggerFactory.getLogger("CONTRAGENT");

    @Override
    public boolean doAfterDelete() {
        try {
            FacesContext.getCurrentInstance().getExternalContext().redirect("delete_contragent.xhtml");
        } catch (IOException e){
            logger.error("Error on redirect", e);
            return false;
        }
        return true;
    }

    @Override
    protected boolean deleteDocument() {
        try {
            Contragent contragent = getDocument();
            boolean hasDocuments;
            final Map<String, Object> in_map = new HashMap<>();
            in_map.put(DocumentSearchMapKeys.CONTRAGENT_KEY, contragent);
            List<IncomingDocument> incomingDocuments = sessionManagement.getDAO(IncomingDocumentDAOImpl.class, INCOMING_DOCUMENT_FORM_DAO).getDocumentListByFilters(sessionManagement.getAuthData(), null, in_map, null, true, 0, -1, false, false);
            if (incomingDocuments.isEmpty()) {
                List<RequestDocument> requestDocuments = sessionManagement.getDAO(RequestDocumentDAOImpl.class, REQUEST_DOCUMENT_FORM_DAO).getDocumentListByFilters(
                        sessionManagement.getAuthData(),
                        null,
                        in_map,
                        null,
                        true,
                        0, -1, false, false);
                if (requestDocuments.isEmpty()) {
                    List<OutgoingDocument> outgoingDocuments = sessionManagement.getDAO(OutgoingDocumentDAOImpl.class, OUTGOING_DOCUMENT_FORM_DAO).getDocumentListByFilters(sessionManagement.getAuthData(), null, in_map, null, true, 0, -1, false, false);
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
                    addMessage(null, MSG_CANT_DELETE);
                    return false;
                }
                return true;
            } else {
                addMessage(null, MSG_CANT_DELETE_CONTRAGENT_DOCUMENTS_EXISTS);
            }
        } catch (Exception e) {
            logger.error("Error on deleteDocument", e);
            FacesContext.getCurrentInstance().addMessage(null, MSG_INTERNAL_ERROR);
        }
        return false;
    }

    @Override
    protected void initDocument(Integer id) {
        try {
            setDocument(sessionManagement.getDAO(ContragentDAOHibernate.class, CONTRAGENT_DAO).getItemById(id));
            if (getDocument() == null) {
              setDocumentNotFound();
            }
        } catch (Exception e) {
            addMessage(null, MSG_ERROR_ON_INITIALIZE);
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
               addMessage(null, MSG_CANT_SAVE);
                return false;
            }
            return true;
        } catch (Exception e) {
            logger.error("Error on save", e);
            addMessage(null, MSG_ERROR_ON_SAVE);
            return false;
        }
    }

    @Override
    protected boolean saveNewDocument() {
        try {
            final Contragent contragent = sessionManagement.getDAO(ContragentDAOHibernate.class, CONTRAGENT_DAO).save(getDocument());
            if (contragent == null) {
               addMessage(null, MSG_CANT_SAVE);
                return false;
            }
            return true;
        } catch (Exception e) {
            logger.error("Error on save New", e);
            addMessage(null, MSG_ERROR_ON_SAVE_NEW);
            return false;
        }
    }


    @Inject
    @Named("sessionManagement")
    private transient SessionManagementBean sessionManagement;

    private static final long serialVersionUID = 4716264614655470705L;
}