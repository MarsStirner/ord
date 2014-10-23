package ru.efive.sql.dao.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.hibernate.FetchMode;
import org.hibernate.criterion.*;

import ru.efive.sql.dao.GenericDAOHibernate;
import ru.entity.model.enums.PermissionType;
import ru.entity.model.user.Permission;
import ru.entity.model.user.PersonContact;
import ru.entity.model.user.Role;
import ru.entity.model.user.User;
import ru.util.ApplicationHelper;

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

            List<User> users = getHibernateTemplate().findByCriteria(detachedCriteria, -1, 1);
            if ((users != null) && !users.isEmpty()) {
                // don't modify this routine work! (for proxy caching such objects as Personage, Location)
                User user = users.get(0);
                if (user != null) {
                    // todo caching proxy objects
                }

                return user;
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
            if ((users != null) && !users.isEmpty()) {
                // don't modify this routine work! (for proxy caching such objects as Role, etc.)
                User user = users.get(0);
                if (user != null) {

                }
                return user;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public User getByLogin(String login, Integer excludeUserId) {
        if (StringUtils.isNotEmpty(login)) {
            DetachedCriteria detachedCriteria = DetachedCriteria.forClass(User.class);
            detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

            detachedCriteria.add(Restrictions.eq("login", login));
            detachedCriteria.add(Restrictions.ne("id", excludeUserId));

            List<User> users = getHibernateTemplate().findByCriteria(detachedCriteria, -1, 1);
            if ((users != null) && !users.isEmpty()) {
                // don't modify this routine work! (for proxy caching such objects as Role, etc.)
                User user = users.get(0);
                if (user != null) {

                }
                return user;
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
            if ((users != null) && !users.isEmpty()) {
                return users.get(0);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public User getByEmailName(String emailName) {
        if (StringUtils.isNotEmpty(emailName)) {
            DetachedCriteria detachedCriteria = DetachedCriteria.forClass(User.class);
            detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

            detachedCriteria.add(Restrictions.ilike("email", emailName));

            List<User> users = getHibernateTemplate().findByCriteria(detachedCriteria, -1, 1);
            if ((users != null) && !users.isEmpty()) {
                return users.get(0);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public User getByEmail(String email, Integer excludeUserId) {
        if (StringUtils.isNotEmpty(email)) {
            DetachedCriteria detachedCriteria = DetachedCriteria.forClass(User.class);
            detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

            detachedCriteria.add(Restrictions.eq("email", email));
            detachedCriteria.add(Restrictions.ne("id", excludeUserId));

            List<User> users = getHibernateTemplate().findByCriteria(detachedCriteria, -1, 1);
            if ((users != null) && !users.isEmpty()) {
                return users.get(0);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    private DetachedCriteria createCriteriaForRoles(String name, Set<Permission> permissions) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(Role.class);
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

        if (StringUtils.isNotEmpty(name)) {
            detachedCriteria.add(Restrictions.eq("name", name));
        }

        if ((permissions != null) && !permissions.isEmpty()) {
            detachedCriteria.add(Restrictions.eq("permissions", permissions));
        }

        return detachedCriteria;
    }

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
    public List<Role> findRoles(String name, Set<Permission> permissions, int offset, int count, String orderBy, boolean asc) {
        DetachedCriteria detachedCriteria = createCriteriaForRoles(name, permissions);

        if (StringUtils.isNotEmpty(orderBy)) {
            detachedCriteria.addOrder(asc ? Order.asc(orderBy) : Order.desc(orderBy));
        }

        return getHibernateTemplate().findByCriteria(detachedCriteria, offset, count);
    }

    /**
     * Считает количество ролей пользователей, зарегистрированных в системе
     *
     * @param name        название роли
     * @param permissions разрешенные действия
     * @return количество ролей пользователей, зарегистрированных в системе
     */
    public long countRoles(String name, Set<Permission> permissions) {
        DetachedCriteria detachedCriteria = createCriteriaForRoles(name, permissions);
        return getCountOf(detachedCriteria);
    }

    private DetachedCriteria createCriteriaForUsers(String login, String firstname, String lastname, String middlename, String email, Role role, boolean showDeleted) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(User.class);
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

        if (StringUtils.isNotEmpty(login)) {
            detachedCriteria.add(Restrictions.ilike("login", login + "%"));
        }

        if (StringUtils.isNotEmpty(lastname)) {
            detachedCriteria.add(Restrictions.ilike("lastname", lastname + "%"));
        }

        if (StringUtils.isNotEmpty(firstname)) {
            detachedCriteria.add(Restrictions.ilike("firstname", firstname + "%"));
        }

        if (StringUtils.isNotEmpty(middlename)) {
            detachedCriteria.add(Restrictions.ilike("middlename", middlename + "%"));
        }

        if (StringUtils.isNotEmpty(email)) {
            detachedCriteria.add(Restrictions.ilike("email", email + "%"));
        }

        if (role != null) {
            detachedCriteria.add(Restrictions.eq("role", role));
        }

        if (!showDeleted)
            detachedCriteria.add(Restrictions.or(Restrictions.isNull("deleted"), Restrictions.eq("deleted", false)));

        return detachedCriteria;
    }

    private DetachedCriteria createCriteriaForUsers(String login, String firstname, String lastname, String middlename, String email, String jobPosition, String jobDepartment, Role role, boolean showDeleted) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(User.class);
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

        if (StringUtils.isNotEmpty(login)) {
            detachedCriteria.add(Restrictions.ilike("login", login + "%"));
        }

        if (StringUtils.isNotEmpty(lastname)) {
            detachedCriteria.add(Restrictions.ilike("lastname", lastname + "%"));
        }

        if (StringUtils.isNotEmpty(firstname)) {
            detachedCriteria.add(Restrictions.ilike("firstname", firstname + "%"));
        }

        if (StringUtils.isNotEmpty(middlename)) {
            detachedCriteria.add(Restrictions.ilike("middlename", middlename + "%"));
        }

        if (StringUtils.isNotEmpty(email)) {
            detachedCriteria.add(Restrictions.ilike("email", email + "%"));
        }

        if (StringUtils.isNotEmpty(jobDepartment)) {
            String[] parts = jobDepartment.split(" ");
            for (String part : parts) {
                detachedCriteria.add(Restrictions.ilike("jobDepartment", "%" + part + "%"));
            }

        }

        if (StringUtils.isNotEmpty(jobPosition)) {
            detachedCriteria.add(Restrictions.ilike("jobPosition", jobPosition + "%"));
        }

        if (role != null) {
            detachedCriteria.add(Restrictions.eq("role", role));
        }

        if (!showDeleted)
            detachedCriteria.add(Restrictions.or(Restrictions.isNull("deleted"), Restrictions.eq("deleted", false)));

        return detachedCriteria;
    }

    /**
     * Есть ли у пользователя user право permissionType
     *
     * @param user           пользователь
     * @param permissionType право
     * @return true - есть; false - доступ запрещен
     */
    @Override
    public boolean hasPermission(User user, PermissionType permissionType) {
        if ((user != null) && (permissionType != null)) {
            DetachedCriteria detachedCriteria = DetachedCriteria.forClass(Role.class);
            detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

            detachedCriteria.createCriteria("users", DetachedCriteria.LEFT_JOIN).add(Restrictions.idEq(user.getId()));

            detachedCriteria.createCriteria("permissions", DetachedCriteria.LEFT_JOIN).add(Restrictions.eq("permissionType", permissionType));

            List<Role> roles = getHibernateTemplate().findByCriteria(detachedCriteria, -1, 1);
            return ((roles != null) && !roles.isEmpty());
        } else {
            return false;
        }
    }

    /**
     * Находит права(разрешения) в справочнике прав по типу и по названию
     *
     * @param name           название
     * @param permissionType тип права доступа
     * @return право доступа
     */
    @Override
    public List<Permission> getPermission(String name, PermissionType permissionType) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(Permission.class);
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
        if ((StringUtils.isNotEmpty(name)) || (permissionType != null)) {
            if (StringUtils.isNotEmpty(name)) {
                detachedCriteria.add(Restrictions.eq("name", name));
            }
            if ((permissionType != null)) {
                detachedCriteria.add(Restrictions.eq("permissionType", permissionType));
            }
        } else {
            return null;
        }
        return getHibernateTemplate().findByCriteria(detachedCriteria);
    }


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
    @Override
    public List<User> findUsers(String login, String firstname, String lastname, String middlename, String email, Role role, boolean showDeleted, int offset, int count, String orderBy, boolean orderAsc) {
        DetachedCriteria detachedCriteria = createCriteriaForUsers(login, firstname, lastname, middlename, email, role, showDeleted);
        addOrder(detachedCriteria, orderBy, orderAsc);
        return getHibernateTemplate().findByCriteria(detachedCriteria, offset, count);
    }

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
    @Override
    public long countUsers(String login, String firstname, String lastname, String middlename, String email, Role role, boolean showDeleted) {
        DetachedCriteria detachedCriteria = createCriteriaForUsers(login, firstname, lastname, middlename, email, role, showDeleted);
        return getCountOf(detachedCriteria);
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
            LogicalExpression orExp = Restrictions.or(Restrictions.ilike("lastName", pattern + "%"),
                    Restrictions.ilike("middleName", pattern + "%"));
            orExp = Restrictions.or(orExp, Restrictions.ilike("firstName", pattern + "%"));
            orExp = Restrictions.or(orExp, Restrictions.ilike("email", pattern + "%"));
            searchCriteria.createAlias("contacts", "contacts", CriteriaSpecification.LEFT_JOIN);
            orExp = Restrictions.or(orExp, Restrictions.ilike("contacts.value", pattern + "%"));
            searchCriteria.createAlias("jobPosition", "jobPosition", CriteriaSpecification.LEFT_JOIN);
            orExp = Restrictions.or(orExp, Restrictions.ilike("jobPosition.value", pattern + "%"));
            searchCriteria.createAlias("jobDepartment", "jobDepartment", CriteriaSpecification.LEFT_JOIN);
            orExp = Restrictions.or(orExp, Restrictions.ilike("jobDepartment.value", pattern + "%"));
            searchCriteria.add(orExp);
        }
    }


    @Override
    public List<User> findUsers(final String pattern, final boolean showDeleted, final boolean showFired, final int offset, final int count, final String orderBy, final boolean orderAsc) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(User.class);
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
        addPatternCriteria(detachedCriteria, pattern);
        if (!showDeleted) {
            detachedCriteria.add(Restrictions.eq("deleted", false));
        }
        if (!showFired) {
            detachedCriteria.add(Restrictions.eq("fired", false));
        }
        detachedCriteria.setProjection(Projections.id());
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
        DetachedCriteria resultCriteria = DetachedCriteria.forClass(getPersistentClass()).add(Restrictions.in("id", ids));
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
    public long countEmployes(String pattern, boolean showDeleted) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(User.class);
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
        detachedCriteria.add(Restrictions.eq("fired", false));
        addPatternCriteria(detachedCriteria, pattern);
        if (showDeleted) {
            detachedCriteria.add(Restrictions.eq("deleted", true));
        }

        return getCountOf(detachedCriteria);
    }


    /**
     * {@inheritDoc}
     */
    public List<User> findEmployes(String pattern, boolean showDeleted, int offset, int count, String orderBy, boolean orderAsc) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(User.class);
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
        detachedCriteria.add(Restrictions.or(Restrictions.isNull("fired"), Restrictions.eq("fired", false)));
        addPatternCriteria(detachedCriteria, pattern);
        if (showDeleted) {
            detachedCriteria.add(Restrictions.eq("deleted", true));
        }

        String[] ords = orderBy == null ? null : orderBy.split(",");
        if (ords != null) {
            if (ords.length > 1) {
                addOrder(detachedCriteria, ords, orderAsc);
            } else {
                addOrder(detachedCriteria, orderBy, orderAsc);
            }
        }
        return getHibernateTemplate().findByCriteria(detachedCriteria, offset, count);
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
    public List<User> findFiredUsers(String pattern, boolean showDeleted, int offset, int count, String orderBy, boolean orderAsc) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(User.class);
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
        addPatternCriteria(detachedCriteria, pattern);
        if (!showDeleted) {
            detachedCriteria.add(Restrictions.eq("deleted", false));
        }
        detachedCriteria.add(Restrictions.eq("fired", true));
        detachedCriteria.setProjection(Projections.id());
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
        DetachedCriteria resultCriteria = DetachedCriteria.forClass(getPersistentClass()).add(Restrictions.in("id", ids));
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
        detachedCriteria.add(Restrictions.eq("id", id));
        final List<User> users = getHibernateTemplate().findByCriteria(detachedCriteria);
        if(!users.isEmpty()){
            final User result = users.iterator().next();
            //TODO  http://stackoverflow.com/questions/18753245/one-to-many-relationship-gets-duplicate-objects-whithout-using-distinct-why
            /**
             * Want to know why the duplicates are there?
             * Look at the SQL resultset, Hibernate does not hide these duplicates on the left side of the outer joined result
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