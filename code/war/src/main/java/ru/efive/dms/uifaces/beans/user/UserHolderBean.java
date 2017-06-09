package ru.efive.dms.uifaces.beans.user;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentHolderBean;
import ru.efive.dms.uifaces.beans.annotations.ViewScopedController;
import ru.entity.model.referenceBook.RbContactPointSystem;
import ru.entity.model.user.ContactPoint;
import ru.entity.model.user.User;
import ru.hitsl.sql.dao.interfaces.UserDao;
import ru.hitsl.sql.dao.interfaces.referencebook.ContactInfoTypeDao;
import ru.hitsl.sql.dao.util.AuthorizationData;

import javax.faces.event.ValueChangeEvent;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ViewScopedController("user")
public class UserHolderBean extends AbstractDocumentHolderBean<User, UserDao> {

    @Autowired
    @Qualifier("contactInfoTypeDao")
    private ContactInfoTypeDao contactInfoTypeDao;

    private List<ContactPoint> contactList;

    public UserHolderBean(@Qualifier("userDao") final UserDao dao, @Qualifier("authData") final AuthorizationData authData) {
        super(dao, authData);
    }

    public List<ContactPoint> getContactList() {
        return contactList;
    }

    public void setContactList(final List<ContactPoint> contactList) {
        this.contactList = contactList;
    }

    @Override
    public boolean isCanCreate(final AuthorizationData authData) {
        return authData.isAdministrator() || authData.isHr();
    }

    @Override
    public boolean isCanUpdate(final AuthorizationData authData) {
        return authData.isAdministrator() || authData.isHr() || authData.getAuthorized().equals(getDocument());
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
    public void changeFired() {
        final User user = getDocument();
        final LocalDateTime now = LocalDateTime.now();
        if (user.isFired()) {
            //Восстанавливаем уволенного сотрудника
            user.hire(now);
        } else {
            //Увольняем сотрудника
            user.fire(now);
        }
        dao.update(user);
    }

    /**
     * Удаление контакта из модели \ бина
     *
     * @param contact контакт, который надо удалить
     * @return успешность удаления
     */
    public boolean deleteContact(final ContactPoint contact) {
        return getContactList().remove(contact);

    }

    /**
     * Добавление нового контакта в модель
     *
     * @return успешность добавления
     */
    public boolean addContact() {
        final ContactPoint newContact = new ContactPoint();
        newContact.setUser(getDocument());
        final List<RbContactPointSystem> typeList = contactInfoTypeDao.getItems();
        if (!typeList.isEmpty()) {
            newContact.setSystem(typeList.get(0));
            return getContactList().add(newContact);
        }
        return false;
    }

    @Override
    protected User newModel(AuthorizationData authData) {
        final User user = new User();
        user.setCreated(LocalDateTime.now());
        user.setDeleted(false);
        user.setFired(false);
        contactList = new ArrayList<>(5);
        return user;
    }

    @Override
    public boolean afterRead(User document, AuthorizationData authData) {
        final Set<ContactPoint> contacts = document.getTelecom();
        if (contacts != null && !contacts.isEmpty()) {
            contactList = new ArrayList<>(contacts);
        } else {
            contactList = new ArrayList<>(5);
        }
        return true;
    }


    @Override
    public boolean beforeCreate(final User document, final AuthorizationData authData) {
        //Удаляем пустые контактные данные, введенные пользователем
        contactList.removeIf(item -> StringUtils.isEmpty(item.getValue()));
        document.setTelecom(new HashSet<>(contactList));
        document.setCurrentUserAccessLevel(document.getMaxUserAccessLevel());
        return true;
    }

}