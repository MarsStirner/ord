package ru.efive.sql.dao.user;

import java.util.List;
import java.util.Set;

import ru.efive.sql.dao.GenericDAO;
import ru.entity.model.enums.PermissionType;
import ru.entity.model.user.Permission;
import ru.entity.model.user.Role;
import ru.entity.model.user.User;

public interface UserDAO extends GenericDAO<User> {

    /**
     * Возвращает пользователя по логину и паролю
     *
     * @param login    логин
     * @param password пароль
     * @return пользователь или null, если такового не существует
     */
    public User findByLoginAndPassword(String login, String password);

    /**
     * Возвращает пользователя по логину
     *
     * @param login логин
     * @return пользователь или null, если такового не существует
     */
    public User getByLogin(String login);

    public User getByLogin(String login, Integer excludeUserId);

    /**
     * Возвращает пользователя по email
     *
     * @param email адрес электронной почты
     * @return пользователь или null, если такового не существует
     */
    public User getByEmail(String email);

    public User getByEmail(String email, Integer excludeUserId);

    /**
     * Находит роли пользователей
     *
     * @param name        название роли
     * @param permissions разрешенные действия
     * @param offset      номер начального элемента списка
     * @param count       количество возвращаемых элементов
     * @param orderBy     поле для сортировки списка
     * @param asc         указывает направление сортировки. true = по возрастанию
     * @return коллекция ролей
     */
    public List<Role> findRoles(String name, Set<Permission> permissions, int offset, int count, String orderBy, boolean asc);

    /**
     * Считает количество ролей пользователей, зарегистрированных в системе
     *
     * @param name        название роли
     * @param permissions разрешенные действия
     * @return количество ролей пользователей, зарегистрированных в системе
     */
    public long countRoles(String name, Set<Permission> permissions);

    /**
     * Есть ли у пользователя user право permissionType
     *
     * @param user           пользователь
     * @param permissionType право
     * @return true - есть; false - доступ запрещен
     */
    public boolean hasPermission(User user, PermissionType permissionType);

    /**
     * Находит права(разрешения) в справочнике прав по типу и по названию
     *
     * @param name           название
     * @param permissionType тип права доступа
     * @return право доступа
     */
    public List<Permission> getPermission(String name, PermissionType permissionType);

    /**
     * Находит всех пользователей, удовлетворяющих условиям
     *
     * @param login       логин пользователя
     * @param firstname   имя пользователя
     * @param lastname    фамилия пользователя
     * @param middlename  отчество пользователя
     * @param email       адрес электронной почты пользователя
     * @param role        роль пользователя
     * @param showDeleted включает в выборку удалённых пользователей
     * @param offset      номер начального элемента списка
     * @param count       количество возвращаемых элементов
     * @param orderBy     поле для сортировки списка
     * @param orderAsc    указывает направление сортировки. true = по возрастанию
     * @return список пользователей удовлетворяющих условию поиска
     */
    public List<User> findUsers(String login, String firstname, String lastname, String middlename, String email, Role role, boolean showDeleted, int offset, int count, String orderBy, boolean orderAsc);

    /**
     * Находит количество пользователей, зарегистрированных в системе, удовлетворяющих условиям
     *
     * @param login       логин пользователя
     * @param firstname   имя пользователя
     * @param lastname    фамилия пользователя
     * @param middlename  отчество пользователя
     * @param email       адрес электронной почты пользователя
     * @param role        роль пользователя
     * @param showDeleted включает в выборку удалённых пользователей
     * @return количество зарегистрированных пользователей удовлетворяющих условию поиска
     */
    public long countUsers(String login, String firstname, String lastname, String middlename, String email, Role role, boolean showDeleted);


    /**
     * Находит всех пользователей по маске
     *
     * @param pattern     маска поиска
     * @param showDeleted включает в выборку удалённых пользователей
     * @param offset      номер начального элемента списка
     * @param count       количество возвращаемых элементов
     * @param orderBy     поле для сортировки списка
     * @param orderAsc    указывает направление сортировки. true = по возрастанию
     * @return список пользователей удовлетворяющих условию поиска
     */
    public List<User> findUsers(String pattern, boolean showDeleted, boolean showFired, int offset, int count, String orderBy, boolean orderAsc);

    /**
     * Находит количество пользователей по маске
     *
     * @param pattern     маска поиска
     * @param showDeleted включает в выборку удалённых пользователей
     * @return количество зарегистрированных пользователей удовлетворяющих условию поиска
     */
    public long countUsers(String pattern, boolean showDeleted, boolean showFired);

}