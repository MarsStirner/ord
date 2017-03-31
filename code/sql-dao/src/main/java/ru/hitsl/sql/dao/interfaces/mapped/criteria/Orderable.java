package ru.hitsl.sql.dao.interfaces.mapped.criteria;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;

/**
 * Author: Upatov Egor <br>
 * Date: 22.03.2017, 19:18 <br>
 * Company: Bars Group [ www.bars.open.ru ]
 * Description:
 */
public interface Orderable {
    default void applyOrder(DetachedCriteria criteria, Order order) {
        if (order != null) {
            criteria.addOrder(order);
        }
    }

    default void applyOrder(DetachedCriteria criteria, String orderBy, boolean isAscending) {
        if (StringUtils.isNotEmpty(orderBy)) {
            applyOrder(criteria, isAscending ? Order.asc(orderBy) : Order.desc(orderBy));
        }
    }
}
