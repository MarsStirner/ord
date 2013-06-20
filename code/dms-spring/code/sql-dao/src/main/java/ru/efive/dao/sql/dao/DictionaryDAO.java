package ru.efive.sql.dao;

import java.util.List;

/**
 * Интерфейс для управления справочными данными.
 */
public interface DictionaryDAO<E> extends GenericDAO<E> {
	
	/**
     * Поиск справочных данных по категории
     *
     * @param category        категория справочника
     * @return список документов
     */
	public List<E> findByValue(String value);
	
	/**
     * Поиск справочных данных по категории
     *
     * @return список документов
     */
	public List<E> findDocuments();
	
	/**
     * Кол-во справочных данных
     *
     * @return кол-во результатов
     */
	public long countDocuments();
	
}