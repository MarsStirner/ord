package ru.efive.dms.uifaces.beans;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import ru.efive.dms.dao.ReportDAOImpl;
import ru.efive.dms.data.ReportTemplate;
import ru.efive.dms.util.ApplicationHelper;
import ru.efive.uifaces.bean.AbstractDocumentListHolderBean;
import ru.efive.uifaces.bean.ModalWindowHolderBean;

@Named("reportTemplateList")
@SessionScoped
public class ReportTemplateListBean extends AbstractDocumentListHolderBean<ReportTemplate> {
	
	@Override
	public Pagination initPagination() {
		return new Pagination(0, getTotalCount(), 100);
	}
	
	@Override
	protected Sorting initSorting() {
		return new Sorting("displayName", true);
    }
	
	@Override
	protected int getTotalCount() {
		int result = 0;
		try {
			result = new Long(sessionManagement.getDAO(ReportDAOImpl.class, ApplicationHelper.REPORT_DAO).countDocument(false)).intValue();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	protected List<ReportTemplate> loadDocuments() {
		List<ReportTemplate> result = new ArrayList<ReportTemplate>();
		try {
			result = sessionManagement.getDAO(ReportDAOImpl.class, ApplicationHelper.REPORT_DAO).findDocuments(false, 
					getPagination().getOffset(), getPagination().getPageSize(), getSorting().getColumnId(), getSorting().isAsc());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public void composeReportTemplate(ReportTemplate reportTemplate) {
		reportSetupModal.setReportTemplate(reportTemplate);
		reportSetupModal.show();
	}
	
	
	public class ReportSetupModal extends ModalWindowHolderBean {
    	
		public void setReportTemplate(ReportTemplate reportTemplate) {
			this.reportTemplate = reportTemplate;
		}
		
		public ReportTemplate getReportTemplate() {
			return reportTemplate;
		}
		
    	@Override
        protected void doHide() {
    		super.doHide();
    		reportTemplate = null;
        }

		private ReportTemplate reportTemplate;
		
		private static final long serialVersionUID = 686982749044631899L;
	}
    
    public ReportSetupModal getReportSetupModal() {
        return reportSetupModal;
    }
	
    
    private ReportSetupModal reportSetupModal = new ReportSetupModal();
	
	@Inject @Named("sessionManagement")
	SessionManagementBean sessionManagement = new SessionManagementBean();
	
	
	private static final long serialVersionUID = 1023130636261147049L;
}