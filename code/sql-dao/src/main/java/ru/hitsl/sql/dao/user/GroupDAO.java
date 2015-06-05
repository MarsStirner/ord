package ru.hitsl.sql.dao.user;

import ru.entity.model.user.Group;
import ru.entity.model.user.Role;
import ru.entity.model.user.User;
import ru.hitsl.sql.dao.GenericDAO;

import java.util.List;

public interface GroupDAO extends GenericDAO<Group> {

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
    public List<User> findUsers(String pattern, boolean showDeleted, int offset, int count, String orderBy, boolean orderAsc);

    /**
     * Находит количество пользователей по маске
     *
     * @param pattern     маска поиска
     * @param showDeleted включает в выборку удалённых пользователей
     * @return количество зарегистрированных пользователей удовлетворяющих условию поиска
     */
    public long countUsers(String pattern, boolean showDeleted);

}