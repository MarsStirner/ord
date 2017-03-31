package ru.hitsl.sql.dao.interfaces.mapped;

import org.hibernate.criterion.DetachedCriteria;
import org.slf4j.LoggerFactory;
import ru.entity.model.mapped.IdentifiedEntity;

import java.util.List;


public interface AbstractDao<T extends IdentifiedEntity> {

    /**
     * Actual entity class (cause generic)
     * lang[RU]: Фактический класс сущности
     *
     * @return Actual entity class
     */
    Class<T> getEntityClass();

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Different levels of criteria for querying
    // lang[RU]: Критерии для различных вариантов отображения документов
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Получить самый простой критерий для отбора сущностей, без лишних FETCH
     *
     * @return критерий для документов с DISTINCT
     */
    DetachedCriteria getSimpleCriteria();

    /**
     * Получить критерий для отбора сущностей и их показа в списках
     *
     * @return критерий для документов с DISTINCT и нужными alias (with fetch strategy)
     */
    DetachedCriteria getListCriteria();

    /**
     * Получить критерий для отбора Документов с максимальным количеством FETCH
     *
     * @return критерий для документов с DISTINCT и нужными alias (with fetch strategy)
     */
    DetachedCriteria getFullCriteria();


    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Методы для получения единичной записи с нужным уровнем вложенности связей
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Получить документ с FULL_CRITERIA  по его идентификатору
     *
     * @param id идентификатор документа
     * @return документ, полученный с FULL_CRITERIA
     */
    T getItemByFullCriteria(Integer id);

    /**
     * Получить документ с LIST_CRITERIA  по его идентификатору
     *
     * @param id идентификатор документа
     * @return документ, полученный с LIST_CRITERIA
     */
    T getItemByListCriteria(Integer id);

    /**
     * Получить документ с SIMPLE_CRITERIA  по его идентификатору
     *
     * @param id идентификатор документа
     * @return документ, полученный с SIMPLE_CRITERIA
     */
    T getItemBySimpleCriteria(Integer id);

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // CRUD
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Получить сущность по ключу (default - FULL_CRITERIA)
     *
     * @param id ключ
     * @return сущность
     */
    default T get(Integer id) {
        return getItemByFullCriteria(id);
    }

    /**
     * Сохранить в БД новую сущность
     *
     * @param entity сущность для сохранения
     * @return сохраненная сущность
     */
    T save(T entity);


    /**
     * Обновить сущность в БД
     *
     * @param entity сущность с новым состоянием
     * @return синхронизированное с БД состояние
     */
    T update(T entity);


    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Criteria Execution
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Посчитать количество строк удовлетворяющих критериям выборки
     *
     * @param criteria критерии выборки
     * @return количество строк подходящих под критерии
     */
    int countItems(DetachedCriteria criteria);

    /**
     * Получить порцию списка сущностей  подходящих под критерии выборки
     *
     * @param criteria критерии выборки
     * @param offset   начальная позиция выборки
     * @param limit    максимальный размер выборки
     * @return список сущностей
     */
    List<T> getItems(DetachedCriteria criteria, int offset, int limit);

    /**
     * Получить весь списка сущностей  подходящих под критерии выборки
     *
     * @param criteria критерии выборки
     * @return список сущностей
     */
    default List<T> getItems(DetachedCriteria criteria) {
        return getItems(criteria, -1, -1);
    }


    default List<T> getItems(){
        return getItems(getListCriteria());
    }

    /**
     * Получить единственную сущность подходящюю под критерии поиска или NULL
     *
     * @return первая сущность из выборки или NULL если выборка пуста
     */
    default T getFirstItem(DetachedCriteria criteria) {
        final List<T> items = getItems(criteria, -1, -1);
        switch (items.size()) {
            case 0:
                return null;
            case 1:
                return items.get(0);
            default:
                LoggerFactory.getLogger(this.getClass()).warn("Criteria return more then one item, first will be used. Total={}, Criteria={}", items.size(), criteria);
                return items.get(0);
        }
    }
}
