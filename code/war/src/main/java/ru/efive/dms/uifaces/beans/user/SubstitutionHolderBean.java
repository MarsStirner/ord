package ru.efive.dms.uifaces.beans.user;

import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentHolderBean;
import ru.efive.dms.uifaces.beans.annotations.ViewScopedController;
import ru.efive.dms.uifaces.beans.dialogs.AbstractDialog;
import ru.efive.dms.uifaces.beans.dialogs.UserDialogHolder;
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
public class SubstitutionHolderBean extends AbstractDocumentHolderBean<Substitution, SubstitutionDao> {


    @Autowired
    public SubstitutionHolderBean(@Qualifier("substitutionDao") SubstitutionDao dao, @Qualifier("authData") AuthorizationData authData) {
        super(dao, authData);
    }

    @Override
    protected Substitution newModel(AuthorizationData authData) {
        final Substitution doc = new Substitution();
        doc.setUser(authData.getAuthorized());
        return doc;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////// Диалоговые окошки  /////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //Выбора замещаемого ////////////////////////////////////////////////////////////////////////////////////////////////////
    public void choosePerson() {
        final Map<String, List<String>> params = new HashMap<>();
        params.put(
                UserDialogHolder.DIALOG_TITLE_GET_PARAM_KEY, Collections.singletonList(UserDialogHolder.DIALOG_TITLE_VALUE_PERSON_SUBSTITUTION)
        );
        final User preselected = getDocument().getUser();
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
            getDocument().setUser(selected);
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


    public void onSubstitutorChosen(final SelectEvent event) {
        final AbstractDialog.DialogResult result = (AbstractDialog.DialogResult) event.getObject();
        log.info("Choose substitution: {}", result);
        if (AbstractDialog.Button.CONFIRM.equals(result.getButton())) {
            final User selected = (User) result.getResult();
            getDocument().setSubstitution(selected);
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///// КОНЕЦ Диалоговые окошки  /////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public boolean isCanCreate(final AuthorizationData authData) {
        return authData.isFilling();
    }

    @Override
    public boolean isCanDelete(final AuthorizationData authData) {
        return authData.isFilling();
    }

    @Override
    public boolean isCanUpdate(final AuthorizationData authData) {
        return authData.isFilling();
    }

    @Override
    public boolean isCanRead(final AuthorizationData authData) {
        return authData.isFilling();
    }

    /**
     * Проверяет заполнение полей перед сохранением
     *
     * @param document документ для проверки
     * @return признак успешности проверки { false - некорректный документ }
     */
    @Override
    public boolean beforeCreate(Substitution document, AuthorizationData authData) {
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
        if (document.getUser() == null) {
            log.error("Save cancelled: person not set");
            MessageUtils.addMessage(MSG_SUBSTITUTION_PERSON_NOT_SET);
            result = false;
        }
        if (document.getUser() != null && document.getSubstitution() != null && document.getUser().equals(document.getSubstitution())) {
            log.error("Save cancelled: person and substitution is one people");
            MessageUtils.addMessage(MSG_SUBSTITUTION_PERSON_DUPLICATE);
            result = false;
        }
        return result;
    }

    @Override
    public boolean beforeUpdate(Substitution document, AuthorizationData authData) {
        return beforeCreate(document, authData);
    }
}
