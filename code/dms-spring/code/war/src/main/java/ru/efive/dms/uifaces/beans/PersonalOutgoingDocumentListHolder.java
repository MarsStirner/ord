package ru.efive.dms.uifaces.beans;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import ru.efive.dms.dao.OutgoingDocumentDAOImpl;
import ru.efive.dms.data.OutgoingDocument;
import ru.efive.uifaces.bean.AbstractDocumentListHolderBean;

@Named("personal_out_documents")
@SessionScoped
public class PersonalOutgoingDocumentListHolder extends AbstractDocumentListHolderBean<OutgoingDocument> {

	@Override
	protected Pagination initPagination() {
		return new Pagination(0, getTotalCount(), 100);
	}

	@Override
	protected Sorting initSorting() {
		return new Sorting("creationDate", false);
	}

	@Override
	protected int getTotalCount() {
		int result = 0;
		try {
			return new Long(sessionManagement.getDAO(OutgoingDocumentDAOImpl.class, ApplicationHelper.OUTGOING_DOCUMENT_FORM_DAO).countDraftDocumentsByAuthor(
					sessionManagement.getLoggedUser(), false)).intValue();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	protected List<OutgoingDocument> loadDocuments() {
		List<OutgoingDocument> result = new ArrayList<OutgoingDocument>();
		try {			
			result = sessionManagement.getDAO(OutgoingDocumentDAOImpl.class, ApplicationHelper.OUTGOING_DOCUMENT_FORM_DAO).findDraftDocumentsByAuthor(filter, sessionManagement.getLoggedUser(),
					false, getPagination().getOffset(), getPagination().getPageSize(), getSorting().getColumnId(), getSorting().isAsc());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public List<OutgoingDocument> getDocuments() {
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