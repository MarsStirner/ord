package ru.efive.sql.dao.user;

import ru.efive.sql.dao.GenericDAO;
import ru.entity.model.user.Group;
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
    public List<User> findUsersForDialog(String pattern, boolean showDeleted, boolean showFired, int offset, int count, String orderBy, boolean orderAsc);

    /**
     * Находит количество пользователей по маске
     *
     * @param pattern     маска поиска
     * @param showDeleted включает в выборку удалённых пользователей
     * @return количество зарегистрированных пользователей удовлетворяющих условию поиска
     */
    public long countUsersForDialog(String pattern, boolean showDeleted, boolean showFired);


    /**
     * Получает список пользователей (SIMPLE_CRITERIA) по поисковому шаблону, с учетом уволенных\удаленных и принадлежащих заданной группе
     * @param pattern поисковый шаблон
     * @param showDeleted включать ли в список удаленных
     * @param showFired включать ли в список уволенных
     * @param group пользователи должны принадлежать заданной группе
     * @param offset начиная с какого результата вернуть
     * @param count сколько записей вернуть
     * @param orderBy  соритировка по
     * @param orderAsc порядок сортировки
     * @return список пользователей удовлетворяющий условиям
     */
    public List<User> findUsersForDialogByGroup(final String pattern, final boolean showDeleted, final boolean showFired,final Group group, final int
            offset, final int count, final String orderBy, final boolean orderAsc);


    /**
     * Находит количество пользователей в заданной группе по маске
     * @param pattern     маска поиска
     * @param showDeleted включает в выборку удалённых пользователей
     * @return количество зарегистрированных пользователей удовлетворяющих условию поиска
     */
    public long countUsersForDialogByGroup(final String pattern, final boolean showDeleted, final boolean showFired, final Group group);
}