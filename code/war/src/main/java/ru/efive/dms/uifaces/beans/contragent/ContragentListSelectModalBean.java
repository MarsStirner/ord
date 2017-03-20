package ru.efive.dms.uifaces.beans.contragent;

import ru.efive.uifaces.bean.ModalWindowHolderBean;
import ru.entity.model.referenceBook.Contragent;

import javax.faces.context.FacesContext;
import java.util.ArrayList;
import java.util.List;

public class ContragentListSelectModalBean extends ModalWindowHolderBean {

    public ContragentListHolderBean getContragentList() {
        if (contragentList == null) {
            FacesContext context = FacesContext.getCurrentInstance();
            contragentList = context.getApplication().evaluateExpressionGet(context, "#{contragentList}", ContragentListHolderBean.class);
        }
        return contragentList;
    }

    public List<Contragent> getContragents() {
        return contragents;
    }

    public void setContragents(List<Contragent> contragents) {
        if (contragents == null) {
            this.contragents = new ArrayList<>();
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

    private List<Contragent> contragents = new ArrayList<>();

    private static final long serialVersionUID = -4213711638535679710L;
}