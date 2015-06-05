package ru.hitsl.sql.dao;

import java.util.Collection;

public interface BaseDAO<E> {

    /**
     * Сохраняет элемент сущности и возвращает сохраненный
     * элемент.
     *
     * @param entity сущность для сохранения в БД
     * @return сохраненный элемент сущности в БД
     */
    public E save(E entity);

    /**
     * Сохраняет несколько элементов сущности.
     *
     * @param entities сущности для сохранения в БД
     */
    public void save(Collection<? extends E> entities);

    /**
     * Обновляет элемент сущности в БД.
     *
     * @param entity сущность для обновления
     * @return обновленный элемент сущности
     */
    public E update(E entity);

    /**
     * Удаляет элемент сущности из БД.
     *
     * @param entity сущность для удаления
     */
    public void delete(E entity);

    /**
     * Удаляет список элементов из БД.
     *
     * @param entities список элементов для удаления
     */
    public void delete(Collection<? extends E> entities);

}