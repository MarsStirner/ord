package ru.efive.dms.uifaces.beans;

import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;

import ru.entity.model.crm.Contragent;
import ru.efive.uifaces.bean.ModalWindowHolderBean;

public class ContragentListSelectModalBean extends ModalWindowHolderBean {

    public ContragentListHolderBean getContragentList() {
        if (contragentList == null) {
            FacesContext context = FacesContext.getCurrentInstance();
            contragentList = (ContragentListHolderBean) context.getApplication().evaluateExpressionGet(context, "#{contragentList}", ContragentListHolderBean.class);
        }
        return contragentList;
    }

    public List<Contragent> getContragents() {
        return contragents;
    }

    public void setContragents(List<Contragent> contragents) {
        if (contragents == null) {
            this.contragents = new ArrayList<Contragent>();
        } else {
            this.contragents = contragents;
        }
    }

    public void select(Contragent contragent) {
        contragents.add(contragent);
    }

    public void unselect(Contragent contragent) {
        contragents.remove(contragent);
    }

    public boolean selected(Contragent contragent) {
        return contragents.contains(contragent);
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

    private List<Contragent> contragents = new ArrayList<Contragent>();

    private static final long serialVersionUID = -4213711638535679710L;
}