package ru.efive.dms.uifaces.beans.user;

import java.io.Serializable;
import java.util.*;

import javax.enterprise.context.ConversationScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.efive.dms.uifaces.beans.utils.MessageHolder;
import ru.efive.sql.dao.user.RbContactTypeDAO;
import ru.efive.sql.dao.user.UserDAOHibernate;
import ru.efive.sql.entity.user.PersonContact;
import ru.efive.sql.entity.user.RbContactInfoType;
import ru.efive.sql.entity.user.User;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.util.ApplicationHelper;
import ru.efive.uifaces.bean.AbstractDocumentHolderBean;
import ru.efive.uifaces.bean.FromStringConverter;

@Named("user")
@ConversationScoped
public class UserHolderBean extends AbstractDocumentHolderBean<User, Integer> implements Serializable {
    private static final Logger LOGGER = LoggerFactory.getLogger("USER");

    @Override
    public boolean isCanCreate() {
        if(!super.isCanEdit()){
            LOGGER.error("TRY TO CREATE ON ErrorState");
            return false;
        }
        final User loggedUser = sessionManagement.getLoggedUser();
        if(!loggedUser.isAdministrator() && !loggedUser.isHr()){
            LOGGER.error("User[{}] try to create new User without permission. Restricted!", loggedUser.getId());
            return false;
        } else {
            return true;
        }
    }

    @Override
    public boolean isCanEdit(){
        if(!super.isCanEdit()){
            LOGGER.error("TRY TO EDIT ON ErrorState");
            return false;
        }
        final User loggedUser = sessionManagement.getLoggedUser();
        if(!loggedUser.isAdministrator() && !loggedUser.isHr()){
            LOGGER.error("User[{}] try to edit User[{}] info without permission. Restricted!", loggedUser.getId(), getDocumentId());
            return false;
        } else {
            return true;
        }
    }

    /**
     * Меняет пароль у открытого на редактирование пользователя (+ хэширует при setPassword)
     * @param newPass новый пароль
     */
    public void changePassword(String newPass){
        getDocument().setPassword(newPass);
    }

    /**
     * Обработчик нажатия на кнопку "Уволить\Восстановить", с сохранением изменений в БД
     *
     * @return флаг удаления
     */
    public boolean changeFired() {
        final User user = getDocument();
        final Date now = new Date();
        if (user.isFired()) {
            //Восстанавливаем уволенного сотрудника
            user.hire(now);
        } else {
            //Увольняем сотрудника
            user.fire(now);
        }
        final User alteredUser = sessionManagement.getDAO(UserDAOHibernate.class, ApplicationHelper.USER_DAO).save(user);
        setDocument(alteredUser);
        return alteredUser.isFired();
    }

    /**
     * Удаление контакта из модели \ бина
     * @param contact контакт, который надо удалить
     * @return успешность удаления
     */
    public boolean deleteContact(final PersonContact contact) {
        return getDocument().getContacts().remove(contact);

    }

    /**
     * Добавление нового контакта в модель
     *
     * @return успешность добавления
     */
    public boolean addContact() {
        PersonContact newContact = new PersonContact();
        newContact.setPerson(getDocument());
        final List<RbContactInfoType> typeList = sessionManagement.getDictionaryDAO(RbContactTypeDAO.class, ApplicationHelper.RB_CONTACT_TYPE_DAO).findDocuments();
        if (!typeList.isEmpty()) {
            newContact.setType(typeList.get(0));
            return getDocument().getContacts().add(newContact);
        }
        return false;
    }

    @Override
    protected boolean deleteDocument() {
        boolean result = false;
        try {
            sessionManagement.getDAO(UserDAOHibernate.class, ApplicationHelper.USER_DAO).delete(getDocument());
            result = true;
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, MessageHolder.MSG_CANT_DELETE);
        }
        return result;
    }

    @Override
    protected Integer getDocumentId() {
        return getDocument().getId();
    }

    @Override
    protected FromStringConverter<Integer> getIdConverter() {
        return FromStringConverter.INTEGER_CONVERTER;
    }

    @Override
    protected void initDocument(Integer id) {
        setDocument(sessionManagement.getDAO(UserDAOHibernate.class, ApplicationHelper.USER_DAO).getUser(id));
    }

    @Override
    protected void initNewDocument() {
        User user = new User();
        user.setCreated(Calendar.getInstance(ApplicationHelper.getLocale()).getTime());
        user.setDeleted(false);
        user.setFired(false);
        setDocument(user);
    }

    @Override
    protected boolean saveDocument() {
        boolean result = false;
        try {
            //Удаляем пустые контактные данные, введенные пользователем
            removeEmptyContacts(getDocument().getContacts());
            User user = sessionManagement.getDAO(UserDAOHibernate.class, ApplicationHelper.USER_DAO).update(getDocument());
            if (user == null) {
                FacesContext.getCurrentInstance().addMessage(null, MessageHolder.MSG_CANT_SAVE);
            } else {
                setDocument(user);
                result = true;
            }
        } catch (Exception e) {
            result = false;
            FacesContext.getCurrentInstance().addMessage(null, MessageHolder.MSG_ERROR_ON_SAVE);
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Удаляем незаполненные контактные данные
     * @param contacts
     */
    private void removeEmptyContacts(Collection<PersonContact> contacts) {
        final Iterator<PersonContact> contactIterator = contacts.iterator();
        while (contactIterator.hasNext()) {
            final PersonContact item = contactIterator.next();
            if (item.getValue().isEmpty()) {
                contactIterator.remove();
            }
        }
    }

    @Override
    protected boolean saveNewDocument() {
        boolean result = false;
        try {
            User user = sessionManagement.getDAO(UserDAOHibernate.class, ApplicationHelper.USER_DAO).save(getDocument());
            if (user == null) {
                FacesContext.getCurrentInstance().addMessage(null, MessageHolder.MSG_CANT_SAVE);
            } else {
                setDocument(user);
                result = true;
            }
        } catch (Exception e) {
            result = false;
            FacesContext.getCurrentInstance().addMessage(null, MessageHolder.MSG_ERROR_ON_SAVE_NEW);
            e.printStackTrace();
        }
        return result;
    }

    @Inject
    @Named("sessionManagement")
    SessionManagementBean sessionManagement = new SessionManagementBean();

    private static final long serialVersionUID = 5947443099767481905L;
}