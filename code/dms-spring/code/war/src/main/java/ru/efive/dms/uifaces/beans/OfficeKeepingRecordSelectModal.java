package ru.efive.dms.uifaces.beans;

import javax.faces.context.FacesContext;

import ru.efive.dms.data.OfficeKeepingRecord;
import ru.efive.uifaces.bean.ModalWindowHolderBean;

public class OfficeKeepingRecordSelectModal extends ModalWindowHolderBean {	

	public OfficeKeepingRecordListHolderBean getOfficeKeepingRecordList() {
		if (officeKeepingRecordList == null) {
			FacesContext context = FacesContext.getCurrentInstance();
			officeKeepingRecordList = (OfficeKeepingRecordListHolderBean) context.getApplication().evaluateExpressionGet(context, "#{officeKeepingRecordList}", OfficeKeepingRecordListHolderBean.class);
		}
		return officeKeepingRecordList;
	}
	
	public OfficeKeepingRecord getOfficeKeepingRecord() {
		return officeKeepingRecord;
	}
	
	public void setOfficeKeepingRecord(OfficeKeepingRecord officeKeepingRecord) {
		this.officeKeepingRecord = officeKeepingRecord;
	}
	
	public void select(OfficeKeepingRecord officeKeepingRecord) {
		this.officeKeepingRecord = officeKeepingRecord;
	}
	
	public boolean selected(OfficeKeepingRecord officeKeepingRecord) {
		return this.officeKeepingRecord == null? false: this.officeKeepingRecord.getRecordIndex().equals(officeKeepingRecord.getRecordIndex());
	}
	
	@Override
	protected void doSave() {
		super.doSave();
	}
	
	@Override
	protected void doShow() {
		super.doShow();
	}
	
	@Override
    protected void doHide() {
		super.doHide();
    }
		
	private OfficeKeepingRecordListHolderBean officeKeepingRecordList;
	
	private OfficeKeepingRecord officeKeepingRecord;
	
	private static final long serialVersionUID = 8811191935540534357L;
	
}