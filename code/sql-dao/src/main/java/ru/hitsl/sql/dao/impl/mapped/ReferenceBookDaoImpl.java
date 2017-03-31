package ru.hitsl.sql.dao.impl.mapped;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import ru.entity.model.mapped.ReferenceBookEntity;
import ru.hitsl.sql.dao.interfaces.mapped.ReferenceBookDao;

import java.util.List;

/**
 * Author: Upatov Egor <br>
 * Date: 21.03.2017, 18:03 <br>
 * Company: Bars Group [ www.bars.open.ru ]
 * Description:
 */
@SuppressWarnings("unchecked")
public abstract class ReferenceBookDaoImpl<T extends ReferenceBookEntity> extends CommonDaoImpl<T> implements ReferenceBookDao<T> {

    /**
     * Получить запись справочника по уникальному коду (если не найдено - NULL)
     *
     * @param code Уникальный код записи справочника
     * @return запись справочника (в том числе и удаленную)\ NULL
     */
    @Override
    public T getByCode(final String code) {
        final DetachedCriteria criteria = getFullCriteria();
        criteria.add(Restrictions.eq("code", code));
        return getFirstItem(criteria);
    }


    /**
     * Получить запись справочника по ее значению (если не найдено - NULL)
     *
     * @param value Значение записи справочника
     * @return записи справочника (только не удаленные) \ NULL
     */
    @Override
    public List<T> getByValue(final String value) {
        final DetachedCriteria criteria = getFullCriteria();
        criteria.add(Restrictions.eq("value", value));
        return getItems(criteria);
    }

    /**
     * Применить к переданному запросу условия простого поиска
     * IMPL = value LIKE '%{filter}%'
     *
     * @param criteria изначальный запрос
     * @param filter   простой поисковый фильтр
     */
    @Override
    public void applyFilter(final DetachedCriteria criteria, final String filter) {
        if (StringUtils.isNotEmpty(filter)) {
            criteria.add(Restrictions.ilike("value", filter, MatchMode.ANYWHERE));
        }
    }

}
