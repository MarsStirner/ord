package ru.hitsl.sql.dao.user;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.*;
import ru.entity.model.user.Group;
import ru.entity.model.user.User;
import ru.hitsl.sql.dao.GenericDAOHibernate;

import java.util.ArrayList;
import java.util.List;

public class UserDAOHibernate extends GenericDAOHibernate<User> implements UserDAO {

    @Override
    protected Class<User> getPersistentClass() {
        return User.class;
    }

    public UserDAOHibernate() {
    }

    /**
     * Получить самый простой критерий для отбора пользователей, без лишних FETCH
     * @return критерий для пользователей с DISTINCT
     */
    public DetachedCriteria getSimplestCriteria(){
        return DetachedCriteria.forClass(User.class, "this").setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
    }

    /**
     * Получить критерий для отбора пользоваетелей и их показа в расширенных списках
     * @return критерий для пользователей с DISTINCT, должностью, подразделением, контактами
     */
    public DetachedCriteria getListCriteria(){
        final DetachedCriteria result = getSimplestCriteria();
        result.createAlias("jobPosition", "jobPosition", CriteriaSpecification.LEFT_JOIN);
        result.createAlias("jobDepartment", "jobDepartment", CriteriaSpecification.LEFT_JOIN);
        result.createAlias("contacts", "contacts", CriteriaSpecification.LEFT_JOIN);
        return result;
    }

    /**
     * Получить критерий для отбора пользоваетелей с подтягиванием всех возможных полей
     * @return критерий для пользователей с DISTINCT, должностью, подразделением, контактами, группами, ролями, номенклатурой по-умолчанию
     */
    public DetachedCriteria getEagerCriteria(){
        final DetachedCriteria result = getListCriteria();
        //EAGER LOADING OF GROUPS, ROLES, defaultNomenclature, and accessLevels
        result.createAlias("groups", "groups", CriteriaSpecification.LEFT_JOIN);
        result.createAlias("roles", "roles", CriteriaSpecification.LEFT_JOIN);
        result.createAlias("defaultNomenclature", "defaultNomenclature", CriteriaSpecification.LEFT_JOIN);
        result.createAlias("maxUserAccessLevel", "maxUserAccessLevel", CriteriaSpecification.INNER_JOIN);
        result.createAlias("currentUserAccessLevel", "currentUserAccessLevel", CriteriaSpecification.LEFT_JOIN);
        return result;
    }


    /**
     * Получает список ВСЕХ пользователей для показа в расширенных списках
     * @return  список всех пользователей с LIST_CRITERIA
     */
    public List<User> findAllUsers() {
        return getHibernateTemplate().findByCriteria(getListCriteria());
    }


