package ru.efive.sql.dao.user;

import org.apache.commons.lang.StringUtils;
import org.hibernate.FetchMode;
import org.hibernate.criterion.*;
import ru.efive.sql.dao.GenericDAOHibernate;
import ru.entity.model.user.PersonContact;
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

    public List<User> findAllUsers() {
        DetachedCriteria in_searchCriteria = DetachedCriteria.forClass(getPersistentClass());
        in_searchCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
        in_searchCriteria.setFetchMode("jobPosition", FetchMode.JOIN);
        in_searchCriteria.setFetchMode("jobDepartment", FetchMode.JOIN);
        in_searchCriteria.setFetchMode("contacts", FetchMode.JOIN);
        return getHibernateTemplate().findByCriteria(in_searchCriteria);
    }

    public List<User> findAllUsers(boolean showDeleted, boolean showFired) {
        DetachedCriteria in_searchCriteria = DetachedCriteria.forClass(getPersistentClass());
        in_searchCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

        if (showDeleted) {
            in_searchCriteria.add(Restrictions.eq("deleted", true));
        }

        if (showFired) {
            in_searchCriteria.add(Restrictions.eq("fired", true));
        }

        return getHibernateTemplate().findByCriteria(in_searchCriteria);
    }

    /**
     * Возвращает пользователя по логину и паролю.
     *
     * @param login    логин
     * @param password пароль
     * @return пользователь или null, если такового не существует
     */
    @Override
    public User findByLoginAndPassword(final String login, final String password) {
        if (StringUtils.isNotEmpty(login) && StringUtils.isNotEmpty(password)) {
            DetachedCriteria detachedCriteria = DetachedCriteria.forClass(User.class);
            detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

            detachedCriteria.add(Restrictions.eq("login", login));
            detachedCriteria.add(Restrictions.eq("password", password));
            //EAGER LOADING OF GROUPS
            detachedCriteria.setFetchMode("groups", FetchMode.JOIN);
            detachedCriteria.setFetchMode("roles", FetchMode.JOIN);

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
     * Возвращает пользователя по логину
     *
     * @param login логин
     * @return пользователь или null, если такового не существует
     */
    @Override
    public User getByLogin(String login) {
        if (StringUtils.isNotEmpty(login)) {
            DetachedCriteria detachedCriteria = DetachedCriteria.forClass(User.class);
            detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

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


    /**
     * Возвращает пользователя по email
     *
     * @param email адрес электронной почты
     * @return пользователь или null, если такового не существует
     */
    public User getByEmail(String email) {
        if (StringUtils.isNotEmpty(email)) {
            DetachedCriteria detachedCriteria = DetachedCriteria.forClass(User.class);
            detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

            detachedCriteria.add(Restrictions.eq("email", email));

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
        DetachedCriteria in_searchCriteria = DetachedCriteria.forClass(getPersistentClass());
        in_searchCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
        addPatternCriteria(in_searchCriteria, pattern);
        if (showDeleted) {
            in_searchCriteria.add(Restrictions.eq("deleted", true));
        }

        if (showFired) {
            in_searchCriteria.add(Restrictions.eq("fired", true));
        }

        return getHibernateTemplate().findByCriteria(in_searchCriteria);
    }

    private void addPatternCriteria(final DetachedCriteria searchCriteria, final String pattern) {
        if (StringUtils.isNotEmpty(pattern)) {
            final String likePattern = pattern.concat("%");
            final Disjunction disjunction = Restrictions.disjunction();
            disjunction.add(Restrictions.ilike("lastName", likePattern));
            disjunction.add(Restrictions.ilike("middleName", likePattern));
            disjunction.add(Restrictions.ilike("firstName", likePattern));
            disjunction.add(Restrictions.ilike("email", likePattern));
            searchCriteria.createAlias("contacts", "contacts", CriteriaSpecification.LEFT_JOIN);
            disjunction.add(Restrictions.ilike("contacts.value", likePattern));
            searchCriteria.createAlias("jobPosition", "jobPosition", CriteriaSpecification.LEFT_JOIN);
            disjunction.add(Restrictions.ilike("jobPosition.value", likePattern));
            searchCriteria.createAlias("jobDepartment", "jobDepartment", CriteriaSpecification.LEFT_JOIN);
            disjunction.add(Restrictions.ilike("jobDepartment.value", likePattern));
            searchCriteria.add(disjunction);
        }
    }


    @Override
    public List<User> findUsers(final String pattern, final boolean showDeleted, final boolean showFired, final int
            offset, final int count, final String orderBy, final boolean orderAsc) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(User.class);
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
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
        DetachedCriteria resultCriteria = DetachedCriteria.forClass(getPersistentClass()).add(Restrictions.in("id",
                ids));
        //EAGER LOADING
        resultCriteria.setFetchMode("jobPosition", FetchMode.JOIN);
        resultCriteria.setFetchMode("jobDepartment", FetchMode.JOIN);
        resultCriteria.setFetchMode("contacts", FetchMode.JOIN);
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
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(User.class);
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
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
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(User.class);
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
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
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(User.class);
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
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
        DetachedCriteria resultCriteria = DetachedCriteria.forClass(getPersistentClass()).add(Restrictions.in("id",
                ids));
        //EAGER LOADING
        resultCriteria.setFetchMode("jobPosition", FetchMode.JOIN);
        resultCriteria.setFetchMode("jobDepartment", FetchMode.JOIN);
        resultCriteria.setFetchMode("contacts", FetchMode.JOIN);
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
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
        detachedCriteria.setFetchMode("jobPosition", FetchMode.JOIN);
        detachedCriteria.setFetchMode("jobDepartment", FetchMode.JOIN);
        detachedCriteria.setFetchMode("contacts", FetchMode.JOIN);
        detachedCriteria.setFetchMode("defaultNomenclature", FetchMode.JOIN);
        detachedCriteria.add(Restrictions.eq("id", id));
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
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(PersonContact.class);
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
        detachedCriteria.add(Restrictions.eq("person", user));
        user.getContacts().clear();
        user.getContacts().addAll(getHibernateTemplate().findByCriteria(detachedCriteria));
    }
}