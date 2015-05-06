package ru.efive.dms.uifaces.beans;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.efive.dms.dao.NumeratorDAOImpl;
import ru.efive.uifaces.bean.AbstractDocumentHolderBean;
import ru.efive.uifaces.bean.FromStringConverter;
import ru.entity.model.document.Numerator;
import ru.util.ApplicationHelper;

import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

import static ru.efive.dms.uifaces.beans.utils.MessageHolder.*;
import static ru.efive.dms.util.ApplicationDAONames.NUMERATOR_DAO;

@Named("numerator")
@ViewScoped
public class NumeratorHolder extends AbstractDocumentHolderBean<Numerator, Integer> implements Serializable {
    private static final long serialVersionUID = 4716264614655470705L;

    private static final Logger logger = LoggerFactory.getLogger("RB_NUMERATOR");

    @Inject
    @Named("sessionManagement")
    private transient SessionManagementBean sessionManagement;

    @Override
    public boolean isCanEdit(){
        return isCreateState();
    }

    @Override
    public boolean isCanDelete(){
        return false;
    }

    @Override
    public String delete() {
        String in_result = super.delete();
        if (in_result != null && in_result.equals("delete")) {
            try {
                FacesContext.getCurrentInstance().getExternalContext().redirect("delete_document.xhtml");
            } catch (Exception e) {
                FacesContext.getCurrentInstance().addMessage(null, MSG_CANT_DELETE);
                logger.error("CANT_DELETE_NUMERATOR", e);
            }
        }
        return in_result;
    }

    @Override
    protected boolean deleteDocument() {
        try {
            final Numerator document = getDocument();
            document.setDeleted(true);
            setDocument(sessionManagement.getDAO(NumeratorDAOImpl.class, NUMERATOR_DAO).merge(document));
            if (!getDocument().getDeleted()) {
                FacesContext.getCurrentInstance().addMessage(null, MSG_CANT_DELETE);
            }
            return true;
        } catch (Exception e) {
            logger.error("CANT_DELETE_NUMERATOR", e);
            FacesContext.getCurrentInstance().addMessage(null, MSG_ERROR_ON_DELETE);
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
            setDocument(sessionManagement.getDAO(NumeratorDAOImpl.class, NUMERATOR_DAO).findDocumentById(id));
            if (getDocument() == null) {
                setState(STATE_NOT_FOUND);
            }
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, MSG_ERROR_ON_INITIALIZE);
            logger.error("CANT_INIT_NUMERATOR", e);
        }
    }

    @Override
    protected void initNewDocument() {
        final Numerator doc = new Numerator();
        final Date created = Calendar.getInstance(ApplicationHelper.getLocale()).getTime();
        doc.setCreationDate(created);
        doc.setAuthor(sessionManagement.getLoggedUser());
        doc.setDeleted(false);
        doc.setValue(0);
        setDocument(doc);
    }

    @Override
    protected boolean saveDocument() {
        try {
            Numerator document = getDocument();
            document = sessionManagement.getDAO(NumeratorDAOImpl.class, NUMERATOR_DAO).save(document);
            if (document == null) {
                FacesContext.getCurrentInstance().addMessage(null, MSG_CANT_SAVE);
            } else {
                setDocument(document);
                return true;
            }
        } catch (Exception e) {
            logger.error("CANT_SAVE_NUMERATOR", e);
            FacesContext.getCurrentInstance().addMessage(null, MSG_ERROR_ON_SAVE);
        }
        return false;
    }

    @Override
    protected boolean saveNewDocument() {
        try {
            Numerator document = getDocument();
            document = sessionManagement.getDAO(NumeratorDAOImpl.class, NUMERATOR_DAO).save(document);
            if (document == null) {
                FacesContext.getCurrentInstance().addMessage(null,MSG_CANT_SAVE);
            } else {
                return true;
            }
        } catch (Exception e) {
            logger.error("CANT_SAVE_NEW_NUMERATOR", e);
            FacesContext.getCurrentInstance().addMessage(null, MSG_ERROR_ON_SAVE_NEW);
        }
        return false;
    }
}