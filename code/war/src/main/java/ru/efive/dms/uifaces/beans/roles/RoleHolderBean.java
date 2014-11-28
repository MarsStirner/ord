package ru.efive.dms.uifaces.beans.roles;

import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.sql.dao.user.RoleDAOHibernate;
import ru.efive.uifaces.bean.AbstractDocumentHolderBean;
import ru.efive.uifaces.bean.FromStringConverter;
import ru.entity.model.enums.RoleType;
import ru.entity.model.user.Role;

import javax.enterprise.context.ConversationScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static ru.efive.dms.uifaces.beans.utils.MessageHolder.*;
import static ru.efive.dms.util.ApplicationDAONames.ROLE_DAO;

@Named("role")
@ConversationScoped
public class RoleHolderBean extends AbstractDocumentHolderBean<Role, Integer> implements Serializable {

    @Override
    protected boolean deleteDocument() {
        boolean result = false;
        try {
            sessionManagement.getDAO(RoleDAOHibernate.class, ROLE_DAO).delete(getDocument());
            result = true;
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, MSG_CANT_DELETE);
        }
        return result;
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
        setDocument(sessionManagement.getDAO(RoleDAOHibernate.class, ROLE_DAO).get(id));
        if (getDocument() == null) {
            setState(STATE_NOT_FOUND);
        }
    }

    @Override
    protected void initNewDocument() {
        setDocument(new Role());
    }

    @Override
    protected boolean saveDocument() {
        boolean result = false;
        try {
            Role role = sessionManagement.getDAO(RoleDAOHibernate.class, ROLE_DAO).update(getDocument());
            if (role == null) {
                FacesContext.getCurrentInstance().addMessage(null, MSG_CANT_SAVE);
            } else {
                setDocument(role);
                result = true;
            }
        } catch (Exception e) {
            result = false;
            FacesContext.getCurrentInstance().addMessage(null, MSG_ERROR_ON_SAVE);
            e.printStackTrace();
        }
        return result;
    }

    @Override
    protected boolean saveNewDocument() {
        boolean result = false;
        try {
            Role role = sessionManagement.getDAO(RoleDAOHibernate.class, ROLE_DAO).save(getDocument());
            if (role == null) {
                FacesContext.getCurrentInstance().addMessage(null,MSG_CANT_SAVE);
            } else {
                setDocument(role);
                result = true;
            }
        } catch (Exception e) {
            result = false;
            FacesContext.getCurrentInstance().addMessage(null, MSG_ERROR_ON_SAVE_NEW);
            e.printStackTrace();
        }
        return result;
    }

    public List<RoleType> getTypes() {
        List<RoleType> result = new ArrayList<RoleType>();
        Collections.addAll(result, RoleType.values());
        return result;
    }


    @Inject
    @Named("sessionManagement")
    SessionManagementBean sessionManagement = new SessionManagementBean();


    private static final long serialVersionUID = 5947443099767481905L;
}