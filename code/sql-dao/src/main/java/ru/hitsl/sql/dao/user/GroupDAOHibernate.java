package ru.hitsl.sql.dao.user;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.*;
import ru.entity.model.user.Group;
import ru.hitsl.sql.dao.DictionaryDAOHibernate;

import java.util.ArrayList;
import java.util.List;

public class GroupDAOHibernate extends DictionaryDAOHibernate<Group> {

    @Override
    protected Class<Group> getPersistentClass() {
        return Group.class;
    }

    /**
     * Получить самый простой критерий для отбора групп, без лишних FETCH
     *
     * @return критерий для групп с DISTINCT
     */
    public DetachedCriteria getSimplestCriteria() {
        return DetachedCriteria.forClass(Group.class, "this").setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
    }

    /**
     * Получить критерий для отбора групп и их показа в расширенных списках  (НЕ ОТЛИЧАЕТСЯ от SIMPLE_CRITERIA)
     *
     * @return критерий для групп
     */
    public DetachedCriteria getListCriteria() {
        final DetachedCriteria result = getSimplestCriteria();
        result.createAlias("members", "members", CriteriaSpecification.LEFT_JOIN);
        return result;
    }

    /**
     * Получить критерий для отбора групп с подтягиванием всех возможных полей
     *
     * @return критерий групп
     */
    public DetachedCriteria getEagerCriteria() {
        final DetachedCriteria result = getListCriteria();
        //EAGER LOADING OF GROUPS, ROLES, defaultNomenclature, and accessLevels
        result.createAlias("author", "author", CriteriaSpecification.LEFT_JOIN);
        result.createAlias("groupType", "groupType", CriteriaSpecification.INNER_JOIN);
        return result;
    }


    public Group getItemById(final Integer id) {
        final DetachedCriteria detachedCriteria = getEagerCriteria().add(Restrictions.eq("id", id));
        final List<Group> result = getHibernateTemplate().findByCriteria(detachedCriteria);
        if (!result.isEmpty()) {
            return result.iterator().next();
        }
        return null;
    }

    public Group getItemByIdForListView(final Integer id) {
        final DetachedCriteria detachedCriteria = getListCriteria().add(Restrictions.eq("id", id));
        final List<Group> result = getHibernateTemplate().findByCriteria(detachedCriteria);
        if (!result.isEmpty()) {
            return result.iterator().next();
        }
        return null;
    }


    public List<Group> findItems(
            final String filter, final boolean showDeleted, final int first, final int pageSize, final String orderBy, final boolean orderAsc
    ) {
        final DetachedCriteria detachedCriteria = getListCriteria();
        applyFilterCriteria(detachedCriteria, filter);
        if (!showDeleted) {
            detachedCriteria.add(Restrictions.eq("deleted", false));
        }
        addOrder(detachedCriteria, orderBy, orderAsc);
        return getCorrectLimitingItems(detachedCriteria, orderBy, orderAsc, first, pageSize);
    }

    public int countItems(final String filter, final boolean showDeleted) {
        final DetachedCriteria criteria = getSimplestCriteria();
        if (!showDeleted) {
            criteria.add(Restrictions.eq("deleted", false));
        }
        applyFilterCriteria(criteria, filter);
        return (int) getCountOf(criteria);
    }


    /**
     * Обрабатывает поисковый шаблон через ИЛИ (НУЖНА LIST_CRITERIA)
     *
     * @param criteria критерий отбора
     * @param filter   поисковый шаблон
     */
    public void applyFilterCriteria(final DetachedCriteria criteria, final String filter) {
        if (StringUtils.isNotEmpty(filter)) {
            final Disjunction disjunction = Restrictions.disjunction();
            disjunction.add(Restrictions.ilike("code", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("value", filter, MatchMode.ANYWHERE));
            criteria.add(disjunction);
        }
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
    public List<Group> getCorrectLimitingItems(
            final DetachedCriteria criteria, final String sortField, final boolean sortOrder, final int first, final int pageSize
    ) {
        criteria.setProjection(Projections.distinct(Projections.id()));
        //получаем список ключей от сущностей, которые нам нужны (с корректным [LIMIT offset, count])
        final List ids = getHibernateTemplate().findByCriteria(criteria, first, pageSize);
        if (ids.isEmpty()) {
            return new ArrayList<>(0);
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

    public Group findGroupByAlias(final String alias) {
        final List result = getHibernateTemplate().findByCriteria(getEagerCriteria().add(Restrictions.eq("alias", alias)));
        if (!result.isEmpty()) {
            return (Group) result.get(0);
        }
        return null;
    }
}