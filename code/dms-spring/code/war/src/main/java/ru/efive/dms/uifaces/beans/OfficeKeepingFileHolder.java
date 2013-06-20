package ru.efive.dms.uifaces.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import ru.efive.sql.entity.enums.RoleType;
import ru.efive.dms.dao.DocumentFormDAOImpl;
import ru.efive.dms.dao.OfficeKeepingFileDAOImpl;
import ru.efive.dms.data.DocumentForm;
import ru.efive.dms.data.HistoryEntry;
import ru.efive.dms.data.IncomingDocument;
import ru.efive.dms.data.OfficeKeepingFile;
import ru.efive.dms.util.ApplicationHelper;
import ru.efive.uifaces.bean.AbstractDocumentHolderBean;
import ru.efive.uifaces.bean.FromStringConverter;
import ru.efive.wf.core.ActionResult;

@Named("officeKeepingFile")
@ConversationScoped
public class OfficeKeepingFileHolder extends AbstractDocumentHolderBean<OfficeKeepingFile, Integer> implements Serializable {

	@Override
	protected boolean deleteDocument() {
		boolean result = false;
		try {
			sessionManagement.getDAO(OfficeKeepingFileDAOImpl.class, ApplicationHelper.OFFICE_KEEPING_FILE_DAO).delete(getDocument());
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
		setDocument(sessionManagement.getDAO(OfficeKeepingFileDAOImpl.class, ApplicationHelper.OFFICE_KEEPING_FILE_DAO).get(id));
		if (getDocument() == null) {
			setState(STATE_NOT_FOUND);
		}
	}
	
	@Override
	protected void initNewDocument() {
		OfficeKeepingFile document = new OfficeKeepingFile();
		document.setDocumentStatus(DocumentStatus.NEW);
		Date created = Calendar.getInstance(ApplicationHelper.getLocale()).getTime();		
		//document.setCreationDate(created);
		//document.setAuthor(sessionManagement.getLoggedUser());		
		
		HistoryEntry historyEntry = new HistoryEntry();
		historyEntry.setCreated(created);
		historyEntry.setStartDate(created);
		historyEntry.setOwner(sessionManagement.getLoggedUser());
		historyEntry.setDocType(document.getDocumentType().getName());
		historyEntry.setParentId(document.getId());
		historyEntry.setActionId(0);
		historyEntry.setFromStatusId(1);
		historyEntry.setEndDate(created);
		historyEntry.setProcessed(true);
		historyEntry.setCommentary("");
		Set<HistoryEntry> history = new HashSet<HistoryEntry>();
		history.add(historyEntry);
		document.setHistory(history);
		
		setDocument(document);		
	}
	
	@Override
	protected boolean saveDocument() {
		boolean result = false;
		try {
			OfficeKeepingFile record = sessionManagement.getDAO(OfficeKeepingFileDAOImpl.class, ApplicationHelper.OFFICE_KEEPING_FILE_DAO).update(getDocument());
			if (record == null) {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
						FacesMessage.SEVERITY_ERROR,
						"Невозможно сохранить документ. Попробуйте повторить позже.", ""));
			}
			else {
				setDocument(record);
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
			OfficeKeepingFile record = sessionManagement.getDAO(OfficeKeepingFileDAOImpl.class, ApplicationHelper.OFFICE_KEEPING_FILE_DAO).save(getDocument());
			if (record == null) {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
						FacesMessage.SEVERITY_ERROR,
						"Невозможно сохранить документ. Попробуйте повторить позже.", ""));
			}
			else {
				setDocument(record);
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
		officeKeepingFiles.markNeedRefresh();
        return super.doAfterCreate();
    }
    
    @Override
    protected String doAfterDelete() {
    	officeKeepingFiles.markNeedRefresh();
        return super.doAfterDelete();
    }
    
    @Override
    protected String doAfterSave() {
    	officeKeepingFiles.markNeedRefresh();
        return super.doAfterSave();
    }
    
    public List<RoleType> getTypes() {
    	List<RoleType> result = new ArrayList<RoleType>();
    	for (RoleType type: RoleType.values()) {
    		result.add(type);
    	}
    	return result;
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
	
	public String getVolumesTabHeader() {
		return "<span><span>Тома дел</span></span>";
	}

	public boolean isVolumesTabSelected() {
		return isVolumesTabSelected;
	}
	
	public void setVolumesTabSelected(boolean isVolumesTabSelected) {
		this.isVolumesTabSelected = isVolumesTabSelected;
	}
	
    public String getLocationTabHeader() {
		return "<span><span>Местоположение</span></span>";
	}

	public boolean isLocationTabSelected() {
		return isLocationTabSelected;
	}
	
	public void setLocationTabSelected(boolean isLocationTabSelected) {
		this.isLocationTabSelected = isLocationTabSelected;
	}
	
	public String getHistoryTabHeader() {
		return "<span><span>История</span></span>";
	}

	public void setHistoryTabSelected(boolean isHistoryTabSelected) {
		this.isHistoryTabSelected = isHistoryTabSelected;
	}

	public boolean isHistoryTabSelected() {
		return isHistoryTabSelected;
	}

	private boolean isRequisitesTabSelected = true;
	private boolean isVolumesTabSelected = false;
	private boolean isLocationTabSelected = false;
	private boolean isHistoryTabSelected = false;
	
	public ProcessorModalBean getProcessorModal() {
		return processorModal;
	}
    
    private ProcessorModalBean processorModal = new ProcessorModalBean() {
    	    	
		@Override
		protected void doInit() {
			setProcessedData(getDocument());
			if (getDocumentId() == null || getDocumentId() == 0) saveNewDocument();
		}
		@Override
		protected void doPostProcess(ActionResult actionResult) {			
			OfficeKeepingFile document = (OfficeKeepingFile) actionResult.getProcessedData();
			if (getSelectedAction().isHistoryAction()) {
				Set<HistoryEntry> history = document.getHistory();
				if (history == null) {
					history = new HashSet<HistoryEntry>();
				}
				history.add(getHistoryEntry());
				document.setHistory(history);
			}
			setDocument(document);
			OfficeKeepingFileHolder.this.save();
		}
		@Override
		protected void doProcessException(ActionResult actionResult) {
			OfficeKeepingFile document = (OfficeKeepingFile) actionResult.getProcessedData();
			String in_result = document.getWFResultDescription();
			
			if (!in_result.equals("")) {
				setActionResult(in_result);
			}
		}
	};
	
    @Inject @Named("sessionManagement")
	SessionManagementBean sessionManagement = new SessionManagementBean();
	@Inject @Named("officeKeepingFiles")
	OfficeKeepingFilesHolderBean officeKeepingFiles=new OfficeKeepingFilesHolderBean();
	
	private static final long serialVersionUID = 5947443099767481905L;
}