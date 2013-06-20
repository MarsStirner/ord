package ru.efive.dms.uifaces.beans;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import ru.efive.dms.dao.RequestDocumentDAOImpl;
import ru.efive.dms.data.RequestDocument;
import ru.efive.uifaces.bean.AbstractDocumentListHolderBean;

@Named("personal_request_documents")
@SessionScoped
public class PersonalRequestDocumentListHolder extends AbstractDocumentListHolderBean<RequestDocument> {

	@Override
	protected Pagination initPagination() {
		return new Pagination(0, getTotalCount(), 100);
	}
	
	@Override
	protected Sorting initSorting() {
		return new Sorting("creationDate,id", false);
    }
	
	@Override
	protected int getTotalCount() {
		int result = 0;
		try {
			return new Long(sessionManagement.getDAO(RequestDocumentDAOImpl.class, ApplicationHelper.REQUEST_DOCUMENT_FORM_DAO).countDraftDocumentsByAuthor(
					sessionManagement.getLoggedUser(), false)).intValue();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	protected List<RequestDocument> loadDocuments() {
		List<RequestDocument> result = new ArrayList<RequestDocument>();
		try {
			result = sessionManagement.getDAO(RequestDocumentDAOImpl.class, ApplicationHelper.REQUEST_DOCUMENT_FORM_DAO).findDraftDocumentsByAuthor(filter, sessionManagement.getLoggedUser(),
					false, getPagination().getOffset(), getPagination().getPageSize(), getSorting().getColumnId(), getSorting().isAsc());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public List<RequestDocument> getDocuments() {
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

	private static final long serialVersionUID = 8535420074467871583L;
}