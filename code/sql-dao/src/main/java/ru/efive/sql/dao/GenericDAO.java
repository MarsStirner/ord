package ru.efive.sql.dao;

import java.io.Serializable;
import java.util.List;

/**
 * Интерфейс для управления сущностями. Содержит основные методы для
 * сохранения, обновления и удаления сущностей в БД.
 */
public interface GenericDAO<E> extends BaseDAO<E> {

    /**
     * Возвращает элемент по идентификатору.
     *
     * @param id идентификатор сущности (обычно числовое значение)
     * @return элемент сохраненный в БД.
     */
    public E get(Serializable id);

    /**
     * Удаляет элемент сущности из БД.
     *
     * @param id идентификатор сущности для удаления.
     * @return true если удаление прошло успешно, иначе false
     */
    public boolean delete(Serializable id);

    /**
     * Поиск документов
     *
     * @param showDeleted true - show deleted, false - hide deleted
     * @param offset      смещение
     * @param count       кол-во результатов
     * @param orderBy     поле для сортировки
     * @param orderAsc    направление сортировки
     * @return список документов
     */
    public List<E> findDocuments(boolean showDeleted, int offset, int count, String orderBy, boolean orderAsc);

    /**
     * Кол-во документов
     *
     * @param showDeleted true - show deleted, false - hide deleted
     * @return кол-во результатов
     */
    public long countDocument(boolean showDeleted);
}