package ru.hitsl.sql.dao.impl.referencebook;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.criterion.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.entity.model.referenceBook.Group;
import ru.hitsl.sql.dao.impl.mapped.ReferenceBookDaoImpl;
import ru.hitsl.sql.dao.interfaces.referencebook.GroupDao;

import static org.hibernate.sql.JoinType.LEFT_OUTER_JOIN;

@Repository("groupDao")
@Transactional(propagation = Propagation.MANDATORY)
public class GroupDaoImpl extends ReferenceBookDaoImpl<Group> implements GroupDao{

    @Override
    public Class<Group> getEntityClass() {
        return Group.class;
    }

    /**
     * Получить критерий для отбора групп и их показа в расширенных списках  (НЕ ОТЛИЧАЕТСЯ от SIMPLE_CRITERIA)
     *
     * @return критерий для групп
     */
    @Override
    public DetachedCriteria getListCriteria() {
        final DetachedCriteria result = getSimpleCriteria();
        result.createAlias("members", "members", LEFT_OUTER_JOIN);
        return result;
    }

    /**
     * Получить критерий для отбора групп с подтягиванием всех возможных полей
     *
     * @return критерий групп
     */
    @Override
    public DetachedCriteria getFullCriteria() {
        final DetachedCriteria result = getListCriteria();
        //EAGER LOADING OF GROUPS, ROLES, defaultNomenclature, and accessLevels
        result.createAlias("author", "author", LEFT_OUTER_JOIN);
        result.createAlias("groupType", "groupType", CriteriaSpecification.INNER_JOIN);
        return result;
    }

    /**
     * Обрабатывает поисковый шаблон через ИЛИ (НУЖНА LIST_CRITERIA)
     *
     * @param criteria критерий отбора
     * @param filter   поисковый шаблон
     */
    @Override
    public void applyFilter(final DetachedCriteria criteria, final String filter) {
        if (StringUtils.isNotEmpty(filter)) {
            final Disjunction disjunction = Restrictions.disjunction();
            disjunction.add(Restrictions.ilike("code", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("value", filter, MatchMode.ANYWHERE));
            criteria.add(disjunction);
        }
    }

    @Override
    public Group findGroupByAlias(final String alias) {
        return getFirstItem(getFullCriteria().add(Restrictions.eq("alias", alias)));
    }
}