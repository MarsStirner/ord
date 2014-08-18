package ru.efive.dms.uifaces.beans;

import java.io.Serializable;
import java.util.HashSet;

import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import ru.efive.sql.dao.user.GroupDAOHibernate;
import ru.efive.sql.dao.user.UserDAOHibernate;
import ru.efive.sql.entity.user.Group;
import ru.efive.dms.uifaces.beans.user.UserListSelectModalBean;
import ru.efive.dms.util.ApplicationHelper;
import ru.efive.sql.entity.user.User;
import ru.efive.uifaces.bean.AbstractDocumentHolderBean;
import ru.efive.uifaces.bean.FromStringConverter;

@Named("group")
@ConversationScoped
public class GroupHolderBean extends AbstractDocumentHolderBean<Group, Integer> implements Serializable {

    @Override
    protected boolean deleteDocument() {
        boolean result = false;
        try {
            sessionManagement.getDAO(GroupDAOHibernate.class, ApplicationHelper.GROUP_DAO).delete(getDocument());
            FacesContext.getCurrentInstance().getExternalContext().redirect("deleted_group.xhtml");
            result = true;
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Невозможно удалить документ. Попробуйте повторить позже.", ""));
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
        setDocument(sessionManagement.getDAO(GroupDAOHibernate.class, ApplicationHelper.GROUP_DAO).get(id));
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
            Group group = sessionManagement.getDAO(GroupDAOHibernate.class, ApplicationHelper.GROUP_DAO).update(getDocument());

            if (group == null) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_ERROR,
                        "Невозможно сохранить документ. Попробуйте повторить позже.", ""));
            } else {
                setDocument(group);
                result = true;
            }
        } catch (Exception e) {
            result = false;
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Ошибка при сохранении документа.", ""));
            e.printStackTrace();
        }
        return result;
    }

    @Override
    protected boolean saveNewDocument() {
        boolean result = false;
        try {
            Group group = sessionManagement.getDAO(GroupDAOHibernate.class, ApplicationHelper.GROUP_DAO).save(getDocument());
            if (group == null) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_ERROR,
                        "Невозможно сохранить документ. Попробуйте повторить позже.", ""));
            } else {
                setDocument(group);
                result = true;
            }
        } catch (Exception e) {
            result = false;
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Ошибка при сохранении документа.", ""));
            e.printStackTrace();
        }
        return result;
    }

    @Override
    protected String doAfterCreate() {
        //markNeedRefresh();
        return super.doAfterCreate();
    }

    @Override
    protected String doAfterDelete() {
        //userList.markNeedRefresh();
        return super.doAfterDelete();
    }

    @Override
    protected String doAfterSave() {
        //userList.markNeedRefresh();
        return super.doAfterSave();
    }

    protected boolean isCurrentUserDocEditor() {
        //Group group=sessionManagement.getLoggedUser();
        //User in_doc=getDocument();
        //Group in_group=sessionManagement.getDAO(GroupDAOHibernate.class, ApplicationHelper.GROUP_DAO).findByLoginAndPassword(user.getLogin(),user.getPassword());

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
            getDocument().setMembers(new HashSet(getUsers()));
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