    /**
     * Возвращает пользователя (EAGER_CRITERIA) по логину и паролю.
     *
     * @param login    логин
     * @param password пароль
     * @return пользователь или null, если такового не существует
     */
    @Override
    public User findByLoginAndPassword(final String login, final String password) {
        if (StringUtils.isNotEmpty(login) && StringUtils.isNotEmpty(password)) {
            final DetachedCriteria detachedCriteria = getEagerCriteria();
            detachedCriteria.add(Restrictions.eq("login", login));
            detachedCriteria.add(Restrictions.eq("password", password));
            List<User> users = getHibernateTemplate().findByCriteria(detachedCriteria);
            if (!users.isEmpty()) {
                return users.get(0);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Возвращает пользователя (EAGER_CRITERIA) по логину
     *
     * @param login логин
     * @return пользователь или null, если такового не существует
     */
    @Override
    public User getByLogin(String login) {
        if (StringUtils.isNotEmpty(login)) {
            DetachedCriteria detachedCriteria = getEagerCriteria();
            detachedCriteria.add(Restrictions.eq("login", login));
            List<User> users = getHibernateTemplate().findByCriteria(detachedCriteria, -1, 1);
            if (!users.isEmpty()) {
                return users.get(0);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public User getItemById(final Integer id) {
        final DetachedCriteria detachedCriteria = getEagerCriteria().add(Restrictions.eq("id", id));
        final List<User> users = getHibernateTemplate().findByCriteria(detachedCriteria);
        if (!users.isEmpty()) {
            return users.iterator().next();
        }
        return null;
    }

    public User getItemByIdForListView(final Integer id){
        final DetachedCriteria detachedCriteria = getListCriteria().add(Restrictions.eq("id", id));
        final List<User> users = getHibernateTemplate().findByCriteria(detachedCriteria);
        if (!users.isEmpty()) {
            return users.iterator().next();
        }
        return null;
    }

    /**
     * Обрабатывает поисковый шаблон через ИЛИ (НУЖНА LIST_CRITERIA)   для диалогов
     * @param criteria критерий отбора
     * @param filter поисковый шаблон
     */
    public void applyFilterCriteriaForDialogs(final DetachedCriteria criteria, final String filter) {
        if (StringUtils.isNotEmpty(filter)) {
            final Disjunction disjunction = Restrictions.disjunction();
            disjunction.add(Restrictions.ilike("lastName", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("middleName", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("firstName", filter, MatchMode.ANYWHERE));
            criteria.add(disjunction);
        }
    }


    /**
     * Обрабатывает поисковый шаблон через ИЛИ (НУЖНА LIST_CRITERIA)
     * @param criteria критерий отбора
     * @param filter поисковый шаблон
     */
    public void applyFilterCriteria(final DetachedCriteria criteria, final String filter){
        if (StringUtils.isNotEmpty(filter)) {
            final Disjunction disjunction = Restrictions.disjunction();
            disjunction.add(Restrictions.ilike("lastName", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("middleName", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("firstName", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("jobPositionString", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("jobDepartmentString", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("email", filter, MatchMode.ANYWHERE));
            criteria.add(disjunction);
        }
    }


    @Override
    public List<User> findUsers(
            final String filter,
            final boolean showDeleted,
            final boolean showFired,
            final int first,
            final int pageSize,
            final String orderBy,
            final boolean orderAsc
    ) {
        final DetachedCriteria detachedCriteria = getListCriteria();
        applyFilterCriteria(detachedCriteria, filter);
        if (!showDeleted) {
            detachedCriteria.add(Restrictions.eq("deleted", false));
        }
        detachedCriteria.add(Restrictions.eq("fired", showFired));
        addOrder(detachedCriteria, orderBy, orderAsc);
        return getCorrectLimitingUsers(detachedCriteria, orderBy, orderAsc, first, pageSize);
    }

    @Override
    public long countUsers(final String pattern, final boolean showDeleted, final boolean showFired) {
        final DetachedCriteria detachedCriteria = getListCriteria();
        applyFilterCriteria(detachedCriteria, pattern);
        if (!showDeleted) {
            detachedCriteria.add(Restrictions.eq("deleted", false));
        }
        detachedCriteria.add(Restrictions.eq("fired", showFired));
        return getCountOf(detachedCriteria);
    }


    @Override
    public List<User> findUsersForDialog(
            final String filter,
            final boolean showDeleted,
            final boolean showFired,
            final int first,
            final int pageSize,
            final String orderBy,
            final boolean orderAsc
    ) {
        final DetachedCriteria detachedCriteria = getListCriteria();
        applyFilterCriteriaForDialogs(detachedCriteria, filter);
        if (!showDeleted) {
            detachedCriteria.add(Restrictions.eq("deleted", false));
        }
        detachedCriteria.add(Restrictions.eq("fired", showFired));
        addOrder(detachedCriteria, orderBy, orderAsc);
        return getCorrectLimitingUsers(detachedCriteria, orderBy, orderAsc, first, pageSize);
    }

    @Override
    public long countUsersForDialog(final String pattern, final boolean showDeleted, final boolean showFired) {
        final DetachedCriteria detachedCriteria = getListCriteria();
        applyFilterCriteriaForDialogs(detachedCriteria, pattern);
        if (!showDeleted) {
            detachedCriteria.add(Restrictions.eq("deleted", false));
        }
        detachedCriteria.add(Restrictions.eq("fired", showFired));
        return getCountOf(detachedCriteria);
    }

    /**
     * Получает список пользователей (SIMPLE_CRITERIA) по поисковому шаблону, с учетом уволенных\удаленных и принадлежащих заданной группе
     * @param pattern поисковый шаблон
     * @param showDeleted включать ли в список удаленных
     * @param showFired включать ли в список уволенных
     * @param group пользователи должны принадлежать заданной группе
     * @param first начиная с какого результата вернуть
     * @param pageSize сколько записей вернуть
     * @param orderBy  соритировка по
     * @param orderAsc порядок сортировки
     * @return список пользователей удовлетворяющий условиям
     */
    @Override
    public List<User> findUsersForDialogByGroup(
            final String pattern,
            final boolean showDeleted,
            final boolean showFired,
            final Group group,
            final int first,
            final int pageSize,
            final String orderBy,
            final boolean orderAsc
    ) {
        if(group == null){
            logger.error("Try to findUsersByGroup with NULL group. return findUsers()");
            return findUsersForDialog(pattern, showDeleted, showFired, first, pageSize, orderBy, orderAsc);
        }
        final DetachedCriteria criteria = getListCriteria();
        applyFilterCriteriaForDialogs(criteria, pattern);
        if (!showDeleted) {
            criteria.add(Restrictions.eq("deleted", false));
        }
        criteria.add(Restrictions.eq("fired", showFired));

        criteria.createAlias("groups", "groups", CriteriaSpecification.INNER_JOIN);
        criteria.add(Restrictions.eq("groups.id", group.getId()));
        addOrder(criteria, orderBy, orderAsc);
        return getCorrectLimitingUsers(criteria, orderBy, orderAsc, first, pageSize);
    }

    @Override
    public long countUsersForDialogByGroup(final String pattern, final boolean showDeleted, final boolean showFired, final Group group) {
        if(group == null){
            logger.error("Try to countUsersByGroup with NULL group. return countUsers()");
            return countUsersForDialog(pattern, showDeleted, showFired);
        }
        final DetachedCriteria criteria = getListCriteria();
        applyFilterCriteriaForDialogs(criteria, pattern);
        if (!showDeleted) {
            criteria.add(Restrictions.eq("deleted", false));
        }
        criteria.add(Restrictions.eq("fired", showFired));
        criteria.createAlias("groups", "groups", CriteriaSpecification.INNER_JOIN);
        criteria.add(Restrictions.eq("groups.id", group.getId()));
        return getCountOf(criteria);
    }

    /**
     * Получить список документов с корректными LIMIT
     *
     * @param criteria  изначальный критерий для отбора документов
     * @param sortField поле сортировки
     * @param sortOrder порядок сортировки
     * @param first     начальное смещение
     * @param pageSize  макс размер бвыбираемого списка
     * @return список документов заданного размера
     */
    @SuppressWarnings("unchecked")
    public List<User> getCorrectLimitingUsers(
            final DetachedCriteria criteria, final String sortField, final boolean sortOrder, final int first, final int pageSize
    ) {
        criteria.setProjection(Projections.distinct(Projections.id()));
        //получаем список ключей от сущностей, которые нам нужны (с корректным [LIMIT offset, count])
        final List ids = getHibernateTemplate().findByCriteria(criteria, first, pageSize);
        if (ids.isEmpty()) {
            return new ArrayList<User>(0);
        }
        //Ищем только по этим ключам с упорядочиванием
        return getHibernateTemplate().findByCriteria(getIDListCriteria(ids, sortField, sortOrder));
    }

    /**
     * Формирование запроса на поиск документов по списку идентификаторов
     *
     * @param ids      список идентифкаторов документов
     * @param orderBy  колонка для сортировки
     * @param orderAsc направление сортировки
     * @return запрос, с ограничениями на идентификаторы документов и сортировки
     */
    private DetachedCriteria getIDListCriteria(List ids, String orderBy, boolean orderAsc) {
        final DetachedCriteria result = getListCriteria().add(Restrictions.in("id", ids));
        if (StringUtils.isNotEmpty(orderBy)) {
            result.addOrder(orderAsc ? Order.asc(orderBy) : Order.desc(orderBy));
        }
        return result;
    }
}