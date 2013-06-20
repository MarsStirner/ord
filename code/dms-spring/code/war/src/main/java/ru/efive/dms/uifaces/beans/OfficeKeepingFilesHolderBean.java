package ru.efive.dms.uifaces.beans;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import ru.efive.dms.dao.OfficeKeepingFileDAOImpl;
import ru.efive.dms.data.OfficeKeepingFile;
import ru.efive.dms.util.ApplicationHelper;
import ru.efive.uifaces.bean.AbstractDocumentListHolderBean;

@Named("officeKeepingFiles")
@SessionScoped
public class OfficeKeepingFilesHolderBean extends AbstractDocumentListHolderBean<OfficeKeepingFile> {
	
	@Override
	public Pagination initPagination() {
		return new Pagination(0, getTotalCount(), 25);
	}
	
	@Override
	protected Sorting initSorting() {
		return new Sorting("name", true);
    }
	
	@Override
	protected int getTotalCount() {
		int result = 0;
		try {
			result = new Long(sessionManagement.getDAO(OfficeKeepingFileDAOImpl.class, ApplicationHelper.OFFICE_KEEPING_FILE_DAO).countDocument(false)).intValue();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	protected List<OfficeKeepingFile> loadDocuments() {
		List<OfficeKeepingFile> result = new ArrayList<OfficeKeepingFile>();
		try {	
			result = sessionManagement.getDAO(OfficeKeepingFileDAOImpl.class, ApplicationHelper.OFFICE_KEEPING_FILE_DAO).findDocuments(false);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public List<OfficeKeepingFile> getAvailableOfficeKeepingFiles() {
		List<OfficeKeepingFile> result = new ArrayList<OfficeKeepingFile>();
		try {
			result = sessionManagement.getDAO(OfficeKeepingFileDAOImpl.class, ApplicationHelper.OFFICE_KEEPING_FILE_DAO).findDocuments(false);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public String getFilter() {
		return filter;
	}
	
	public void setFilter(String filter) {
		this.filter = filter;
	}
	
	@Inject @Named("sessionManagement")
	SessionManagementBean sessionManagement = new SessionManagementBean();
	
	private String filter;
	
	private static final long serialVersionUID = 1023130636261147049L;
}