package ru.efive.dms.uifaces.beans;

import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentHolderBean;
import ru.efive.dms.uifaces.beans.annotations.ViewScopedController;
import ru.efive.dms.uifaces.beans.dialogs.AbstractDialog;
import ru.efive.dms.uifaces.beans.dialogs.ContragentDialogHolder;
import ru.efive.dms.uifaces.beans.dialogs.UserDialogHolder;
import ru.entity.model.numerator.Numerator;
import ru.entity.model.referenceBook.Contragent;
import ru.entity.model.referenceBook.Group;
import ru.entity.model.user.User;
import ru.hitsl.sql.dao.interfaces.NumeratorDao;
import ru.hitsl.sql.dao.util.AuthorizationData;

import javax.faces.context.FacesContext;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@ViewScopedController("numerator")
public class NumeratorHolder extends AbstractDocumentHolderBean<Numerator, NumeratorDao> {

    @Autowired
    public NumeratorHolder(@Qualifier("numeratorDao") NumeratorDao dao, @Qualifier("authData") AuthorizationData authData) {
        super(dao, authData);
    }

    @Override
    protected Numerator newModel(AuthorizationData authData) {
        return new Numerator();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////// Диалоговые окошки  /////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //Выбора руководителя ////////////////////////////////////////////////////////////////////////////////////////////////////
    public void chooseController() {
        final Map<String, List<String>> params = new HashMap<>();
        params.put(UserDialogHolder.DIALOG_TITLE_GET_PARAM_KEY, Collections.singletonList(UserDialogHolder.DIALOG_TITLE_VALUE_CONTROLLER));
        params.put(UserDialogHolder.DIALOG_GROUP_KEY, Collections.singletonList(Group.RB_CODE_MANAGERS));
        final User preselected = getDocument().getController();
        if (preselected != null) {
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(UserDialogHolder.DIALOG_SESSION_KEY, preselected);
        }
        RequestContext.getCurrentInstance().openDialog("/dialogs/selectUserDialog.xhtml", AbstractDialog.getViewOptions(), params);
    }

    public void onControllerChosen(SelectEvent event) {
        final AbstractDialog.DialogResult result = (AbstractDialog.DialogResult) event.getObject();
        log.info("Choose controller: {}", result);
        if (AbstractDialog.Button.CONFIRM.equals(result.getButton())) {
            final User selected = (User) result.getResult();
            getDocument().setController(selected);
        }
    }

    //Выбора контрагента ////////////////////////////////////////////////////////////////////////////////////////////////////
    public void chooseContragent() {
        final Contragent preselected = getDocument().getContragent();
        if (preselected != null) {
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ContragentDialogHolder.DIALOG_SESSION_KEY, preselected);
        }
        RequestContext.getCurrentInstance().openDialog("/dialogs/selectContragentDialog.xhtml", AbstractDialog.getViewOptions(), null);
    }

    public void onContragentChosen(SelectEvent event) {
        final AbstractDialog.DialogResult result = (AbstractDialog.DialogResult) event.getObject();
        log.info("Choose contragent: {}", result);
        if (AbstractDialog.Button.CONFIRM.equals(result.getButton())) {
            final Contragent selected = (Contragent) result.getResult();
            getDocument().setContragent(selected);
        }
    }


}