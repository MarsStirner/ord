package ru.efive.dms.uifaces.beans;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import ru.efive.sql.dao.user.UserDAOHibernate;
import ru.efive.sql.entity.user.User;
import ru.efive.dms.util.ApplicationHelper;
import ru.efive.uifaces.bean.AbstractDocumentListHolderBean;

@Named("userList")
@SessionScoped
public class UserListHolderBean extends AbstractDocumentListHolderBean<User> {

	@Override
	public Pagination initPagination() {
		return new Pagination(0, getTotalCount(), 100);
	}

	@Override
	protected Sorting initSorting() {
		return new Sorting("lastName,firstName,middleName", true);
    }

	@Override
	protected int getTotalCount() {
		int result = 0;
		try {
			result = new Long(sessionManagement.getDAO(UserDAOHibernate.class, ApplicationHelper.USER_DAO).countUsers(filter, false)).intValue();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	protected List<User> loadDocuments() {
		List<User> result = new ArrayList<User>();
		try {
			result = sessionManagement.getDAO(UserDAOHibernate.class, ApplicationHelper.USER_DAO).findUsers(filter, false,
					getPagination().getOffset(), getPagination().getPageSize(), getSorting().getColumnId(), getSorting().isAsc());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	protected List<User> getAllWithEmptyUser() {
		List<User> result = getDocuments();
		try {
			result = sessionManagement.getDAO(UserDAOHibernate.class, ApplicationHelper.USER_DAO).findUsers(filter, false,
					getPagination().getOffset(), getPagination().getPageSize(), getSorting().getColumnId(), getSorting().isAsc());
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		User empty= new User();
		empty.setLastName("");
		empty.setFirstName("");
		empty.setMiddleName("");
		result.add(0, empty);
		return result;
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

	public String getUserDescription(int id) {
		String description = "";
		try {
			User user = sessionManagement.getDAO(UserDAOHibernate.class, ApplicationHelper.USER_DAO).get(id);
			if (user != null) {
				description = user.getDescriptionShort();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return description;
	}


	private String filter;

	@Inject @Named("sessionManagement")
	SessionManagementBean sessionManagement = new SessionManagementBean();


	private static final long serialVersionUID = 1023130636261147049L;
}