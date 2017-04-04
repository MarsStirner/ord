package ru.hitsl.sql.dao.interfaces.mapped;

import ru.entity.model.mapped.ReferenceBookEntity;

import java.util.List;

/**
 * Интерфейс для управления справочными данными.
 */
public interface ReferenceBookDao<T extends ReferenceBookEntity> extends CommonDao<T> {

    /**
     * Получить запись справочника по уникальному коду (если не найдено - NULL)
     *
     * @param code Уникальный код записи справочника
     * @return запись справочника (в том числе и удаленную)\ NULL
     */
    T getByCode(String code);

    /**
     * Получить запись справочника по ее значению (если не найдено - NULL)
     *
     * @param value Значение записи справочника
     * @return записи справочника (только не удаленные) \ NULL
     */
    List<T> getByValue(String value);

}