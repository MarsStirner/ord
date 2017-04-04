package ru.hitsl.sql.dao.impl.mapped;


import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import ru.entity.model.mapped.IdentifiedEntity;
import ru.hitsl.sql.dao.interfaces.mapped.AbstractDao;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Transactional("ordTransactionManager")
public abstract class DaoImpl<T extends IdentifiedEntity> implements AbstractDao<T> {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    protected SessionFactory sessionFactory;

    @PostConstruct
    public void init() {
        log.info("Init for work with [{}] and SessionFactory[@{}]", getEntityClass().getSimpleName(), Integer.toHexString(sessionFactory.hashCode()));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Different levels of criteria for querying
    // lang[RU]: Критерии для различных вариантов отображения документов
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public DetachedCriteria getSimpleCriteria() {
        return DetachedCriteria.forClass(getEntityClass()).setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
    }

    @Override
    public DetachedCriteria getListCriteria() {
        return getSimpleCriteria();
    }

    @Override
    public DetachedCriteria getFullCriteria() {
        return getListCriteria();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Методы для получения единичной записи с нужным уровнем вложенности связей
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    @Override
    public T getItemByFullCriteria(Integer id) {
        return getFirstItem(getFullCriteria().add(Restrictions.idEq(id)));
    }

    @Override
    public T getItemByListCriteria(Integer id) {
        return getFirstItem(getListCriteria().add(Restrictions.idEq(id)));
    }

    @Override
    public T getItemBySimpleCriteria(Integer id) {
        return getFirstItem(getSimpleCriteria().add(Restrictions.idEq(id)));
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // CRUD
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public T save(T entity) {
        log.trace("Save {}", entity);
        final Serializable assignedId = sessionFactory.getCurrentSession().save(entity);
        log.debug("Saved with ID[{}] {}", assignedId, entity);
        return entity;
    }

    @Override
    public T update(T entity) {
        log.trace("Update {}.", entity);
        sessionFactory.getCurrentSession().update(entity);
        log.debug("Updated {}.", entity);
        return entity;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Criteria Execution
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public int countItems(DetachedCriteria criteria) {
        return ((Number)criteria.setProjection(Projections.countDistinct("id"))
                .getExecutableCriteria(sessionFactory.getCurrentSession()).uniqueResult()).intValue();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<T> getItems(DetachedCriteria criteria, int offset, int limit) {
        final Criteria executableCriteria = criteria.getExecutableCriteria(sessionFactory.getCurrentSession());
        if (offset > 0) {
            executableCriteria.setFirstResult(offset);
        }
        if (limit > 0) {
            executableCriteria.setMaxResults(limit);
        }
        return executableCriteria.list();
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
    public List<T> getWithCorrectLimitings(
            final DetachedCriteria criteria, final String sortField, final boolean sortOrder, final int first, final int pageSize
    ) {
        criteria.setProjection(Projections.distinct(Projections.id()));
        //получаем список ключей от сущностей, которые нам нужны (с корректным [LIMIT offset, countItems])
        final List ids = getItems(criteria, first, pageSize);
        if (ids.isEmpty()) {
            return new ArrayList<>(0);
        }
        //Ищем только по этим ключам с упорядочиванием
        return getItems(getIDListCriteria(ids, sortField, sortOrder));
    }

    /**
     * Формирование запроса на поиск документов по списку идентификаторов
     *
     * @param ids      список идентифкаторов документов
     * @param orderBy  колонка для сортировки
     * @param orderAsc направление сортировки
     * @return запрос, с ограничениями на идентификаторы документов и сортировки
     */
    protected DetachedCriteria getIDListCriteria(List ids, String orderBy, boolean orderAsc) {
        final DetachedCriteria result = getListCriteria().add(Restrictions.in("id", ids));
        if (StringUtils.isNotEmpty(orderBy)) {
            result.addOrder(orderAsc ? Order.asc(orderBy) : Order.desc(orderBy));
        }
        return result;
    }
}
