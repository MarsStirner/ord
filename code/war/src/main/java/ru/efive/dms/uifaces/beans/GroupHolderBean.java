package ru.efive.dms.uifaces.beans;

import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentHolderBean;
import ru.efive.dms.uifaces.beans.annotations.ViewScopedController;
import ru.efive.dms.uifaces.beans.dialogs.AbstractDialog;
import ru.efive.dms.uifaces.beans.dialogs.MultipleUserDialogHolder;
import ru.efive.dms.util.message.MessageHolder;
import ru.efive.dms.util.message.MessageUtils;
import ru.entity.model.referenceBook.Group;
import ru.entity.model.user.User;
import ru.hitsl.sql.dao.interfaces.referencebook.GroupDao;

import javax.faces.context.FacesContext;
import java.util.HashSet;
import java.util.List;

@ViewScopedController("group")
public class GroupHolderBean extends AbstractDocumentHolderBean<Group> {

    private static final Logger LOGGER = LoggerFactory.getLogger("GROUP");

    @Autowired
    @Qualifier("groupDao")
    private GroupDao groupDao;


    @Override
    protected boolean deleteDocument() {
        try {
            getDocument().setDeleted(true);
            groupDao.update(getDocument());
            FacesContext.getCurrentInstance().getExternalContext().redirect("deleted_group.xhtml");
            return true;
        } catch (Exception e) {
            MessageUtils.addMessage(MessageHolder.MSG_ERROR_ON_DELETE);
            LOGGER.error("Error on delete:", e);
        }
        return false;
    }

    @Override
    protected void initDocument(Integer id) {
        setDocument(groupDao.get(id));
        if (getDocument() == null) {
            setDocumentNotFound();
        } else if (getDocument().isDeleted()) {
            setDocumentDeleted();
        }
    }

    @Override
    protected void initNewDocument() {
        Group group = new Group();
        setDocument(group);
    }

    @Override
    protected boolean saveDocument() {
        try {
            Group group = groupDao.update(getDocument());
            if (group == null) {
                MessageUtils.addMessage(MessageHolder.MSG_CANT_SAVE);
            } else {
                setDocument(group);
                return true;
            }
        } catch (Exception e) {
            MessageUtils.addMessage(MessageHolder.MSG_ERROR_ON_SAVE);
            LOGGER.error("Error on save:", e);
        }
        return false;
    }

    @Override
    public boolean saveNewDocument() {
        try {
            Group group = groupDao.save(getDocument());
            if (group == null) {
                MessageUtils.addMessage(MessageHolder.MSG_CANT_SAVE);
            } else {
                setDocument(group);
                return true;
            }
        } catch (Exception e) {
            MessageUtils.addMessage(MessageHolder.MSG_ERROR_ON_SAVE_NEW);
            LOGGER.error("Error on saveNew:", e);
        }
        return false;
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
        LOGGER.info("Choose members  : {}", result);
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