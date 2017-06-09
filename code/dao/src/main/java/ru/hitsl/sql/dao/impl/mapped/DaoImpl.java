package ru.hitsl.sql.dao.impl.mapped;


import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.*;
import org.hibernate.engine.spi.LoadQueryInfluencers;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.internal.CriteriaImpl;
import org.hibernate.internal.SessionImpl;
import org.hibernate.loader.OuterJoinLoader;
import org.hibernate.loader.criteria.CriteriaLoader;
import org.hibernate.loader.criteria.CriteriaQueryTranslator;
import org.hibernate.persister.entity.OuterJoinLoadable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.entity.model.mapped.IdentifiedEntity;
import ru.hitsl.sql.dao.interfaces.mapped.AbstractDao;
import ru.util.ApplicationHelper;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public abstract class DaoImpl<T extends IdentifiedEntity> implements AbstractDao<T> {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    @PersistenceContext(unitName = ApplicationHelper.ORD_PERSISTENCE_UNIT_NAME)
    protected EntityManager em;


    @PostConstruct
    public void init() {
        log.info("Init[{}] for work with [{}] and EntityManager[{}]",
                Integer.toHexString(hashCode()),
                getEntityClass().getSimpleName(),
                Integer.toHexString(em.hashCode())
        );
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
    @Transactional(transactionManager = "ordTransactionManager", propagation = Propagation.MANDATORY)
    public T save(T entity) {
        log.trace("Save {}", entity);
        em.persist(entity);
        log.debug("Saved with ID[{}]", entity.getId());
        return entity;
    }

    @Override
    @Transactional(transactionManager = "ordTransactionManager", propagation = Propagation.MANDATORY)
    public T update(T entity) {
        log.trace("Update {}", entity);
        em.merge(entity);
        log.debug("Updated {}", entity);
        return entity;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Criteria Execution
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public int countItems(DetachedCriteria criteria) {
        return ((Number) criteria.setProjection(Projections.countDistinct("id"))
                .getExecutableCriteria(em.unwrap(Session.class)).uniqueResult()).intValue();
    }

    @Override
    public List<T> getItems(DetachedCriteria criteria, int offset, int limit) {
        final Criteria executableCriteria = criteria.getExecutableCriteria(em.unwrap(Session.class));
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


    @Deprecated
    public String TEST_ONLY_RAW_SQL(DetachedCriteria criteria) throws NoSuchFieldException, IllegalAccessException {
        String sql;
        Object[] parameters = null;

        CriteriaImpl c = (CriteriaImpl) criteria.getExecutableCriteria(em.unwrap(Session.class));
        SessionImpl s = (SessionImpl)c.getSession();
        SessionFactoryImplementor factory = (SessionFactoryImplementor)s.getSessionFactory();
        String[] implementors = factory.getImplementors( c.getEntityOrClassName() );
        CriteriaLoader loader = new CriteriaLoader((OuterJoinLoadable)factory.getEntityPersister(implementors[0]), factory, c, implementors[0], LoadQueryInfluencers.NONE);
        Field f = OuterJoinLoader.class.getDeclaredField("sql");
        f.setAccessible(true);
        sql = (String)f.get(loader);
        Field fp = CriteriaLoader.class.getDeclaredField("translator");
        fp.setAccessible(true);
        CriteriaQueryTranslator translator = (CriteriaQueryTranslator) fp.get(loader);
        parameters = translator.getQueryParameters().getPositionalParameterValues();

        if (sql !=null){
            int fromPosition = sql.indexOf(" from ");
            sql = "SELECT * "+ sql.substring(fromPosition);

            if (parameters!=null && parameters.length>0){
                for (Object val : parameters) {
                    String value;
                    if(val instanceof Boolean){
                        value = ((Boolean)val)?"1":"0";
                    }else if (val instanceof String){
                        value = "'"+val+"'";
                    } else if(val instanceof IdentifiedEntity){
                        value = String.valueOf(((IdentifiedEntity) val).getId());
                    } else {
                        value = "'"+val+"'";
                    }
                    sql = sql.replaceFirst("\\?", value);
                }
            }
        }
       return sql.replaceAll("left outer join", "\nleft outer join").replace(" and ", "\nand ").replace(" on ", "\non ");
    }
}
