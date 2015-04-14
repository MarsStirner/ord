package ru.efive.sql.dao.user;

import org.apache.commons.lang.StringUtils;
import org.hibernate.FetchMode;
import org.hibernate.criterion.*;
import ru.efive.sql.dao.GenericDAOHibernate;
import ru.entity.model.user.Group;
import ru.entity.model.user.User;

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
    public DetachedCriteria getSimpliestCriteria(){
        return DetachedCriteria.forClass(getPersistentClass()).setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
    }

    /**
     * Получить критерий для отбора пользоваетелей и их показа в расширенных списках
     * @return критерий для пользователей с DISTINCT, должностью, подразделением, контактами
     */
    public DetachedCriteria getListCriteria(){
        final DetachedCriteria result = getSimpliestCriteria();
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
        //EAGER LOADING OF GROUPS
        result.setFetchMode("groups", FetchMode.JOIN);
        result.setFetchMode("roles", FetchMode.JOIN);
        result.setFetchMode("defaultNomenclature", FetchMode.JOIN);
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
     * Получает список ВСЕХ пользователей без лишних FETCH
     * @param showDeleted TODO check
     * @param showFired  TODO check
     * @return  список всех пользователей с SIMPLE_CRITERIA
     */
    public List<User> findAllUsers(boolean showDeleted, boolean showFired) {
        final DetachedCriteria in_searchCriteria = getSimpliestCriteria();
        if (showDeleted) {
            in_searchCriteria.add(Restrictions.eq("deleted", true));
        }
        if (showFired) {
            in_searchCriteria.add(Restrictions.eq("fired", true));
        }

        return getHibernateTemplate().findByCriteria(in_searchCriteria);
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
     * Возвращает пользователя (SIMPLE_CRITERIA) по логину
     *
     * @param login логин
     * @return пользователь или null, если такового не существует
     */
    @Override
    public User getByLogin(String login) {
        if (StringUtils.isNotEmpty(login)) {
            DetachedCriteria detachedCriteria = getSimpliestCriteria();
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


    public List<User> findUsers(final String pattern, boolean showDeleted, boolean showFired) {
        final DetachedCriteria in_searchCriteria = getListCriteria();
        addPatternCriteria(in_searchCriteria, pattern);
        if (showDeleted) {
            in_searchCriteria.add(Restrictions.eq("deleted", true));
        }

        if (showFired) {
            in_searchCriteria.add(Restrictions.eq("fired", true));
        }

        return getHibernateTemplate().findByCriteria(in_searchCriteria);
    }

    /**
     * Обрабатывает поисковый шаблон через ИЛИ (НУЖНА LIST_CRITERIA)
     * @param searchCriteria критерий отбора
     * @param pattern поисковый шаблон
     */
    private void addPatternCriteria(final DetachedCriteria searchCriteria, final String pattern) {
        if (StringUtils.isNotEmpty(pattern)) {
            final String likePattern = pattern.concat("%");
            final Disjunction disjunction = Restrictions.disjunction();
            disjunction.add(Restrictions.ilike("lastName", likePattern));
            disjunction.add(Restrictions.ilike("middleName", likePattern));
            disjunction.add(Restrictions.ilike("firstName", likePattern));
            disjunction.add(Restrictions.ilike("email", likePattern));
            disjunction.add(Restrictions.ilike("contacts.value", likePattern));
            disjunction.add(Restrictions.ilike("jobPosition.value", likePattern));
            disjunction.add(Restrictions.ilike("jobDepartment.value", likePattern));
            searchCriteria.add(disjunction);
        }
    }


    @Override
    public List<User> findUsers(final String pattern, final boolean showDeleted, final boolean showFired, final int
            offset, final int count, final String orderBy, final boolean orderAsc) {
        final DetachedCriteria detachedCriteria = getListCriteria();
        addPatternCriteria(detachedCriteria, pattern);
        if (!showDeleted) {
            detachedCriteria.add(Restrictions.eq("deleted", false));
        }
        if (!showFired) {
            detachedCriteria.add(Restrictions.eq("fired", false));
        }
        detachedCriteria.setProjection(Projections.distinct(Projections.id()));
        String[] ords = orderBy == null ? null : orderBy.split(",");
        if (ords != null) {
            if (ords.length > 1) {
                addOrder(detachedCriteria, ords, orderAsc);
            } else {
                addOrder(detachedCriteria, orderBy, orderAsc);
            }
        }
        //получаем список ключей от сущностей, которые нам нужны (с корректным [LIMIT offset, count])
        List ids = getHibernateTemplate().findByCriteria(detachedCriteria, offset, count);
        if (ids.isEmpty()) {
            return new ArrayList<User>(0);
        }
        //Ищем только по этим ключам с упорядочиванием
        DetachedCriteria resultCriteria = getListCriteria().add(Restrictions.in("id", ids));
        if (ords != null) {
            if (ords.length > 1) {
                addOrder(resultCriteria, ords, orderAsc);
            } else {
                addOrder(resultCriteria, orderBy, orderAsc);
            }
        }
        resultCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
        return getHibernateTemplate().findByCriteria(resultCriteria);
    }

    @Override
    public long countUsers(final String pattern, final boolean showDeleted, final boolean showFired) {
        DetachedCriteria detachedCriteria = getListCriteria();
        addPatternCriteria(detachedCriteria, pattern);
        if (!showDeleted) {
            detachedCriteria.add(Restrictions.eq("deleted", false));
        }
        if (!showFired) {
            detachedCriteria.add(Restrictions.eq("fired", false));
        }

        return getCountOf(detachedCriteria);
    }


    /**
     * {@inheritDoc}
     */
    public long countFiredUsers(String pattern, boolean showDeleted) {
        DetachedCriteria detachedCriteria = getListCriteria();
        detachedCriteria.add(Restrictions.and(Restrictions.isNotNull("fired"), Restrictions.eq("fired", true)));
        addPatternCriteria(detachedCriteria, pattern);
        if (showDeleted) {
            detachedCriteria.add(Restrictions.eq("deleted", true));
        }

        return getCountOf(detachedCriteria);
    }

    /**
     * {@inheritDoc}
     */
    public List<User> findFiredUsers(String pattern, boolean showDeleted, int offset, int count, String orderBy,
                                     boolean orderAsc) {
        DetachedCriteria detachedCriteria = getListCriteria();
        addPatternCriteria(detachedCriteria, pattern);
        if (!showDeleted) {
            detachedCriteria.add(Restrictions.eq("deleted", false));
        }
        detachedCriteria.add(Restrictions.eq("fired", true));
        detachedCriteria.setProjection(Projections.distinct(Projections.id()));
        String[] ords = orderBy == null ? null : orderBy.split(",");
        if (ords != null) {
            if (ords.length > 1) {
                addOrder(detachedCriteria, ords, orderAsc);
            } else {
                addOrder(detachedCriteria, orderBy, orderAsc);
            }
        }
        //получаем список ключей от сущностей, которые нам нужны (с корректным [LIMIT offset, count])
        List ids = getHibernateTemplate().findByCriteria(detachedCriteria, offset, count);
        if (ids.isEmpty()) {
            return new ArrayList<User>(0);
        }
        //Ищем только по этим ключам с упорядочиванием
        DetachedCriteria resultCriteria = getListCriteria().add(Restrictions.in("id", ids));
        if (ords != null) {
            if (ords.length > 1) {
                addOrder(resultCriteria, ords, orderAsc);
            } else {
                addOrder(resultCriteria, orderBy, orderAsc);
            }
        }
        resultCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
        return getHibernateTemplate().findByCriteria(resultCriteria);
    }


    public User getUser(final Integer id) {
        final DetachedCriteria detachedCriteria = getEagerCriteria().add(Restrictions.eq("id", id));
        final List<User> users = getHibernateTemplate().findByCriteria(detachedCriteria);
        if (!users.isEmpty()) {
            final User result = users.iterator().next();
            //TODO  http://stackoverflow.com/questions/18753245/one-to-many-relationship-gets-duplicate-objects
            // -whithout-using-distinct-why
            /**
             * Want to know why the duplicates are there?
             * Look at the SQL resultset, Hibernate does not hide these duplicates on the left side of the outer
             * joined result
             * but returns all the duplicates of the driving table. If you have 5 orders in the database,
             * and each order has 3 line items, the resultset will be 15 rows.
             * The Java result list of these queries will have 15 elements, all of type Order.
             * Only 5 Order instances will be created by Hibernate,
             * but duplicates of the SQL resultset are preserved as duplicate references to these 5 instances
             */
            //ГОРИ ОНО ВСЕ ОГНЕМ!!!!!!!!!!!!!!!!!!!!!
            getUserContacts(result);
            return result;
        }
        return null;
    }

    private void getUserContacts(User user) {
        final  DetachedCriteria detachedCriteria = getSimpliestCriteria().add(Restrictions.eq("person", user));
        user.getContacts().clear();
        user.getContacts().addAll(getHibernateTemplate().findByCriteria(detachedCriteria));
    }

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
    @Override
    public List<User> findUsersByGroup(final String pattern, final boolean showDeleted, final boolean showFired,final Group group, final int
            offset, final int count, final String orderBy, final boolean orderAsc) {
        if(group == null){
            logger.error("Try to findUsersByGroup with NULL group. return findUsers()");
            return findUsers(pattern, showDeleted, showFired, offset, count, orderBy, orderAsc);
        }
        final DetachedCriteria criteria = getListCriteria();
        addPatternCriteria(criteria, pattern);
        if (!showDeleted) {
            criteria.add(Restrictions.eq("deleted", false));
        }
        if (!showFired) {
            criteria.add(Restrictions.eq("fired", false));
        }
        criteria.createAlias("groups", "groups", CriteriaSpecification.INNER_JOIN);
        criteria.add(Restrictions.eq("groups.id", group.getId()));
        criteria.setProjection(Projections.distinct(Projections.id()));
        String[] ords = orderBy == null ? null : orderBy.split(",");
        if (ords != null) {
            if (ords.length > 1) {
                addOrder(criteria, ords, orderAsc);
            } else {
                addOrder(criteria, orderBy, orderAsc);
            }
        }
        //получаем список ключей от сущностей, которые нам нужны (с корректным [LIMIT offset, count])
        List ids = getHibernateTemplate().findByCriteria(criteria, offset, count);
        if (ids.isEmpty()) {
            return new ArrayList<User>(0);
        }
        //Ищем только по этим ключам с упорядочиванием
        final DetachedCriteria resultCriteria = getListCriteria().add(Restrictions.in("id", ids));
        if (ords != null) {
            if (ords.length > 1) {
                addOrder(resultCriteria, ords, orderAsc);
            } else {
                addOrder(resultCriteria, orderBy, orderAsc);
            }
        }
        return getHibernateTemplate().findByCriteria(resultCriteria);
    }

    @Override
    public long countUsersByGroup(final String pattern, final boolean showDeleted, final boolean showFired, final Group group) {
        if(group == null){
            logger.error("Try to countUsersByGroup with NULL group. return countUsers()");
            return countUsers(pattern, showDeleted, showFired);
        }
        final DetachedCriteria criteria = getListCriteria();
        addPatternCriteria(criteria, pattern);
        if (!showDeleted) {
            criteria.add(Restrictions.eq("deleted", false));
        }
        if (!showFired) {
            criteria.add(Restrictions.eq("fired", false));
        }
        criteria.createAlias("groups", "groups", CriteriaSpecification.INNER_JOIN);
        criteria.add(Restrictions.eq("groups.id", group.getId()));
        return getCountOf(criteria);
    }
}