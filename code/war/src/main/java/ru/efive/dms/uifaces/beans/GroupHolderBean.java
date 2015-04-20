package ru.efive.dms.uifaces.beans;

import ru.efive.dms.uifaces.beans.user.UserListSelectModalBean;
import ru.efive.dms.uifaces.beans.utils.MessageHolder;
import ru.efive.sql.dao.user.GroupDAOHibernate;
import ru.efive.uifaces.bean.AbstractDocumentHolderBean;
import ru.efive.uifaces.bean.FromStringConverter;
import ru.entity.model.user.Group;
import ru.entity.model.user.User;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.HashSet;

import static ru.efive.dms.util.ApplicationDAONames.GROUP_DAO;

@ManagedBean(name="group")
@ViewScoped
public class GroupHolderBean extends AbstractDocumentHolderBean<Group, Integer> implements Serializable {

    @Override
    protected boolean deleteDocument() {
        boolean result = false;
        try {
            getDocument().setDeleted(true);
            sessionManagement.getDAO(GroupDAOHibernate.class, GROUP_DAO).update(getDocument());
            FacesContext.getCurrentInstance().getExternalContext().redirect("deleted_group.xhtml");
            result = true;
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, MessageHolder.MSG_ERROR_ON_DELETE);
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
        boolean result = false;
        try {
            Group group = sessionManagement.getDAO(GroupDAOHibernate.class, GROUP_DAO).update(getDocument());
            if (group == null) {
                FacesContext.getCurrentInstance().addMessage(null, MessageHolder.MSG_CANT_SAVE);
            } else {
                setDocument(group);
                result = true;
            }
        } catch (Exception e) {
            result = false;
            FacesContext.getCurrentInstance().addMessage(null, MessageHolder.MSG_ERROR_ON_SAVE);
            e.printStackTrace();
        }
        return result;
    }

    @Override
    protected boolean saveNewDocument() {
        boolean result = false;
        try {
            Group group = sessionManagement.getDAO(GroupDAOHibernate.class, GROUP_DAO).save(getDocument());
            if (group == null) {
                FacesContext.getCurrentInstance().addMessage(null, MessageHolder.MSG_CANT_SAVE);
            } else {
                setDocument(group);
                result = true;
            }
        } catch (Exception e) {
            result = false;
            FacesContext.getCurrentInstance().addMessage(null, MessageHolder.MSG_ERROR_ON_SAVE_NEW);
            e.printStackTrace();
        }
        return result;
    }


    protected boolean isCurrentUserDocEditor() {
        //Group group=sessionManagement.getLoggedUser();
        //User in_doc=getDocument();
        //Group in_group=sessionManagement.getDAO(GroupDAOHibernate.class,GROUP_DAO).findByLoginAndPassword(user.getLogin(),user.getPassword());

        //if(in_group!=null){
        //for(Role in_role:in_group.getRoleList()){
        //if(in_role.getRoleType().name().equals(RoleType.ENTERPRISE_ADMINISTRATION.name())){
        //return true;
        //}
        //}
        //}
        return false;
    }

    public UserListSelectModalBean getMembersPickList() {
        return membersPickList;
    }

    private UserListSelectModalBean membersPickList = new UserListSelectModalBean() {

        @Override
        protected void doShow() {
            super.doShow();
            setUsers(getDocument().getMembersList());
        }

        @Override
        protected void doSave() {
            super.doSave();
            getDocument().setMembers(new HashSet<User>(getUsers()));
        }

        @Override
        protected void doHide() {
            super.doHide();
            getUserList().setFilter("");
            getUserList().markNeedRefresh();
            setUsers(null);
        }
    };

    @Inject
    @Named("sessionManagement")
    SessionManagementBean sessionManagement = new SessionManagementBean();

}