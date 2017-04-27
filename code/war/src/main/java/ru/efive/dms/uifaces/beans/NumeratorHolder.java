package ru.efive.dms.uifaces.beans;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentHolderBean;
import ru.efive.dms.uifaces.beans.annotations.ViewScopedController;
import ru.entity.model.document.Numerator;

@ViewScopedController("numerator")
public class NumeratorHolder extends AbstractDocumentHolderBean<Numerator> {
    private static final Logger logger = LoggerFactory.getLogger("RB_NUMERATOR");

//    @Autowired
//    Numerat
//
//    @Override
//    protected boolean deleteDocument() {
//        try {
//            final Numerator document = getDocument();
//            document.setDeleted(true);
//            setDocument(sessionManagement.getDAO(NumeratorDAOImpl.class, NUMERATOR_DAO).merge(document));
//            if (!getDocument().isDeleted()) {
//MessageUtils.addMessage(MSG_CANT_DELETE);
//            }
//            return true;
//        } catch (Exception e) {
//            logger.error("CANT_DELETE_NUMERATOR", e);
//MessageUtils.addMessage(MSG_ERROR_ON_DELETE);
//        }
//        return false;
//    }
//
//    @Override
//    protected void initNewDocument() {
//        final Numerator doc = new Numerator();
//        final Date created = LocalDateTime.now();;
//        doc.setCreationDate(created);
//        doc.setAuthor(authData.getAuthorized());
//        doc.setDeleted(false);
//        doc.setValue(0);
//        setDocument(doc);
//    }
//
//    @Override
//    protected void initDocument(Integer id) {
//        try {
//            setDocument(sessionManagement.getDAO(NumeratorDAOImpl.class, NUMERATOR_DAO).findDocumentById(id));
//            if (getDocument() == null) {
//                setState(State.ERROR);
//                MessageUtils.addMessage(MessageKey.ERROR, MessageHolder.MSG_DOCUMENT_NOT_FOUND);
//            }
//        } catch (Exception e) {
//MessageUtils.addMessage(MSG_ERROR_ON_INITIALIZE);
//            logger.error("CANT_INIT_NUMERATOR", e);
//        }
//    }
//
//    @Override
//    protected boolean saveNewDocument() {
//        try {
//            Numerator document = getDocument();
//            document = sessionManagement.getDAO(NumeratorDAOImpl.class, NUMERATOR_DAO).save(document);
//            if (document == null) {
//MessageUtils.addMessage(MSG_CANT_SAVE);
//            } else {
//                return true;
//            }
//        } catch (Exception e) {
//            logger.error("CANT_SAVE_NEW_NUMERATOR", e);
//MessageUtils.addMessage(MSG_ERROR_ON_SAVE_NEW);
//        }
//        return false;
//    }
//
//    @Override
//    protected boolean saveDocument() {
//        try {
//            Numerator document = getDocument();
//            document = sessionManagement.getDAO(NumeratorDAOImpl.class, NUMERATOR_DAO).save(document);
//            if (document == null) {
//MessageUtils.addMessage(MSG_CANT_SAVE);
//            } else {
//                setDocument(document);
//                return true;
//            }
//        } catch (Exception e) {
//            logger.error("CANT_SAVE_NUMERATOR", e);
//MessageUtils.addMessage(MSG_ERROR_ON_SAVE);
//        }
//        return false;
//    }

    @Override
    protected boolean deleteDocument() {
        return false;
    }

    @Override
    protected void initNewDocument() {

    }

    @Override
    protected void initDocument(Integer documentId) {

    }

    @Override
    public boolean saveNewDocument() {
        return false;
    }

    @Override
    protected boolean saveDocument() {
        return false;
    }

    @Override
    public boolean isCanDelete() {
        return false;
    }

    @Override
    public boolean isCanEdit() {
        return isCreateState();
    }
}