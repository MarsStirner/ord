package ru.hitsl.sql.dao.interfaces.mapped.criteria;

import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.StringType;
import ru.entity.model.user.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Author: Upatov Egor <br>
 * Date: 22.03.2017, 14:38 <br>
 * ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 * // Работа с поисковыми шаблонами
 * ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 */
public interface Searchable {


    /**
     * Производит поиск заданной строки в (по условию ИЛИ [дизъюнкция]) заданных полях сущности
     *
     * @param criteria критерий отбора в который будет добавлено поисковое условие (НЕ менее LIST_CRITERIA)
     * @param filter   условие поиска
     */
    void applyFilter(DetachedCriteria criteria, String filter);

    /**
     * Применитиь к текущим критериям огарничения сложного фильтра
     *
     * @param criteria текущий критерий, в который будут добавлены условия  (НЕ менее LIST_CRITERIA)
     * @param filter   сложный фильтр (карта)
     */
    void applyFilter(DetachedCriteria criteria, Map<String, Object> filter);

    default void applyFilter(DetachedCriteria criteria, String simpleFilter, Map<String, Object> filter) {
        applyFilter(criteria, simpleFilter);
        applyFilter(criteria, filter);
    }

    /**
     * Создать часть критерия, которая будет проверять заданное поле(типа Дата-Время) на соотвтевие поисковому шаблону
     *
     * @param fieldName имя поля с типом  (Дата-Время)
     * @param filter    поисковый шаблон
     * @return Часть критерия, проверяющая сответвтвие поля поисковому шаблону
     */
    default Criterion createDateLikeTextRestriction(final String fieldName, final String filter) {
        return Restrictions.sqlRestriction("DATE_FORMAT(" + fieldName + ", '%d.%m.%Y') like lower(?)", filter + "%", new StringType());
    }
}
