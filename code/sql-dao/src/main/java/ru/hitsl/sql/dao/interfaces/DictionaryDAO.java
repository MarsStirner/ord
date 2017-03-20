package ru.hitsl.sql.dao.interfaces;

import ru.entity.model.mapped.DictionaryEntity;

import java.util.List;

/**
 * Интерфейс для управления справочными данными.
 */
public interface DictionaryDAO<E extends DictionaryEntity>  {

    /**
     * Получить запись справочника по уникальному коду (если не найдено - NULL)
     *
     * @param code Уникальный код записи справочника
     * @return запись справочника (в том числе и удаленную)\ NULL
     */
    E getByCode(String code);

    /**
     * Получить запись справочника по ее значению (если не найдено - NULL)
     *
     * @param value Значение записи справочника
     * @return записи справочника (только не удаленные) \ NULL
     */
    List<E> getByValue(String value);

    /**
     * Получить все неудаленные записи справочника
     *
     * @return список неудаленных записей справочника
     */
    List<E> getItems();

    /**
     * Получить количество неудаленных записей справочника
     *
     * @return количество неудаленных записей справочника
     */
    int countItems();



    /**
     * Получить заданную страницу с неудаленными записи справочника с учетом фильтра
     *
     * @param filter простой строковый фильтр
     * @param first  начальная запись страницы
     * @param count  количество элементов на странице
     * @param orderBy поле сортировки
     * @param orderAsc направление сортировки
     * @return страница со списком неудаленных записей справочника
     */
    List<E> getItems(final String filter, final int first, final int count, final String orderBy, final boolean orderAsc);

    /**
     * Получить количество неудаленными записей справочника с учетом фильтра
     *
     * @param filter простой строковый фильтр
     * @return количество неудаленными записей справочника с учетом фильтра
     */
    int countItems(String filter);
}