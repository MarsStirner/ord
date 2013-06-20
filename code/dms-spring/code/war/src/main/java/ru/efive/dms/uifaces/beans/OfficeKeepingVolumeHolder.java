package ru.efive.dms.uifaces.beans;

import java.io.Serializable;
import java.text.SimpleDateFormat;
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
import ru.efive.dms.dao.IncomingDocumentDAOImpl;
import ru.efive.dms.dao.OfficeKeepingFileDAOImpl;
import ru.efive.dms.dao.OfficeKeepingVolumeDAOImpl;
import ru.efive.dms.data.HistoryEntry;
import ru.efive.dms.data.IncomingDocument;
import ru.efive.dms.data.OfficeKeepingFile;
import ru.efive.dms.data.OfficeKeepingVolume;
import ru.efive.dms.util.ApplicationHelper;
import ru.efive.uifaces.bean.AbstractDocumentHolderBean;
import ru.efive.uifaces.bean.FromStringConverter;
import ru.efive.wf.core.ActionResult;

@Named("officeKeepingVolume")
@ConversationScoped
public class OfficeKeepingVolumeHolder extends AbstractDocumentHolderBean<OfficeKeepingVolume, Integer> implements Serializable {

	private static final long serialVersionUID = -7696075488442962088L;

	@Override
	public String delete(){
		String in_result=super.delete();
		if(in_result!=null && in_result.equals("delete")){
			try {				
				FacesContext.getCurrentInstance().getExternalContext().redirect("delete_document.xhtml");
			}
			catch (Exception e) {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
						FacesMessage.SEVERITY_ERROR, "Невозможно удалить документ", ""));
				e.printStackTrace();
			}			
			return in_result;
		}else{
			return in_result;
		}
	}
	
	@Override
	protected boolean deleteDocument() {
		boolean result = false;
		try {
			sessionManagement.getDAO(OfficeKeepingVolumeDAOImpl.class, ApplicationHelper.OFFICE_KEEPING_VOLUME_DAO).delete(getDocument());			
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
		setDocument(sessionManagement.getDAO(OfficeKeepingVolumeDAOImpl.class, ApplicationHelper.OFFICE_KEEPING_VOLUME_DAO).get(id));

		if (getDocument() == null) {			
			setState(STATE_NOT_FOUND);		
		}
	}

	@Override
	protected void initNewDocument() {		
		OfficeKeepingVolume document=new OfficeKeepingVolume();
		OfficeKeepingFile file=null;
		String parentId=FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("parentId");
		if(parentId==null||parentId.equals("")){
		}else{
			file=sessionManagement.getDAO(OfficeKeepingFileDAOImpl.class, ApplicationHelper.OFFICE_KEEPING_FILE_DAO).findDocumentById(parentId);
			if(file!=null){				
				document.setParentFile(file);				
			}
		}
		document.setDocumentStatus(DocumentStatus.NEW);
		Date created = Calendar.getInstance(ApplicationHelper.getLocale()).getTime();		

		if(file!=null){
			document.setShortDescription(file.getShortDescription());
			document.setComments(file.getComments());
			document.setKeepingPeriodReasons(file.getKeepingPeriodReasons());
			document.setFundNumber(file.getFundNumber());
			document.setBoxNumber(file.getBoxNumber());
			document.setShelfNumber(file.getShelfNumber());
			document.setStandNumber(file.getStandNumber());
			document.setLimitUnitsCount(250);
		}

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
			OfficeKeepingVolume record = sessionManagement.getDAO(OfficeKeepingVolumeDAOImpl.class, ApplicationHelper.OFFICE_KEEPING_VOLUME_DAO).update((OfficeKeepingVolume)getDocument());			
			if (record == null) {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
						FacesMessage.SEVERITY_ERROR,
						"Невозможно сохранить документ. Попробуйте повторить позже.", ""));
			}
			else {				
				//setDocument(record);
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
			OfficeKeepingVolume document=getDocument();
			OfficeKeepingFile file=document.getParentFile();
			if(file!=null){

				OfficeKeepingVolume record = sessionManagement.getDAO(OfficeKeepingVolumeDAOImpl.class, ApplicationHelper.OFFICE_KEEPING_VOLUME_DAO).save(document);
				//OfficeKeepingVolume record=getDocument();
				if (record == null) {
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
							FacesMessage.SEVERITY_ERROR,
							"Невозможно сохранить документ. Попробуйте повторить позже.", ""));
				}
				else {				
					//setDocument(record);
					result = true;					
				}}else{
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
							FacesMessage.SEVERITY_ERROR,
							"Невозможно сохранить документ. Необходимо выбрать корректный документ Номенклатуры дел.", ""));
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
		//officeKeepingVolumes.markNeedRefresh();
		return super.doAfterCreate();
	}

	@Override
	protected String doAfterDelete() {
		//officeKeepingVolumes.markNeedRefresh();
		return super.doAfterDelete();
	}

	@Override
	protected String doAfterSave() {
		//officeKeepingVolumes.markNeedRefresh();
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

	public String getLocationTabHeader() {
		return "<span><span>Местоположение</span></span>";
	}

	public boolean isLocationTabSelected() {
		return isLocationTabSelected;
	}

	public void setLocationTabSelected(boolean isLocationTabSelected) {
		this.isLocationTabSelected = isLocationTabSelected;
	}

	public String getDocumentsTabHeader() {
		return "<span><span>Документы</span></span>";
	}

	public boolean isDocumentsTabSelected() {
		return isDocumentsTabSelected;
	}
	
	public void setDocumentsTabSelected(boolean isDocumentsTabSelected) {
		this.isDocumentsTabSelected = isDocumentsTabSelected;
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
	
	public ProcessorModalBean getProcessorModal() {
		return processorModal;
	}	

	public UserSelectModalBean getCollectorSelectModal() {
		return collectorSelectModal;
	}

	private ProcessorModalBean processorModal = new ProcessorModalBean() {

		@Override
		protected void doInit() {
			setProcessedData(getDocument());
			if (getDocumentId() == null || getDocumentId() == 0) saveNewDocument();
		}
		@Override
		protected void doPostProcess(ActionResult actionResult) {				
			OfficeKeepingVolume document = (OfficeKeepingVolume) actionResult.getProcessedData();
			HistoryEntry historyEntry=getHistoryEntry();
			if(document.getStatusId()==110 && document.getCollector()!=null){				
				historyEntry.setCommentary(document.getCollector().getDescriptionShort()+" : на руках до "+(new SimpleDateFormat("dd.MM.yyyy")).format(document.getReturnDate()));
			}
			if (getSelectedAction().isHistoryAction()) {
				Set<HistoryEntry> history = document.getHistory();
				if (history == null) {
					history = new HashSet<HistoryEntry>();
				}
				history.add(historyEntry);
				document.setHistory(history);
			}
			setDocument(document);
			OfficeKeepingVolumeHolder.this.save();
		}
		@Override
		protected void doProcessException(ActionResult actionResult) {
			OfficeKeepingVolume document = (OfficeKeepingVolume) actionResult.getProcessedData();
			String in_result = document.getWFResultDescription();

			if (!in_result.equals("")) {
				setActionResult(in_result);
			}
		}
	};

	private UserSelectModalBean collectorSelectModal = new UserSelectModalBean() {
		@Override
		protected void doSave() {
			super.doSave();
			getDocument().setCollector(getUser());			
		}
		@Override
		protected void doHide() {
			super.doHide();
			getUserList().setFilter("");
			getUserList().markNeedRefresh();
			setUser(null);
		}
	};

	private boolean isRequisitesTabSelected = true;
	private boolean isDocumentsTabSelected = false;
	private boolean isLocationTabSelected = false;
	private boolean isHistoryTabSelected = false;
	
	@Inject @Named("sessionManagement")
	SessionManagementBean sessionManagement = new SessionManagementBean();
	//@Inject @Named("officeKeepingVolumes")
	//OfficeKeepingVolumesHolderBean officeKeepingVolumes=new OfficeKeepingVolumesHolderBean();

}