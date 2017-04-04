package ru.hitsl.sql.dao.interfaces.mapped.criteria;

import org.hibernate.criterion.DetachedCriteria;
import ru.hitsl.sql.dao.util.AuthorizationData;

/**
 * Author: Upatov Egor <br>
 * Date: 22.03.2017, 14:45 <br>
 * Company: Bars Group [ www.bars.open.ru ]
 * Description:
 */
public interface Accessible {
    /**
     * Применить ограничения допуска для документов
     *
     * @param criteria исходный критерий   (минимум LIST_CRITERIA)
     * @param auth     данные авторизации
     */
    void applyAccessCriteria(DetachedCriteria criteria, AuthorizationData auth);

}
