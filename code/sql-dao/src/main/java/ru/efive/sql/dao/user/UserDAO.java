package ru.efive.sql.dao.user;

import ru.efive.sql.dao.GenericDAO;
import ru.entity.model.user.User;

import java.util.List;

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

    /**
     * Возвращает пользователя по email
     *
     * @param email адрес электронной почты
     * @return пользователь или null, если такового не существует
     */
    public User getByEmail(String email);

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