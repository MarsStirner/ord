package ru.efive.dms.uifaces.beans.user;

import org.primefaces.event.SelectEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.efive.dms.dao.ejb.SubstitutionDaoImpl;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.uifaces.beans.utils.MessageHolder;
import ru.efive.dms.util.ApplicationDAONames;
import ru.efive.uifaces.bean.AbstractDocumentHolderBean;
import ru.efive.uifaces.bean.FromStringConverter;
import ru.entity.model.user.Substitution;
import ru.entity.model.user.User;

import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * Author: Upatov Egor <br>
 * Date: 18.11.2014, 18:16 <br>
 * Company: Korus Consulting IT <br>
 * Description: Ьин для отображения замещения<br>
 */
@Named("substitution")
@ConversationScoped
public class SubstitutionHolderBean extends AbstractDocumentHolderBean<Substitution, Integer> {

    //Именованный логгер (INCOMING_DOCUMENT)
    private static final Logger logger = LoggerFactory.getLogger("SUBSTITUTION");

    private User selectedPerson = null;
    private User selectedSubstitution = null;

    public void confirmPersonSelection() {
        getDocument().setPerson(selectedPerson);
    }

    public void confirmSubstitutionSelection() {
        getDocument().setSubstitution(selectedSubstitution);
    }

    public User getSelectedPerson() {
        return selectedPerson;
    }

    public User getSelectedSubstitution() {
        return selectedSubstitution;
    }

    public void setSelectedSubstitution(User selection) {
        if(selection != null) {
            this.selectedSubstitution = selection;
        }
    }

    public void setSelectedPerson(User selection) {
        if (selection != null) {
            selectedPerson = selection;
        }
    }

    @Override
    protected boolean deleteDocument() {
        return false;
    }

    @Override
    protected Integer getDocumentId() {
        return getDocument().getId();
    }

    @Override
    protected void initNewDocument() {
        final Substitution doc = new Substitution();
        doc.setPerson(sessionManagement.getLoggedUser());
        setDocument(doc);
    }

    @Override
    protected void initDocument(Integer id) {
        final User currentUser = sessionManagement.getLoggedUser();
        logger.info("Open Document[{}] by user[{}]", id, currentUser.getId());
        final Substitution doc = sessionManagement.getDAO(SubstitutionDaoImpl.class, ApplicationDAONames.SUBSTITUTION_DAO).get(id);
        if (doc != null) {
            setDocument(doc);
        } else {
            logger.error("Document[{}] not found", id);
        }
    }

    @Override
    protected boolean saveNewDocument() {
        return saveDocument();
    }

    @Override
    protected boolean saveDocument() {
        final Substitution document = getDocument();
        if (validateBeforeSave(document)) {
            try {
                setDocument(sessionManagement.getDAO(SubstitutionDaoImpl.class, ApplicationDAONames.SUBSTITUTION_DAO).save(document));
                return true;
            } catch (Exception e) {
                logger.error("saveDocument ERROR:", e);
                FacesContext.getCurrentInstance().addMessage(null, MessageHolder.MSG_ERROR_ON_SAVE);
                return false;
            }
        }
        return false;
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
            if (document.getStartDate().after(document.getEndDate())) {
                logger.error("Save cancelled: startDate[{}] is not before endDate[{}]", document.getStartDate(), document.getEndDate());
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Ошибка валидации", "Дата начала периода замещения позже чем его дата окончания"));
                result = false;
            }
        }
        if (document.getSubstitution() == null) {
            logger.error("Save cancelled: substitution not set");
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Ошибка валидации", "Не выбран заместитель"));
            result = false;
        }
        if (document.getPerson() == null) {
            logger.error("Save cancelled: person not set");
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Ошибка валидации", "Не выбрано замещаемое лицо"));
            result = false;
        }
        if (document.getPerson() != null && document.getSubstitution() != null && document.getPerson().equals(document.getSubstitution())) {
            logger.error("Save cancelled: person and substitution is one people");
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Ошибка валидации", "Замещаемое лицо и заместитель один и тот-же человек. Спрашивается: нахрена?"));
            result = false;
        }
        return result;
    }

    @Override
    protected FromStringConverter<Integer> getIdConverter() {
        return FromStringConverter.INTEGER_CONVERTER;
    }

    @Inject
    @Named("sessionManagement")
    SessionManagementBean sessionManagement = new SessionManagementBean();
}
