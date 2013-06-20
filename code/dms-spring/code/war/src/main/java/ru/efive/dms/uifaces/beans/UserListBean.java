package ru.efive.dms.uifaces.beans;

import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import ru.efive.sql.dao.user.UserDAO;
import ru.efive.sql.entity.user.User;
import ru.efive.uifaces.bean.AbstractDocumentListHolderBean;

@Named("employeeList")
@SessionScoped
public class UserListBean extends AbstractDocumentListHolderBean<User> {
	
	@Override
    protected Pagination initPagination() {
        return new Pagination(0, getTotalCount(), 10);
    }
	
	//TODO: filtering
	@Override
	protected int getTotalCount() {
		return new Long(sessionManagement.getDAO(UserDAO.class, "userDao").countDocument(false)).intValue();
	}
	
	//TODO: filtering
	@Override
	protected List<User> loadDocuments() {
		return sessionManagement.getDAO(UserDAO.class, "userDao").findDocuments(
				false, getPagination().getOffset(), getPagination().getPageSize(), "lastName,firstName,middleName", true);
	}
	
	@Override
    public List<User> getDocuments() {
        return super.getDocuments();
    }
	
	public String getFilter() {
		return filter;
	}
	
	public void setFilter(String filter) {
		this.filter = filter;
	}
	
	
	private String filter;
	
	@Inject @Named("sessionManagement")
    private transient SessionManagementBean sessionManagement;
	
	private static final long serialVersionUID = 5788928924371620015L;
}