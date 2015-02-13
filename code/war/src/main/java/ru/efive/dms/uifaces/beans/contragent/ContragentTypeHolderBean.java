package ru.efive.dms.uifaces.beans.contragent;

/**
 * Author: Upatov Egor <br>
 * Date: 12.02.2015, 20:35 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.efive.crm.dao.ContragentDAOHibernate;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.sql.dao.RbContragentTypeDAOImpl;
import ru.efive.uifaces.bean.AbstractDocumentHolderBean;
import ru.efive.uifaces.bean.FromStringConverter;
import ru.entity.model.crm.Contragent;
import ru.entity.model.crm.ContragentType;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import static ru.efive.dms.uifaces.beans.utils.MessageHolder.*;
import static ru.efive.dms.util.ApplicationDAONames.CONTRAGENT_DAO;
import static ru.efive.dms.util.ApplicationDAONames.RB_CONTRAGENT_TYPE_DAO;

@ManagedBean(name = "contragentType")
@ViewScoped
public class ContragentTypeHolderBean extends AbstractDocumentHolderBean<ContragentType, Integer> implements Serializable{

    private static final Logger logger = LoggerFactory.getLogger("RB_CONTRAGENT_TYPE");

    //private List<Contragent> contragentList;

    @Inject
    @Named("sessionManagement")
    private transient SessionManagementBean sessionManagement;

    @Override
    protected boolean deleteDocument() {
        final ContragentType document = getDocument();
        final List<Contragent> contragentsWithThisContragentType =  sessionManagement.getDAO(ContragentDAOHibernate.class, CONTRAGENT_DAO).getByType(document);
        if(contragentsWithThisContragentType.isEmpty()){
            document.setDeleted(true);
            final ContragentType afterDelete = sessionManagement.getDAO(RbContragentTypeDAOImpl.class, RB_CONTRAGENT_TYPE_DAO).save(document);
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
        //this.contragentList = new ArrayList<Contragent>(0);
    }

    @Override
    protected void initDocument(Integer id) {
        try {
            final ContragentType document = sessionManagement.getDAO(RbContragentTypeDAOImpl.class, RB_CONTRAGENT_TYPE_DAO).get(id);
            if (document == null) {
                setState(STATE_NOT_FOUND);
            } else {
                setDocument(document);
                //contragentList = sessionManagement.getDAO(ContragentDAOHibernate.class, CONTRAGENT_DAO).getByType(document);
            }
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, MSG_ERROR_ON_INITIALIZE);
            logger.error("initializeError", e);
        }
    }

    @Override
    protected boolean saveNewDocument() {
        try {
            final ContragentType document = sessionManagement.getDAO(RbContragentTypeDAOImpl.class, RB_CONTRAGENT_TYPE_DAO).save(getDocument());
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
            ContragentType document = sessionManagement.getDAO(RbContragentTypeDAOImpl.class, RB_CONTRAGENT_TYPE_DAO).save(getDocument());
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


//    public List<Contragent> getContragentList() {
//        return contragentList;
//    }
//
//    public void setContragentList(List<Contragent> contragentList) {
//        this.contragentList = contragentList;
//    }
}
