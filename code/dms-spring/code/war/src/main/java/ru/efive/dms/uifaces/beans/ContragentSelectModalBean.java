package ru.efive.dms.uifaces.beans;

import javax.faces.context.FacesContext;

import ru.efive.crm.data.Contragent;
import ru.efive.sql.entity.user.User;
import ru.efive.uifaces.bean.ModalWindowHolderBean;

public class ContragentSelectModalBean extends ModalWindowHolderBean {
	
	public ContragentListHolderBean getContragentList() {
		if (contragentList == null) {
			FacesContext context = FacesContext.getCurrentInstance();
			contragentList = (ContragentListHolderBean) context.getApplication().evaluateExpressionGet(context, "#{contragentList}", ContragentListHolderBean.class);
		}
		return contragentList;
	}
	
	public Contragent getContragent() {
		return contragent;
	}
	
	public void setContragent(Contragent contragent) {
		this.contragent = contragent;
	}
	
	public void select(Contragent contragent) {
		this.contragent = contragent;
	}
	
	public boolean selected(Contragent contragent) {
		return this.contragent == null? false: this.contragent.equals(contragent);
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
		
	private ContragentListHolderBean contragentList;
	
	private Contragent contragent;
		
}