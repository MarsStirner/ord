package ru.efive.dms.uifaces.beans.contragent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentHolderBean;
import ru.efive.dms.uifaces.beans.annotations.ViewScopedController;
import ru.efive.dms.util.message.MessageUtils;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static ru.efive.dms.util.message.MessageHolder.MSG_CANT_DELETE_CONTRAGENT_DOCUMENTS_EXISTS;

@ViewScopedController("contragent")
public class ContragentHolder extends AbstractDocumentHolderBean<Contragent, ContragentDao> {

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
    public ContragentHolder(@Qualifier("contragentDao") final ContragentDao dao,
                            @Qualifier("authData") final AuthorizationData authData) {
        super(dao, authData);
    }

    @Override
    public boolean afterDelete(Contragent document, AuthorizationData authData) {
        try {
            FacesContext.getCurrentInstance().getExternalContext().redirect("delete_contragent.xhtml");
        } catch (IOException e) {
            log.error("Error on redirect", e);
            return false;
        }
        return true;
    }

    @Override
    public boolean beforeDelete(Contragent document, AuthorizationData authData) {
        final Map<String, Object> in_map = Collections.singletonMap(DocumentSearchMapKeys.CONTRAGENT_KEY, document);
        final List<IncomingDocument> incomingDocuments = incomingDocumentDao.getItems(authData, null, in_map, null, true, 0, -1, false, false);
        if (!incomingDocuments.isEmpty()) {
            MessageUtils.addMessage(MSG_CANT_DELETE_CONTRAGENT_DOCUMENTS_EXISTS);
            return false;
        }
        final List<RequestDocument> requestDocuments = requestDocumentDao.getItems(
                authData,
                null,
                in_map,
                null,
                true,
                0, -1, false, false);

        if (!requestDocuments.isEmpty()) {
            MessageUtils.addMessage(MSG_CANT_DELETE_CONTRAGENT_DOCUMENTS_EXISTS);
            return false;
        }

        final List<OutgoingDocument> outgoingDocuments = outgoingDocumentDao.getItems(authData, null, in_map, null, true, 0, -1, false, false);
        if (!outgoingDocuments.isEmpty()) {
            MessageUtils.addMessage(MSG_CANT_DELETE_CONTRAGENT_DOCUMENTS_EXISTS);
            return false;
        }
        return true;
    }

    @Override
    protected Contragent newModel(AuthorizationData authData) {
        final Contragent result = new Contragent();
        result.setDeleted(false);
        return result;
    }

}