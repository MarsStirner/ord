package ru.efive.dms.uifaces.beans.contragent;

import com.github.javaplugs.jsf.SpringScopeView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentHolderBean;
import ru.entity.model.document.IncomingDocument;
import ru.entity.model.document.OutgoingDocument;
import ru.entity.model.document.RequestDocument;
import ru.entity.model.referenceBook.Contragent;
import ru.hitsl.sql.dao.interfaces.document.IncomingDocumentDao;
import ru.hitsl.sql.dao.interfaces.document.OutgoingDocumentDao;
import ru.hitsl.sql.dao.interfaces.document.RequestDocumentDao;
import ru.hitsl.sql.dao.interfaces.referencebook.ContragentDao;
import ru.hitsl.sql.dao.util.AuthorizationData;
import ru.hitsl.sql.dao.util.DocumentSearchMapKeys;

import javax.faces.context.FacesContext;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.efive.dms.uifaces.beans.utils.MessageHolder.*;

@Controller("contragent")
@SpringScopeView
public class ContragentHolder extends AbstractDocumentHolderBean<Contragent> {
    private static final Logger logger = LoggerFactory.getLogger("CONTRAGENT");

    @Autowired
    @Qualifier("contragentDao")
    private ContragentDao contragentDao;
    @Autowired
    @Qualifier("incomingDocumentDao")
    private IncomingDocumentDao incomingDocumentDao;
    @Autowired
    @Qualifier("requestDocumentDao")
    private RequestDocumentDao requestDocumentDao;
    @Autowired
    @Qualifier("outgoingDocumentDao")
    private OutgoingDocumentDao outgoingDocumentDao;
    @Autowired
    @Qualifier("authData")
    private AuthorizationData authData;

    @Override
    public boolean doAfterDelete() {
        try {
            FacesContext.getCurrentInstance().getExternalContext().redirect("delete_contragent.xhtml");
        } catch (IOException e) {
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
            List<IncomingDocument> incomingDocuments = incomingDocumentDao.getItems(authData, null, in_map, null, true, 0, -1, false, false);
            if (incomingDocuments.isEmpty()) {
                List<RequestDocument> requestDocuments = requestDocumentDao.getItems(
                        authData,
                        null,
                        in_map,
                        null,
                        true,
                        0, -1, false, false);
                if (requestDocuments.isEmpty()) {
                    List<OutgoingDocument> outgoingDocuments = outgoingDocumentDao.getItems(authData, null, in_map, null, true, 0, -1, false, false);
                    hasDocuments = !outgoingDocuments.isEmpty();
                } else {
                    hasDocuments = true;
                }
            } else {
                hasDocuments = true;
            }
            if (!hasDocuments) {
                contragent.setDeleted(true);
                contragent = contragentDao.save(contragent);
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
            setDocument(contragentDao.get(id));
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
            Contragent contragent = contragentDao.save(getDocument());
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
            final Contragent contragent = contragentDao.save(getDocument());
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

}