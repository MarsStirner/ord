package ru.efive.dms.uifaces.beans.user;

import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentHolderBean;
import ru.efive.dms.uifaces.beans.abstractBean.State;
import ru.efive.dms.uifaces.beans.annotations.ViewScopedController;
import ru.efive.dms.uifaces.beans.dialogs.AbstractDialog;
import ru.efive.dms.uifaces.beans.dialogs.UserDialogHolder;
import ru.efive.dms.util.message.MessageHolder;
import ru.efive.dms.util.message.MessageKey;
import ru.efive.dms.util.message.MessageUtils;
import ru.entity.model.user.Substitution;
import ru.entity.model.user.User;
import ru.hitsl.sql.dao.interfaces.SubstitutionDao;
import ru.hitsl.sql.dao.util.AuthorizationData;

import javax.faces.context.FacesContext;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.efive.dms.util.message.MessageHolder.*;

/**
 * Author: Upatov Egor <br>
 * Date: 18.11.2014, 18:16 <br>
 * Company: Korus Consulting IT <br>
 * Description: бин для отображения замещения<br>
 */
@ViewScopedController("substitution")
public class SubstitutionHolderBean extends AbstractDocumentHolderBean<Substitution> {

    @Autowired
    @Qualifier("substitutionDao")
    private SubstitutionDao substitutionDao;
    @Autowired
    @Qualifier("authData")
    private AuthorizationData authData;


    //Выбора замещаемого ////////////////////////////////////////////////////////////////////////////////////////////////////
    public void choosePerson() {
        final Map<String, List<String>> params = new HashMap<>();
        params.put(
                UserDialogHolder.DIALOG_TITLE_GET_PARAM_KEY, Collections.singletonList(UserDialogHolder.DIALOG_TITLE_VALUE_PERSON_SUBSTITUTION)
        );
        final User preselected = getDocument().getPerson();
        if (preselected != null) {
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(UserDialogHolder.DIALOG_SESSION_KEY, preselected);
        }
        RequestContext.getCurrentInstance().openDialog("/dialogs/selectUserDialog.xhtml", AbstractDialog.getViewOptions(), params);
    }

    public void onPersonChosen(final SelectEvent event) {
        final AbstractDialog.DialogResult result = (AbstractDialog.DialogResult) event.getObject();
        log.info("Choose person: {}", result);
        if (AbstractDialog.Button.CONFIRM.equals(result.getButton())) {
            final User selected = (User) result.getResult();
            getDocument().setPerson(selected);
        }
    }

    //Выбора замещающего ////////////////////////////////////////////////////////////////////////////////////////////////////
    public void chooseSubstitutor() {
        final Map<String, List<String>> params = new HashMap<>();
        params.put(
                UserDialogHolder.DIALOG_TITLE_GET_PARAM_KEY, Collections.singletonList(UserDialogHolder.DIALOG_TITLE_VALUE_PERSON_SUBSTITUTOR)
        );
        final User preselected = getDocument().getSubstitution();
        if (preselected != null) {
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(UserDialogHolder.DIALOG_SESSION_KEY, preselected);
        }
        RequestContext.getCurrentInstance().openDialog("/dialogs/selectUserDialog.xhtml", AbstractDialog.getViewOptions(), params);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////// Диалоговые окошки  /////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void onSubstitutorChosen(final SelectEvent event) {
        final AbstractDialog.DialogResult result = (AbstractDialog.DialogResult) event.getObject();
        log.info("Choose substitution: {}", result);
        if (AbstractDialog.Button.CONFIRM.equals(result.getButton())) {
            final User selected = (User) result.getResult();
            getDocument().setSubstitution(selected);
        }
    }

    @Override
    protected boolean deleteDocument() {
        final Substitution doc = getDocument();
        doc.setDeleted(true);
        try {
            setDocument(substitutionDao.save(doc));
            FacesContext.getCurrentInstance().getExternalContext().redirect("delete_document.xhtml");
            return true;
        } catch (Exception e) {
            log.error("saveDocument ERROR:", e);
            MessageUtils.addMessage(MSG_ERROR_ON_DELETE);
            return false;
        }
    }

    @Override
    protected void initNewDocument() {
        final Substitution doc = new Substitution();
        doc.setPerson(authData.getAuthorized());
        setDocument(doc);
    }

    @Override
    protected void initDocument(Integer id) {
        final User currentUser = authData.getAuthorized();
        log.info("Open Document[{}] by user[{}]", id, currentUser.getId());
        final Substitution document = substitutionDao.get(id);
        setDocument(document);
        if (document == null) {
            setState(State.ERROR);
            log.warn("Document NOT FOUND");
            MessageUtils.addMessage(MessageKey.ERROR, MessageHolder.MSG_DOCUMENT_NOT_FOUND);
        } else if (document.isDeleted()) {
            setState(State.ERROR);
            log.warn("Document[{}] IS DELETED", document.getId());
            MessageUtils.addMessage(MessageKey.ERROR, MessageHolder.MSG_DOCUMENT_IS_DELETED);
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///// КОНЕЦ Диалоговые окошки  /////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public boolean saveNewDocument() {
        return saveDocument();
    }

    @Override
    protected boolean saveDocument() {
        final Substitution document = getDocument();
        if (validateBeforeSave(document)) {
            try {
                setDocument(substitutionDao.update(document));
                return true;
            } catch (Exception e) {
                log.error("saveDocument ERROR:", e);
                MessageUtils.addMessage(MSG_ERROR_ON_SAVE);
                return false;
            }
        }
        return false;
    }

    @Override
    public boolean isCanCreate() {
        return super.isCanCreate() && authData.isFilling();
    }

    @Override
    public boolean isCanDelete() {
        return super.isCanDelete() && authData.isFilling();
    }

    @Override
    public boolean isCanEdit() {
        return super.isCanEdit() && authData.isFilling();
    }

    @Override
    public boolean isCanView() {
        return super.isCanView() && authData.isFilling();
    }

    /**
     * Проверяет заполнение полей перед сохранением
     *
     * @param document документ для проверки
     * @return признак успешности проверки { false - некорректный документ }
     */
    private boolean validateBeforeSave(Substitution document) {
        boolean result = true;
        if (document.getStartDate() != null && document.getEndDate() != null) {
            if (document.getStartDate().isAfter(document.getEndDate())) {
                log.error("Save cancelled: startDate[{}] is not before endDate[{}]", document.getStartDate(), document.getEndDate());
                MessageUtils.addMessage(MSG_SUBSTITUTION_DATE_MISMATCH);
                result = false;
            }
        }
        if (document.getSubstitution() == null) {
            log.error("Save cancelled: substitution not set");
            MessageUtils.addMessage(MSG_SUBSTITUTION_SUBSTITUTOR_NOT_SET);
            result = false;
        }
        if (document.getPerson() == null) {
            log.error("Save cancelled: person not set");
            MessageUtils.addMessage(MSG_SUBSTITUTION_PERSON_NOT_SET);
            result = false;
        }
        if (document.getPerson() != null && document.getSubstitution() != null && document.getPerson().equals(document.getSubstitution())) {
            log.error("Save cancelled: person and substitution is one people");
            MessageUtils.addMessage(MSG_SUBSTITUTION_PERSON_DUPLICATE);
            result = false;
        }
        return result;
    }
}
