package ru.hitsl.sql.dao;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.*;
import org.hibernate.type.StringType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.entity.model.mapped.DeletableEntity;
import ru.entity.model.user.User;
import ru.external.AuthorizationData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Author: Upatov Egor <br>
 * Date: 30.03.2015, 18:57 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
public abstract class DocumentDAO<T extends DeletableEntity> extends GenericDAOHibernate<T>{

    protected static Logger logger = LoggerFactory.getLogger("DOCUMENT_DAO");


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Критерии для различных вариантов отображения документов
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Получить самый простой критерий для отбора документов, без лишних FETCH
     *
     * @return обычно критерий для документов с DISTINCT
     */
    public abstract DetachedCriteria getSimplestCriteria();

    /**
     * Получить критерий для отбора Документов и их показа в расширенных списках
     * Обычно:
     * Автор - INNER
     * Руководитель - LEFT
     * Вид документа - LEFT
     * ++ В зависимости от вида
     *
     * @return критерий для документов с DISTINCT и нужными alias (with fetch strategy)
     */
    public abstract DetachedCriteria getListCriteria();

    /**
     * Получить критерий для отбора Документов с максимальным количеством FETCH
     *
     * @return критерий для документов с DISTINCT и нужными alias (with fetch strategy)
     */
    public abstract DetachedCriteria getFullCriteria();

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Методы для получения единичной записи с нужным уровнем вложенности связей
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Получить документ с FULL_CRITERIA  по его идентификатору
     *
     * @param id идентификатор документа
     * @return документ, полученный с FULL_CRITERIA
     */
    @SuppressWarnings("unchecked")
    public T getItemById(Integer id) {
        final List resultList = getHibernateTemplate().findByCriteria(getFullCriteria().add(Restrictions.eq("id", id)));
        if (resultList.isEmpty()) {
            return null;
        } else {
            return (T) resultList.get(0);
        }
    }

    /**
     * Получить документ с LIST_CRITERIA  по его идентификатору
     *
     * @param id идентификатор документа
     * @return документ, полученный с LIST_CRITERIA
     */
    @SuppressWarnings("unchecked")
    public T getItemByIdForListView(Integer id) {
        final List resultList = getHibernateTemplate().findByCriteria(getListCriteria().add(Restrictions.eq("id", id)));
        if (resultList.isEmpty()) {
            return null;
        } else {
            return (T) resultList.get(0);
        }
    }

