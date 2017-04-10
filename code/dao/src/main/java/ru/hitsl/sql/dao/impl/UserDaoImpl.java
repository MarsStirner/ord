package ru.hitsl.sql.dao.impl;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.criterion.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.entity.model.referenceBook.Group;
import ru.entity.model.user.User;
import ru.hitsl.sql.dao.impl.mapped.CommonDaoImpl;
import ru.hitsl.sql.dao.interfaces.UserDao;

import java.util.List;

import static org.hibernate.sql.JoinType.INNER_JOIN;
import static org.hibernate.sql.JoinType.LEFT_OUTER_JOIN;

/**
 * Author: Upatov Egor <br>
 * Date: 27.03.2017, 16:49 <br>
 * Company: Bars Group [ www.bars.open.ru ]
 * Description:
 */
@Repository("userDao")
@Transactional(propagation = Propagation.MANDATORY)
public class UserDaoImpl extends CommonDaoImpl<User> implements UserDao {
    @Override
    public Class<User> getEntityClass() {
        return User.class;
    }

    @Override
    public void applyFilter(DetachedCriteria criteria, String filter) {
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

    /**
     * Обрабатывает поисковый шаблон через ИЛИ (НУЖНА LIST_CRITERIA)   для диалогов
     *
     * @param criteria критерий отбора
     * @param filter   поисковый шаблон
     */
    @Override
    public void applyFilterForDialogs(final DetachedCriteria criteria, final String filter) {
        if (StringUtils.isNotEmpty(filter)) {
            final Disjunction disjunction = Restrictions.disjunction();
            disjunction.add(Restrictions.ilike("lastName", filter, MatchMode.ANYWHERE));
            //ORD-115 TODO поиск с весовыми коэффициентами
            // disjunction.add(Restrictions.ilike("middleName", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("firstName", filter, MatchMode.ANYWHERE));
            criteria.add(disjunction);
        }
    }


    /**
     * Получить критерий для отбора пользоваетелей и их показа в расширенных списках
     *
     * @return критерий для пользователей с DISTINCT, должностью, подразделением, контактами
     */
    @Override
    public DetachedCriteria getListCriteria() {
        final DetachedCriteria result = getSimpleCriteria();
        result.createAlias("jobPosition", "jobPosition", LEFT_OUTER_JOIN);
        result.createAlias("jobDepartment", "jobDepartment", LEFT_OUTER_JOIN);
        result.createAlias("contacts", "contacts", LEFT_OUTER_JOIN);
        return result;
    }

    /**
     * Получить критерий для отбора пользоваетелей с подтягиванием всех возможных полей
     *
     * @return критерий для пользователей с DISTINCT, должностью, подразделением, контактами, группами, ролями, номенклатурой по-умолчанию
     */
    @Override
    public DetachedCriteria getFullCriteria() {
        final DetachedCriteria result = getListCriteria();
        //EAGER LOADING OF GROUPS, ROLES, defaultNomenclature, and accessLevels
        result.createAlias("groups", "groups", LEFT_OUTER_JOIN);
        result.createAlias("roles", "roles", LEFT_OUTER_JOIN);
        result.createAlias("defaultNomenclature", "defaultNomenclature", LEFT_OUTER_JOIN);
        result.createAlias("maxUserAccessLevel", "maxUserAccessLevel", INNER_JOIN);
        result.createAlias("currentUserAccessLevel", "currentUserAccessLevel", LEFT_OUTER_JOIN);
        return result;
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
            final DetachedCriteria criteria = getFullCriteria()
                    .add(Restrictions.eq("login", login))
                    .add(Restrictions.eq("password", password));
            filterDeleted(criteria);
            return getFirstItem(criteria);
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
            final DetachedCriteria criteria = getFullCriteria();
            criteria.add(Restrictions.eq("login", login));
            filterDeleted(criteria);
            return getFirstItem(criteria);
        } else {
            return null;
        }
    }

    @Override
    public List<User> getItems(
            final String filter,
            final boolean showDeleted,
            final boolean showFired,
            final int first,
            final int pageSize,
            final String orderBy,
            final boolean orderAsc
    ) {
        final DetachedCriteria criteria = getListCriteria();
        applyFilter(criteria, filter);
        applyDeletedRestriction(criteria, showDeleted);
        criteria.add(Restrictions.eq("fired", showFired));
        applyOrder(criteria, orderBy, orderAsc);
        return getWithCorrectLimitings(criteria, orderBy, orderAsc, first, pageSize);
    }


    @Override
    public int countItems(final String filter, final boolean showDeleted, final boolean showFired) {
        final DetachedCriteria criteria = getListCriteria();
        applyFilter(criteria, filter);
        applyDeletedRestriction(criteria, showDeleted);
        criteria.add(Restrictions.eq("fired", showFired));
        return countItems(criteria);
    }

    @Override
    public List<User> getItemsForDialog(
            final String filter,
            final boolean showDeleted,
            final boolean showFired,
            final int first,
            final int pageSize,
            final String orderBy,
            final boolean orderAsc
    ) {
        final DetachedCriteria criteria = getListCriteria();
        applyFilterForDialogs(criteria, filter);
        applyDeletedRestriction(criteria, showDeleted);
        criteria.add(Restrictions.eq("fired", showFired));
        applyOrder(criteria, orderBy, orderAsc);
        return getWithCorrectLimitings(criteria, orderBy, orderAsc, first, pageSize);
    }


    @Override
    public int countItemsForDialog(final String filter, final boolean showDeleted, final boolean showFired) {
        final DetachedCriteria criteria = getListCriteria();
        applyFilterForDialogs(criteria, filter);
        applyDeletedRestriction(criteria, showDeleted);
        criteria.add(Restrictions.eq("fired", showFired));
        return countItems(criteria);
    }

    /**
     * Получает список пользователей (SIMPLE_CRITERIA) по поисковому шаблону, с учетом уволенных\удаленных и принадлежащих заданной группе
     *
     * @param filter      поисковый шаблон
     * @param showDeleted включать ли в список удаленных
     * @param showFired   включать ли в список уволенных
     * @param group       пользователи должны принадлежать заданной группе
     * @param first       начиная с какого результата вернуть
     * @param pageSize    сколько записей вернуть
     * @param orderBy     соритировка по
     * @param orderAsc    порядок сортировки
     * @return список пользователей удовлетворяющий условиям
     */
    @Override
    public List<User> getItemsForDialogByGroup(
            final String filter,
            final boolean showDeleted,
            final boolean showFired,
            final Group group,
            final int first,
            final int pageSize,
            final String orderBy,
            final boolean orderAsc
    ) {
        final DetachedCriteria criteria = getListCriteria();
        applyFilterForDialogs(criteria, filter);
        applyDeletedRestriction(criteria, showDeleted);
        criteria.add(Restrictions.eq("fired", showFired));
        criteria.createAlias("groups", "groups", INNER_JOIN);
        criteria.add(Restrictions.eq("groups.id", group.getId()));
        applyOrder(criteria, orderBy, orderAsc);
        return getWithCorrectLimitings(criteria, orderBy, orderAsc, first, pageSize);
    }

    @Override
    public int countItemsForDialogByGroup(final String filter, final boolean showDeleted, final boolean showFired, final Group group) {
        final DetachedCriteria criteria = getListCriteria();
        applyFilterForDialogs(criteria, filter);
        applyDeletedRestriction(criteria, showDeleted);
        criteria.add(Restrictions.eq("fired", showFired));
        criteria.createAlias("groups", "groups", INNER_JOIN);
        criteria.add(Restrictions.eq("groups.id", group.getId()));
        return countItems(criteria);
    }

}
