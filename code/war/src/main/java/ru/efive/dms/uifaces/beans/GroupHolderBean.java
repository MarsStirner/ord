package ru.efive.dms.uifaces.beans;

import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentHolderBean;
import ru.efive.dms.uifaces.beans.annotations.ViewScopedController;
import ru.efive.dms.uifaces.beans.dialogs.AbstractDialog;
import ru.efive.dms.uifaces.beans.dialogs.MultipleUserDialogHolder;
import ru.entity.model.referenceBook.Group;
import ru.entity.model.user.User;
import ru.hitsl.sql.dao.interfaces.referencebook.GroupDao;
import ru.hitsl.sql.dao.util.AuthorizationData;

import javax.faces.context.FacesContext;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;

@ViewScopedController("group")
public class GroupHolderBean extends AbstractDocumentHolderBean<Group, GroupDao> {


    @Autowired
    public GroupHolderBean(@Qualifier("groupDao") final GroupDao dao, @Qualifier("authData") final AuthorizationData authData) {
        super(dao, authData);
    }

    @Override
    public boolean afterDelete(Group document, AuthorizationData authData) {
        try {
            FacesContext.getCurrentInstance().getExternalContext().redirect("deleted_group.xhtml");
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    @Override
    protected Group newModel(AuthorizationData authData) {
        return new Group();
    }


    // Выбора исполнителей /////////////////////////////////////////////////////////////////////////////////////////////
    public void chooseMembers() {
        final List<User> preselected = getDocument().getMembersList();
        if (preselected != null && !preselected.isEmpty()) {
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(MultipleUserDialogHolder.DIALOG_SESSION_KEY, preselected);
        }
        RequestContext.getCurrentInstance().openDialog("/dialogs/selectMultipleUserDialog.xhtml", AbstractDialog.getViewOptions(), null);
    }

    public void onMembersChosen(SelectEvent event) {
        final AbstractDialog.DialogResult result = (AbstractDialog.DialogResult) event.getObject();
        log.info("Choose members  : {}", result);
        if (AbstractDialog.Button.CONFIRM.equals(result.getButton())) {
            final List<User> selected = (List<User>) result.getResult();
            if (selected != null && !selected.isEmpty()) {
                getDocument().setMembers(new HashSet<>(selected));
            } else {
                getDocument().getMembers().clear();
            }
        }
    }


}