    /**
     * Получить документ с SIMPLE_CRITERIA  по его идентификатору
     *
     * @param id идентификатор документа
     * @return документ, полученный с SIMPLE_CRITERIA
     */
    @SuppressWarnings("unchecked")
    public T getItemByIdForSimpleView(Integer id) {
        final List resultList = getHibernateTemplate().findByCriteria(getSimplestCriteria().add(Restrictions.eq("id", id)));
        if (resultList.isEmpty()) {
            return null;
        } else {
            return (T) resultList.get(0);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Методы для работы со списками
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Получить список документов, доступных пользователю, согласно фильтрам (LIST_CRITERIA)
     * (может использоваться сразу два способа фильтрации одновременно)
     *
     * @param authData  авторизационные данные пользователя
     * @param filter    строковый фильтр, учитывается только если заполнен (смотри getFilteringListCriteria ->
     *                  applyFilterCriteria)
     * @param filters   фильтр-карта с наборами параметров (смотри getFilteringListCriteria -> applyFilterMapCriteria)
     * @param sortField поле, по которому будет происходить сортировка результатов
     * @param sortOrder порядок сортировки (TRUE = ASC \ FALSE = DESC)
     * @param first     начальное смещение страницы (ранжирование по страницам)
     * @param pageSize  размер страницы
     * @return требуемая часть отсортированного списка документов, удовлетворяющих фильтрам.
     */
    public List<T> getDocumentListByFilters(
            final AuthorizationData authData,
            final String filter,
            final Map<String, Object> filters,
            final String sortField,
            final boolean sortOrder,
            final int first,
            final int pageSize,
            final boolean showDeleted,
            final boolean showDrafts
    ) {
        final DetachedCriteria criteria = getFilteringListCriteria(filter, filters);
        applyAccessCriteria(criteria, authData);
        addDraftsAndDeletedRestrictions(criteria, showDeleted, showDrafts);
        if (StringUtils.isNotEmpty(sortField)) {
            criteria.addOrder(sortOrder ? Order.asc(sortField) : Order.desc(sortField));
        }
        return getCorrectLimitingDocuments(criteria, sortField, sortOrder, first, pageSize);
    }

    /**
     * Получить количество документов, удовлетворяющих фильтрам, доступных пользователю
     *
     * @param authData авторизационные данные пользователя
     * @param filter   простой строковый фильтр
     * @param filters  сложный фильтр
     */
    public int countDocumentListByFilters(
            final AuthorizationData authData,
            final String filter,
            final Map<String, Object> filters,
            final boolean showDeleted,
            final boolean showDrafts
    ) {
        final DetachedCriteria criteria = getFilteringListCriteria(filter, filters);
        applyAccessCriteria(criteria, authData);
        addDraftsAndDeletedRestrictions(criteria, showDeleted, showDrafts);
        return (int) getCountOf(criteria);
    }

    /**
     * Получить персональные документы, находящиеся в проектном статусе
     *
     * @param authData  авторизационные данные
     * @param filter    простой строковый фильтр
     * @param sortField поле, по которому будет происходить сортировка результатов
     * @param sortOrder порядок сортировки (TRUE = ASC \ FALSE = DESC)
     * @param first     начальное смещение страницы (ранжирование по страницам)
     * @param pageSize  размер страницы
     * @return требуемая часть отсортированного списка документов, удовлетворяющих фильтрам.
     */
    public List<T> getPersonalDraftDocumentListByFilters(
            final AuthorizationData authData,
            final String filter,
            final String sortField,
            final boolean sortOrder,
            final int first,
            final int pageSize
    ) {
        final DetachedCriteria criteria = getListCriteria();
        applyFilterCriteria(criteria, filter);
        criteria.add(Restrictions.in("author.id", authData.getUserIds()));
        criteria.add(Restrictions.in("statusId", getDraftStatuses()));
        if (StringUtils.isNotEmpty(sortField)) {
            criteria.addOrder(sortOrder ? Order.asc(sortField) : Order.desc(sortField));
        }
        return getCorrectLimitingDocuments(criteria, sortField, sortOrder, first, pageSize);
    }


    /**
     * Получить количество персональных документы, находящихся в проектном статусе
     *
     * @param authData авторизационные данные пользователя
     * @param filter   простой строковый фильтр
     */
    public int countPersonalDraftDocumentListByFilters(final AuthorizationData authData, final String filter) {
        final DetachedCriteria criteria = getListCriteria();
        applyFilterCriteria(criteria, filter);
        criteria.add(Restrictions.in("author.id", authData.getUserIds()));
        criteria.add(Restrictions.in("statusId", getDraftStatuses()));
        return (int) getCountOf(criteria);
    }


    /**
     * Получить список документов с корректными LIMIT
     *
     * @param criteria  изначальный критерий для отбора документов
     * @param sortField поле сортировки
     * @param sortOrder порядок сортировки
     * @param first     начальное смещение
     * @param pageSize  макс размер бвыбираемого списка
     * @return список документов заданного размера
     */
    @SuppressWarnings("unchecked")
    public List<T> getCorrectLimitingDocuments(
            final DetachedCriteria criteria, final String sortField, final boolean sortOrder, final int first, final int pageSize
    ) {
        criteria.setProjection(Projections.distinct(Projections.id()));
        //получаем список ключей от сущностей, которые нам нужны (с корректным [LIMIT offset, count])
        final List ids = getHibernateTemplate().findByCriteria(criteria, first, pageSize);
        if (ids.isEmpty()) {
            return new ArrayList<T>(0);
        }
        //Ищем только по этим ключам с упорядочиванием
        return getHibernateTemplate().findByCriteria(getIDListCriteria(ids, sortField, sortOrder));
    }

    /**
     * Формирование запроса на поиск документов по списку идентификаторов
     *
     * @param ids      список идентифкаторов документов
     * @param orderBy  колонка для сортировки
     * @param orderAsc направление сортировки
     * @return запрос, с ограничениями на идентификаторы документов и сортировки
     */
    private DetachedCriteria getIDListCriteria(List ids, String orderBy, boolean orderAsc) {
        final DetachedCriteria result = getListCriteria().add(Restrictions.in("id", ids));
        if (StringUtils.isNotEmpty(orderBy)) {
            result.addOrder(orderAsc ? Order.asc(orderBy) : Order.desc(orderBy));
        }
        return result;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Работа с поисковыми шаблонами
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Получить критерии для отображения отфильтрованных документов в списке (LIST_CRITERIA)
     *
     * @param filter  простой строковый фильтр
     * @param filters сложный параметризованный фильтр
     * @return критерий с учетом фильтрации
     */
    public DetachedCriteria getFilteringListCriteria(final String filter, final Map<String, Object> filters) {
        final DetachedCriteria result = getListCriteria();
        applyFilterCriteria(result, filter);
        applyFilterMapCriteria(result, filters);
        return result;
    }

    /**
     * Применитиь к текущим критериям огарничения сложного фильтра
     *
     * @param criteria текущий критерий, в который будут добавлены условия  (НЕ менее LIST_CRITERIA)
     * @param filters  сложный фильтр (карта)
     */
    public abstract void applyFilterMapCriteria(DetachedCriteria criteria, Map<String, Object> filters);

    /**
     * Производит поиск заданной строки в (по условию ИЛИ [дизъюнкция]):
     * заданных полях сущности
     *
     * @param criteria критерий отбора в который будет добавлено поисковое условие (НЕ менее LIST_CRITERIA)
     * @param filter   условие поиска
     */
    public abstract void applyFilterCriteria(final DetachedCriteria criteria, final String filter);


    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Общие части работы с критериями
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Добавление ограничения на удаленные документы в запрос
     *
     * @param criteria    запрос, куда будет добалено ограничение
     * @param showDeleted true - в запрос будет добавлено ограничение на проверку флага, так чтобы документ не
     *                    был удален
     */
    public void addDeletedRestriction(final DetachedCriteria criteria, final boolean showDeleted) {
        if (!showDeleted) {
            criteria.add(Restrictions.eq("deleted", false));
        }
    }

    /**
     * Добавление ограничений на статус докуменита и флаг удаленя
     *
     * @param criteria    запрос, в который будут добавлены ограничения
     * @param showDeleted включать в результат удаленные документы
     * @param showDrafts  включать в результат документы, на прошедшие регистрацию
     */
    public void addDraftsAndDeletedRestrictions(final DetachedCriteria criteria, final boolean showDeleted, boolean showDrafts) {
        addDeletedRestriction(criteria, showDeleted);
        addDraftRestriction(criteria, showDrafts);
    }

    /**
     * Применить ограничения допуска для документов
     *
     * @param criteria исходный критерий   (минимум LIST_CRITERIA)
     * @param auth     данные авторизации
     */
    public abstract void applyAccessCriteria(DetachedCriteria criteria, AuthorizationData auth);

    /**
     * Добавление ограничения на удаленные документы в запрос
     *
     * @param criteria   запрос, куда будет добалено ограничение
     * @param showDrafts false - в запрос будет добавлено ограничение на проверку статуса документа, так чтобы
     *                   его статус был НЕ "Проект документа"
     */
    public void addDraftRestriction(final DetachedCriteria criteria, final boolean showDrafts) {
        if (!showDrafts) {
            criteria.add(Restrictions.not(Restrictions.in("statusId", getDraftStatuses())));
        }
    }

    /**
     * Получить список проектных статусов
     *
     * @return список идентифкаторов проектных статусов
     */
    public abstract Set<Integer> getDraftStatuses();


    /**
     * Добавить в условия проверку на равенство пользователя заданному
     *
     * @param conjunction     Общее условие куда будет добавляться проверка
     * @param key             Ключ карты
     * @param restrictionPath поле для проверки
     * @param value           пользователь (User, иначе проверка не будет добавлена)
     */
    public void createUserEqRestriction(final Conjunction conjunction, final String key, final String restrictionPath, final Object value) {
        try {
            final User user = (User) value;
            conjunction.add(Restrictions.eq(restrictionPath, user.getId()));
        } catch (ClassCastException e) {
            logger.error("Exception while forming FilterMapCriteria: [{}]=\'{}\' IS NOT User. Non critical, continue...", key, value);
        }
    }

    /**
     * Добавить в условия проверку на вхождение пользователя в заданный список
     *
     * @param conjunction     Общее условие куда будет добавляться проверка
     * @param key             Ключ карты
     * @param restrictionPath поле для проверки
     * @param value           список пользователей (List<User>, иначе проверка не будет добавлена)
     */
    public void createUserListInRestriction(final Conjunction conjunction, final String key, final String restrictionPath, final Object value) {
        try {
            final List<User> userList = (List<User>) value;
            if (!userList.isEmpty()) {
                List<Integer> userListId = new ArrayList<Integer>(userList.size());
                for (User user : userList) {
                    userListId.add(user.getId());
                }
                conjunction.add(Restrictions.in(restrictionPath, userListId));
            }
        } catch (Exception e) {
            logger.error("Exception while forming FilterMapCriteria: [{}]=\'{}\' IS NOT List<User>. Non critical, continue...", key, value, e);
        }
    }

    /**
     * Создать часть критерия, которая будет проверять заданное поле(типа Дата-Время) на соотвтевие поисковому шаблону
     * @param fieldName  имя поля с типом  (Дата-Время)
     * @param filter  поисковый шаблон
     * @return Часть критерия, проверяющая сответвтвие поля поисковому шаблону
     */
    public Criterion createDateLikeTextRestriction(final String fieldName, final String filter) {
        return Restrictions.sqlRestriction(
                "DATE_FORMAT(".concat(fieldName).concat(", '%d.%m.%Y') like lower(?)"), filter + "%", new StringType()
        );
    }



}
