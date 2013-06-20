package ru.efive.dms.uifaces.beans;

import java.util.HashMap;
import java.util.Map;

import javax.faces.context.FacesContext;

import ru.efive.dms.data.OfficeKeepingVolume;
import ru.efive.uifaces.bean.ModalWindowHolderBean;

public class OfficeKeepingVolumeSelectModal extends ModalWindowHolderBean {	

	public OfficeKeepingVolumesSelectHolderBean getOfficeKeepingVolumes() {
		if(officeKeepingVolumes==null){
			FacesContext context=FacesContext.getCurrentInstance();		
			officeKeepingVolumes=(OfficeKeepingVolumesSelectHolderBean) context.getApplication().evaluateExpressionGet(context, "#{officeKeepingVolumesSelect}", OfficeKeepingVolumesSelectHolderBean.class);
		}
		return officeKeepingVolumes;		
	}	

	public OfficeKeepingVolume getOfficeKeepingVolume() {
		return officeKeepingVolume;
	}

	public void setOfficeKeepingVolume(OfficeKeepingVolume officeKeepingVolume) {
		this.officeKeepingVolume=officeKeepingVolume;
	}

	public void select(OfficeKeepingVolume officeKeepingVolume) {
		this.officeKeepingVolume=officeKeepingVolume;
	}

	public boolean selected(OfficeKeepingVolume officeKeepingVolume) {
		return this.officeKeepingVolume==null? false: this.officeKeepingVolume.getVolumeIndex().equals(officeKeepingVolume.getVolumeIndex());
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

	private OfficeKeepingVolumesSelectHolderBean 	officeKeepingVolumes;
	private OfficeKeepingVolume 					officeKeepingVolume;

	private static final long serialVersionUID = 8811191935540534357L;

}