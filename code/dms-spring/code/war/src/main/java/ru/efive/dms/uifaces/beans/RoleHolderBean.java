package ru.efive.dms.uifaces.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import ru.efive.sql.dao.user.RoleDAOHibernate;
import ru.efive.sql.entity.enums.RoleType;
import ru.efive.sql.entity.user.Role;
import ru.efive.dms.util.ApplicationHelper;
import ru.efive.uifaces.bean.AbstractDocumentHolderBean;
import ru.efive.uifaces.bean.FromStringConverter;

@Named("role")
@ConversationScoped
public class RoleHolderBean extends AbstractDocumentHolderBean<Role, Integer> implements Serializable {

	@Override
	protected boolean deleteDocument() {
		boolean result = false;
		try {
			sessionManagement.getDAO(RoleDAOHibernate.class, ApplicationHelper.ROLE_DAO).delete(getDocument());
			result = true;
		}
		catch (Exception e) {
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
		setDocument(sessionManagement.getDAO(RoleDAOHibernate.class, ApplicationHelper.ROLE_DAO).get(id));
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
			Role role = sessionManagement.getDAO(RoleDAOHibernate.class, ApplicationHelper.ROLE_DAO).update(getDocument());
			if (role == null) {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
						FacesMessage.SEVERITY_ERROR,
						"Невозможно сохранить документ. Попробуйте повторить позже.", ""));
			}
			else {
				setDocument(role);
				result = true;
			}
		}
		catch (Exception e) {
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
			Role role = sessionManagement.getDAO(RoleDAOHibernate.class, ApplicationHelper.ROLE_DAO).save(getDocument());
			if (role == null) {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
						FacesMessage.SEVERITY_ERROR,
						"Невозможно сохранить документ. Попробуйте повторить позже.", ""));
			}
			else {
				setDocument(role);
				result = true;
			}
		}
		catch (Exception e) {
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
		roleList.markNeedRefresh();
        return super.doAfterCreate();
    }
    
    @Override
    protected String doAfterDelete() {
    	roleList.markNeedRefresh();
        return super.doAfterDelete();
    }
    
    @Override
    protected String doAfterSave() {
    	roleList.markNeedRefresh();
        return super.doAfterSave();
    }
    
    public List<RoleType> getTypes() {
    	List<RoleType> result = new ArrayList<RoleType>();
    	for (RoleType type: RoleType.values()) {
    		result.add(type);
    	}
    	return result;
    }
	
    
    @Inject @Named("sessionManagement")
	SessionManagementBean sessionManagement = new SessionManagementBean();
	@Inject @Named("roleList")
	RoleListHolderBean roleList = new RoleListHolderBean();
	
	private static final long serialVersionUID = 5947443099767481905L;
}