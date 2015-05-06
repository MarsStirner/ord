package ru.efive.dms.uifaces.beans;

import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.efive.dms.uifaces.beans.dialogs.AbstractDialog;
import ru.efive.dms.uifaces.beans.dialogs.MultipleUserDialogHolder;
import ru.efive.dms.uifaces.beans.utils.MessageHolder;
import ru.efive.sql.dao.user.GroupDAOHibernate;
import ru.efive.uifaces.bean.AbstractDocumentHolderBean;
import ru.efive.uifaces.bean.FromStringConverter;
import ru.entity.model.user.Group;
import ru.entity.model.user.User;

import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;

import static ru.efive.dms.util.ApplicationDAONames.GROUP_DAO;

@Named("group")
@ViewScoped
public class GroupHolderBean extends AbstractDocumentHolderBean<Group, Integer> implements Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger("GROUP");

    @Override
    protected boolean deleteDocument() {
        try {
            getDocument().setDeleted(true);
            sessionManagement.getDAO(GroupDAOHibernate.class, GROUP_DAO).update(getDocument());
            FacesContext.getCurrentInstance().getExternalContext().redirect("deleted_group.xhtml");
            return true;
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, MessageHolder.MSG_ERROR_ON_DELETE);
            LOGGER.error("Error on delete:", e);
        }
        return false;
    }

    @Override
    protected Integer getDocumentId() {
        return getDocument().getId();
    }

    @Override
    protected FromStringConverter<Integer> getIdConverter() {
        return FromStringConverter.INTEGER_CONVERTER;
    }

    @Override
    protected void initDocument(Integer id) {
        setDocument(sessionManagement.getDAO(GroupDAOHibernate.class, GROUP_DAO).getItemById(id));
        if (getDocument() == null) {
            setState(STATE_NOT_FOUND);
        }
    }

    @Override
    protected void initNewDocument() {
        Group group = new Group();
        setDocument(group);
    }

    @Override
    protected boolean saveDocument() {
        try {
            Group group = sessionManagement.getDAO(GroupDAOHibernate.class, GROUP_DAO).update(getDocument());
            if (group == null) {
                FacesContext.getCurrentInstance().addMessage(null, MessageHolder.MSG_CANT_SAVE);
            } else {
                setDocument(group);
                return true;
            }
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, MessageHolder.MSG_ERROR_ON_SAVE);
            LOGGER.error("Error on save:", e);
        }
        return false;
    }

    @Override
    protected boolean saveNewDocument() {
        try {
            Group group = sessionManagement.getDAO(GroupDAOHibernate.class, GROUP_DAO).save(getDocument());
            if (group == null) {
                FacesContext.getCurrentInstance().addMessage(null, MessageHolder.MSG_CANT_SAVE);
            } else {
                setDocument(group);
                return true;
            }
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, MessageHolder.MSG_ERROR_ON_SAVE_NEW);
            LOGGER.error("Error on saveNew:", e);
        }
        return false;
    }

    @Inject
    @Named("sessionManagement")
    private SessionManagementBean sessionManagement;

    // Выбора исполнителей /////////////////////////////////////////////////////////////////////////////////////////////
    public void chooseMembers() {
        final List<User> preselected = getDocument().getMembersList();
        if (preselected != null && !preselected.isEmpty()) {
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(MultipleUserDialogHolder.DIALOG_SESSION_KEY, preselected);
        }
        RequestContext.getCurrentInstance().openDialog("/dialogs/selectMultipleUserDialog.xhtml", AbstractDialog.getViewParams(), null);
    }

    public void onMembersChosen(SelectEvent event) {
        final List<User> selected = (List<User>) event.getObject();
        if(selected != null && !selected.isEmpty()) {
            getDocument().setMembers(new HashSet<User>(selected));
        }  else {
            getDocument().getMembers().clear();
        }
        LOGGER.info("Choose users From Dialog \'{}\'", selected != null ? selected : "#NOTSET");
    }


}