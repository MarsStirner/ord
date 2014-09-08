package ru.efive.dms.uifaces.beans.user;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import ru.efive.sql.dao.user.UserDAOHibernate;
import ru.efive.sql.entity.enums.RoleType;
import ru.efive.sql.entity.user.Role;
import ru.efive.sql.entity.user.User;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.util.ApplicationHelper;
import ru.efive.uifaces.bean.AbstractDocumentHolderBean;
import ru.efive.uifaces.bean.FromStringConverter;

@Named("user")
@ConversationScoped
public class UserHolderBean extends AbstractDocumentHolderBean<User, Integer> implements Serializable {

    public boolean changeFired(){
        final User user = getDocument();
        final Date now = new Date();
        if(user.isFired()){
            //Восстанавливаем уволенного сотрудника
           user.hire(now);
        }  else {
            //Увольняем сотрудника
            user.fire(now);
        }
        final User alteredUser = sessionManagement.getDAO(UserDAOHibernate.class, ApplicationHelper.USER_DAO).save(user);
        setDocument(alteredUser);
        return alteredUser.isFired();
    }

    @Override
    protected boolean deleteDocument() {
        boolean result = false;
        try {
            sessionManagement.getDAO(UserDAOHibernate.class, ApplicationHelper.USER_DAO).delete(getDocument());
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
        setDocument(sessionManagement.getDAO(UserDAOHibernate.class, ApplicationHelper.USER_DAO).getUser(id));
        if (getDocument() == null) {
            setState(STATE_NOT_FOUND);
        }
    }

    @Override
    protected void initNewDocument() {
        User user = new User();
        user.setCreated(Calendar.getInstance(ApplicationHelper.getLocale()).getTime());
        user.setDeleted(false);
        setDocument(user);
    }

    @Override
    protected boolean saveDocument() {
        boolean result = false;
        try {
            User user = sessionManagement.getDAO(UserDAOHibernate.class, ApplicationHelper.USER_DAO).update(getDocument());
            /*Set<Group> groups=user.getGroups();
               if(groups!=null){
                   for(Group group:groups){
                       if(group!=null){
                           System.out.println("group->"+group.getDescription());
                       }
                   }
               }else{
                   System.out.println("null groups");
               }*/
            if (user == null) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_ERROR,
                        "Невозможно сохранить документ. Попробуйте повторить позже.", ""));
            } else {
                setDocument(user);
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
            User user = sessionManagement.getDAO(UserDAOHibernate.class, ApplicationHelper.USER_DAO).save(getDocument());
            if (user == null) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_ERROR,
                        "Невозможно сохранить документ. Попробуйте повторить позже.", ""));
            } else {
                setDocument(user);
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
        userList.markNeedRefresh();
        return super.doAfterCreate();
    }

    @Override
    protected String doAfterDelete() {
        userList.markNeedRefresh();
        return super.doAfterDelete();
    }

    @Override
    protected String doAfterSave() {
        userList.markNeedRefresh();
        return super.doAfterSave();
    }

    protected boolean isCurrentUserDocEditor() {
        User in_user = sessionManagement.getLoggedUser();
        //User in_doc=getDocument();
       // User in_user = sessionManagement.getDAO(UserDAOHibernate.class, ApplicationHelper.USER_DAO).findByLoginAndPassword(user.getLogin(), user.getPassword());

        if (in_user != null) {
            for (Role in_role : in_user.getRoleList()) {
                if (in_role.getRoleType().name().equals(RoleType.ADMINISTRATOR.name())) {
                    return true;
                }
            }
        }
        return false;
    }

    public String getRequisitesTabHeader() {
        return "<span><span>Реквизиты</span></span>";
    }

    public boolean isRequisitesTabSelected() {
        return isRequisitesTabSelected;
    }

    public void setRequisitesTabSelected(boolean isRequisitesTabSelected) {
        this.isRequisitesTabSelected = isRequisitesTabSelected;
    }

    public String getAccessTabHeader() {
        return "<span><span>Права доступа|допуска</span></span>";
    }

    public boolean isAccessTabSelected() {
        return isAccessTabSelected;
    }

    public void setAccessTabSelected(boolean isAccessTabSelected) {
        this.isAccessTabSelected = isAccessTabSelected;
    }

    private boolean isRequisitesTabSelected = true;
    private boolean isAccessTabSelected = false;

    @Inject
    @Named("sessionManagement")
    SessionManagementBean sessionManagement = new SessionManagementBean();
    @Inject
    @Named("userList")
    UserListHolderBean userList = new UserListHolderBean();

    private static final long serialVersionUID = 5947443099767481905L;
}