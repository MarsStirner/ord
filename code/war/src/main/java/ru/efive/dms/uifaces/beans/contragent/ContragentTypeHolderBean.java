package ru.efive.dms.uifaces.beans.contragent;

/**
 * Author: Upatov Egor <br>
 * Date: 12.02.2015, 20:35 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.uifaces.bean.AbstractDocumentHolderBean;
import ru.efive.uifaces.bean.FromStringConverter;
import ru.entity.model.referenceBook.Contragent;
import ru.entity.model.referenceBook.ContragentType;
import ru.hitsl.sql.dao.ContragentDAOHibernate;
import ru.hitsl.sql.dao.referenceBook.ContragentTypeDAOImpl;

import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import static ru.efive.dms.uifaces.beans.utils.MessageHolder.*;
import static ru.hitsl.sql.dao.util.ApplicationDAONames.CONTRAGENT_DAO;
import static ru.hitsl.sql.dao.util.ApplicationDAONames.RB_CONTRAGENT_TYPE_DAO;

@Named("contragentType")
@ViewScoped
public class ContragentTypeHolderBean extends AbstractDocumentHolderBean<ContragentType, Integer> implements Serializable{

    private static final Logger logger = LoggerFactory.getLogger("RB_CONTRAGENT_TYPE");

    @Inject
    @Named("sessionManagement")
    private transient SessionManagementBean sessionManagement;

    @Override
    protected boolean deleteDocument() {
        final ContragentType document = getDocument();
        final List<Contragent> contragentsWithThisContragentType =  sessionManagement.getDAO(ContragentDAOHibernate.class, CONTRAGENT_DAO).getByType(document);
        if(contragentsWithThisContragentType.isEmpty()){
            document.setDeleted(true);
            final ContragentType afterDelete = sessionManagement.getDAO(ContragentTypeDAOImpl.class, RB_CONTRAGENT_TYPE_DAO).save(document);
            if(afterDelete != null){
                try {
                    FacesContext.getCurrentInstance().getExternalContext().redirect("../delete_document.xhtml");
                } catch (IOException e) {
                    logger.error("Error in redirect ", e);
                }
                return true;
            } else{
                FacesContext.getCurrentInstance().addMessage(null, MSG_CANT_DELETE);
                return false;
            }
        } else {
            FacesContext.getCurrentInstance().addMessage(null, MSG_RB_CONTRAGENT_TYPE_IS_USED_BY_SOME_CONTRAGENTS);
            return false;
        }
    }

    @Override
    protected Integer getDocumentId() {
        return getDocument() == null ? null : getDocument().getId();
    }

    @Override
    protected void initNewDocument() {
        final ContragentType document = new ContragentType();
        document.setDeleted(false);
        setDocument(document);
    }

    @Override
    protected void initDocument(Integer id) {
        try {
            final ContragentType document = sessionManagement.getDAO(ContragentTypeDAOImpl.class, RB_CONTRAGENT_TYPE_DAO).get(id);
            if (document == null) {
                setState(STATE_NOT_FOUND);
            } else {
                setDocument(document);
            }
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, MSG_ERROR_ON_INITIALIZE);
            logger.error("initializeError", e);
        }
    }

    @Override
    protected boolean saveNewDocument() {
        try {
            final ContragentType document = sessionManagement.getDAO(ContragentTypeDAOImpl.class, RB_CONTRAGENT_TYPE_DAO).save(getDocument());
            if (document == null) {
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

    @Override
    protected boolean saveDocument() {
        try {
            ContragentType document = sessionManagement.getDAO(ContragentTypeDAOImpl.class, RB_CONTRAGENT_TYPE_DAO).save(getDocument());
            if (document == null) {
                FacesContext.getCurrentInstance().addMessage(null, MSG_CANT_SAVE);
                return false;
            }
            setDocument(document);
            return true;
        } catch (Exception e) {
            logger.error("Error on save", e);
            FacesContext.getCurrentInstance().addMessage(null, MSG_ERROR_ON_SAVE);
            return false;
        }
    }

    @Override
    protected FromStringConverter<Integer> getIdConverter() {
        return FromStringConverter.INTEGER_CONVERTER;
    }
}
