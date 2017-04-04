package ru.efive.dms.uifaces.beans.user;

import com.github.javaplugs.jsf.SpringScopeView;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentHolderBean;
import ru.efive.dms.uifaces.beans.utils.MessageHolder;
import ru.entity.model.referenceBook.ContactInfoType;
import ru.entity.model.user.PersonContact;
import ru.entity.model.user.User;
import ru.hitsl.sql.dao.interfaces.UserDao;
import ru.hitsl.sql.dao.interfaces.referencebook.ContactInfoTypeDao;
import ru.hitsl.sql.dao.util.AuthorizationData;
import ru.util.ApplicationHelper;

import javax.faces.event.ValueChangeEvent;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.*;

@Controller("user")
@SpringScopeView
public class UserHolderBean extends AbstractDocumentHolderBean<User> {
    private static final Logger LOGGER = LoggerFactory.getLogger("USER");


    @Autowired
    @Qualifier("authData")
    private AuthorizationData authData;
    @Autowired
    @Qualifier("userDao")
    private UserDao userDao;
    @Autowired
    @Qualifier("contactInfoTypeDao")
    private ContactInfoTypeDao contactInfoTypeDao;

    private List<PersonContact> contactList;

    public List<PersonContact> getContactList() {
        return contactList;
    }

    public void setContactList(final List<PersonContact> contactList) {
        this.contactList = contactList;
    }

    @Override
    public boolean isCanCreate() {
        if (isErrorState()) {
            LOGGER.error("TRY TO CREATE ON ErrorState");
            return false;
        }
        final User loggedUser = authData.getAuthorized();
        if (!loggedUser.isAdministrator() && !loggedUser.isHr()) {
            LOGGER.error("User[{}] try to create new User without permission. Restricted!", loggedUser.getId());
            return false;
        } else {
            return true;
        }
    }

    @Override
    public boolean isCanEdit() {
        if (isErrorState()) {
            LOGGER.error("TRY TO EDIT ON ErrorState");
            return false;
        }
        final User loggedUser = authData.getAuthorized();
        if (!loggedUser.isAdministrator() && !loggedUser.isHr() && !isOwner()) {
            LOGGER.error("User[{}] try to edit User[{}] info without permission. Restricted!", loggedUser.getId(), getDocumentId());
            return false;
        } else {
            return true;
        }
    }

    public boolean isEditPermission() {
        final User loggedUser = authData.getAuthorized();
        return loggedUser.isAdministrator() || loggedUser.isHr();
    }

    /**
     * Меняет пароль у открытого на редактирование пользователя (+ хэширует при setPassword)
     */
    public void changePassword(ValueChangeEvent event) {
        final String value = (String) event.getNewValue();
        if (StringUtils.isNotEmpty(value)) {
            getDocument().setPassword(value);
            event.getComponent().setTransient(true);
        }
    }

    /**
     * Обработчик нажатия на кнопку "Уволить\Восстановить", с сохранением изменений в БД
     *
     * @return флаг удаления
     */
    public boolean changeFired() {
        final User user = getDocument();
        final LocalDateTime now = LocalDateTime.now();
        if (user.isFired()) {
            //Восстанавливаем уволенного сотрудника
            user.hire(now);
        } else {
            //Увольняем сотрудника
            user.fire(now);
        }
        userDao.save(user);
        return user.isFired();
    }

    /**
     * Удаление контакта из модели \ бина
     *
     * @param contact контакт, который надо удалить
     * @return успешность удаления
     */
    public boolean deleteContact(final PersonContact contact) {
        return getContactList().remove(contact);

    }

    /**
     * Добавление нового контакта в модель
     *
     * @return успешность добавления
     */
    public boolean addContact() {
        final PersonContact newContact = new PersonContact();
        newContact.setPerson(getDocument());
        final List<ContactInfoType> typeList = contactInfoTypeDao.getItems();
        if (!typeList.isEmpty()) {
            newContact.setType(typeList.get(0));
            return getContactList().add(newContact);
        }
        return false;
    }

    @Override
    protected boolean deleteDocument() {
        if (isCanEdit()) {
            try {
                final User document = getDocument();
                LOGGER.debug("Delete User[{}]", document.getId());
                document.setDeleted(true);
                userDao.save(getDocument());
                return document.isDeleted();
            } catch (Exception e) {
                addMessage(null, MessageHolder.MSG_CANT_DELETE);
            }
        } else {
            LOGGER.error("TRY TO DELETE without permission");
        }
        return false;
    }


    @Override
    protected void initDocument(Integer id) {
        LOGGER.info("Open User[{}]", id);
        setDocument(userDao.get(id));
        final Set<PersonContact> contacts = getDocument().getContacts();
        if (contacts != null && !contacts.isEmpty()) {
            contactList = new ArrayList<>(contacts);
        } else {
            contactList = new ArrayList<>(5);
        }
    }

    @Override
    protected void initNewDocument() {
        LOGGER.info("Create new user");
        final User user = new User();
        user.setCreated(LocalDateTime.now());
        user.setDeleted(false);
        user.setFired(false);
        setDocument(user);
        contactList = new ArrayList<>(5);
    }

    @Override
    protected boolean saveDocument() {
        LOGGER.info("Save changes to User[{}]", getDocument().getId());
        try {
            //Удаляем пустые контактные данные, введенные пользователем
            removeEmptyContacts(contactList);
            getDocument().getContacts().clear();
            getDocument().getContacts().addAll(new HashSet<>(contactList));
            userDao.update(getDocument());
            return true;
        } catch (Exception e) {
            LOGGER.error("Error on save:", e);
            addMessage(null, MessageHolder.MSG_ERROR_ON_SAVE);
            return false;
        }
    }

    /**
     * Удаляем незаполненные контактные данные
     *
     * @param contacts список контактных данных
     */
    private void removeEmptyContacts(Collection<PersonContact> contacts) {
        contacts.removeIf(item -> StringUtils.isEmpty(item.getValue()));
    }

    @Override
    protected boolean saveNewDocument() {
        LOGGER.info("Save new User");
        try {
            //Удаляем пустые контактные данные, введенные пользователем
            removeEmptyContacts(contactList);
            getDocument().getContacts().clear();
            getDocument().getContacts().addAll(new HashSet<>(contactList));
            userDao.save(getDocument());
            return true;
        } catch (Exception e) {
            LOGGER.error("Error on save new:", e);
            addMessage(null, MessageHolder.MSG_ERROR_ON_SAVE_NEW);
            return false;
        }
    }

    /**
     * Является ли текущий пользователь владельцем карточки (сам открыл себя)
     *
     * @return true- текущий пользователь открыл свою карточку
     */
    public boolean isOwner() {
        return authData.getAuthorized().equals(getDocument());
    }
}