package ru.hitsl.sql.dao.interfaces.mapped.criteria;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import java.util.Set;

/**
 * Author: Upatov Egor <br>
 * Date: 22.03.2017, 20:26 <br>
 * Company: Bars Group [ www.bars.open.ru ]
 * Description:
 */
public interface Draftable {
    /**
     * Добавление ограничения на удаленные документы в запрос
     *
     * @param criteria   запрос, куда будет добалено ограничение
     * @param showDrafts false - в запрос будет добавлено ограничение на проверку статуса документа, так чтобы
     *                   его статус был НЕ "Проект документа"
     */
    default void applyDraftRestriction(final DetachedCriteria criteria, final boolean showDrafts) {
        if (!showDrafts) {
            criteria.add(Restrictions.not(Restrictions.in("statusId", getDraftStatuses())));
        }
    }

    /**
     * Получить список проектных статусов
     *
     * @return список идентифкаторов проектных статусов
     */
    Set<Integer> getDraftStatuses();

}
