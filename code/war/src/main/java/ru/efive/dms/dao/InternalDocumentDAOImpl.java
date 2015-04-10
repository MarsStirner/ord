package ru.efive.dms.dao;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.*;
import org.hibernate.type.StringType;
import org.joda.time.LocalDate;
import org.slf4j.LoggerFactory;
import ru.efive.dms.util.security.AuthorizationData;
import ru.entity.model.document.DocumentForm;
import ru.entity.model.document.InternalDocument;
import ru.entity.model.enums.DocumentStatus;
import ru.entity.model.enums.DocumentType;

import java.util.*;

import static ru.efive.dms.util.DocumentSearchMapKeys.*;
import static ru.util.ApplicationHelper.getNextDayDate;


public class InternalDocumentDAOImpl extends DocumentDAO<InternalDocument> {
    static {
        logger = LoggerFactory.getLogger("INTERNAL_DAO");
    }

    @Override
    protected Class<InternalDocument> getPersistentClass() {
        return InternalDocument.class;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // @Deprecated ПОЛНАЯ АХИНЕЯ
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Deprecated
    @SuppressWarnings("unchecked")
    public List<InternalDocument> findDocumentsByCriteria(Map<String, Object> in_map, boolean showDeleted, boolean showDrafts) {
        DetachedCriteria in_searchCriteria = getListCriteria();
        addDraftsAndDeletedRestrictions(in_searchCriteria, showDeleted, showDrafts);
        final LocalDate currentDate = new LocalDate();
        in_searchCriteria.add(Restrictions.sqlRestriction("DATE_FORMAT(this_.registrationDate, '%Y') like lower(?)",
                                                          currentDate.getYear() + "%", new StringType()));
        applyFilterMapCriteria(in_searchCriteria, in_map);
        return getHibernateTemplate().findByCriteria(in_searchCriteria);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Критерии для различных вариантов отображения документов
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Получить самый простой критерий для отбора документов, без лишних FETCH
     *
     * @return обычно критерий для документов с DISTINCT
     */
    @Override
    public DetachedCriteria getSimplestCriteria() {
        return DetachedCriteria.forClass(InternalDocument.class, "this").setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
    }

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
    @Override
    public DetachedCriteria getListCriteria() {
        final DetachedCriteria result = getSimplestCriteria();
        result.createAlias("author", "author", CriteriaSpecification.INNER_JOIN);
        result.createAlias("controller", "controller", CriteriaSpecification.LEFT_JOIN);
        result.createAlias("form", "form", CriteriaSpecification.LEFT_JOIN);
        result.createAlias("responsible", "responsible", CriteriaSpecification.LEFT_JOIN);
        return result;
    }

    /**
     * Получить критерий для отбора Документов с максимальным количеством FETCH
     *
     * @return критерий для документов с DISTINCT и нужными alias (with fetch strategy)
     */
    @Override
    public DetachedCriteria getFullCriteria() {
        final DetachedCriteria result = getListCriteria();
        result.createAlias("recipientUsers", "recipientUsers", CriteriaSpecification.LEFT_JOIN);
        result.createAlias("personEditors", "personEditors", CriteriaSpecification.LEFT_JOIN);
        result.createAlias("personReaders", "personReaders", CriteriaSpecification.LEFT_JOIN);
        result.createAlias("recipientGroups", "recipientGroups", CriteriaSpecification.LEFT_JOIN);
        result.createAlias("roleReaders", "roleReaders", CriteriaSpecification.LEFT_JOIN);
        result.createAlias("roleEditors", "roleEditors", CriteriaSpecification.LEFT_JOIN);
        result.createAlias("userAccessLevel", "userAccessLevel", CriteriaSpecification.INNER_JOIN);
        result.createAlias("history", "history", CriteriaSpecification.LEFT_JOIN);
        return result;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Работа с критериями (общая)
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Применитиь к текущим критериям огарничения сложного фильтра
     *
     * @param criteria текущий критерий, в который будут добавлены условия  (НЕ менее LIST_CRITERIA)
     * @param filters  сложный фильтр (карта)
     */
    @Override
    public void applyFilterMapCriteria(final DetachedCriteria criteria, final Map<String, Object> filters) {
        if (filters == null || filters.isEmpty()) {
            logger.debug("FilterMapCriteria: null or empty. Skip.");
            return;
        }
        final Conjunction conjunction = Restrictions.conjunction();
        for (Map.Entry<String, Object> entry : filters.entrySet()) {
            final String key = entry.getKey();
            final Object value = entry.getValue();

            if (StringUtils.isEmpty(key)) {
                // Пропустить запись с пустым ключом
                logger.warn("FilterMapCriteria: empty key for \'{}\'", value);
            } else if (REGISTRATION_NUMBER_KEY.equals(key)) {
                conjunction.add(Restrictions.ilike("registrationNumber", (String) value, MatchMode.ANYWHERE));
            } else if (START_REGISTRATION_DATE_KEY.equals(key)) {
                conjunction.add(Restrictions.ge("registrationDate", value));
            } else if (END_REGISTRATION_DATE_KEY.equals(key)) {
                conjunction.add(Restrictions.le("registrationDate", getNextDayDate((Date) value)));
            } else if (START_CREATION_DATE_KEY.equals(key)) {
                conjunction.add(Restrictions.ge("creationDate", value));
            } else if (END_CREATION_DATE_KEY.equals(key)) {
                conjunction.add(Restrictions.le("creationDate", getNextDayDate((Date) value)));
            } else if (START_SIGNATURE_DATE_KEY.equals(key)) {
                conjunction.add(Restrictions.ge("signatureDate", value));
            } else if (END_SIGNATURE_DATE_KEY.equals(key)) {
                conjunction.add(Restrictions.le("signatureDate", getNextDayDate((Date) value)));
            } else if (RESPONSIBLE_KEY.equals(key)) {
                createUserEqRestriction(conjunction, key, "responsible.id", value);
            }  else if (SHORT_DESCRIPTION_KEY.equals(key) && StringUtils.isNotEmpty((String) value)) {
                conjunction.add(Restrictions.ilike("shortDescription", (String) value, MatchMode.ANYWHERE));
            } else if (STATUS_KEY.equals(key) && StringUtils.isNotEmpty((String) value)) {
                conjunction.add(Restrictions.eq("statusId", Integer.valueOf((String)value)));
            } else if (CONTROLLER_KEY.equals(key)) {
                createUserEqRestriction(conjunction, key, "controller.id", value);
            } else if (RECIPIENTS_KEY.equals(key)) {
                createUserListInRestriction(conjunction, key, "recipientUsers.id", value);
            } else if (AUTHOR_KEY.equals(key)) {
                createUserEqRestriction(conjunction, key, "author.id", value);
            } else if (AUTHORS_KEY.equals(key)) {
                createUserListInRestriction(conjunction, key, "author.id", value);
            } else if (EXECUTORS_KEY.equals(key)) {
                createUserListInRestriction(conjunction, key, "executors.id", value);
            }else if (START_EXECUTION_DATE_KEY.equals(key)) {
                conjunction.add(Restrictions.ge("executionDate", value));
            } else if (END_EXECUTION_DATE_KEY.equals(key)) {
                conjunction.add(Restrictions.le("executionDate", getNextDayDate((Date) value)));
            } else if (FORM_KEY.equals(key)) {
                try {
                    final DocumentForm form = (DocumentForm) value;
                    conjunction.add(Restrictions.eq("form.id", form.getId()));
                } catch (ClassCastException e) {
                    logger.error("Exception while forming FilterMapCriteria: [{}]=\'{}\' IS NOT DocumentForm. Non critical, continue...", key, value);
                }
            } else if (FORM_VALUE_KEY.equals(key)) {
                conjunction.add(Restrictions.eq("form.value", value));
            } else if (FORM_CATEGORY_KEY.equals(key)) {
                conjunction.add(Restrictions.eq("form.category", value));
            } else if("closePeriodRegistrationFlag".equals(key)){
                conjunction.add(Restrictions.eq("closePeriodRegistrationFlag", Boolean.valueOf(value.toString())));
            } else {
                logger.warn("FilterMapCriteria: Unknown key \'{}\' (value =\'{}\')", key, value);
            }
        }
        criteria.add(conjunction);
    }

    /**
     * Производит поиск заданной строки в (по условию ИЛИ [дизъюнкция]):
     * заданных полях сущности
     *
     * @param criteria критерий отбора в который будет добавлено поисковое условие (НЕ менее LIST_CRITERIA)
     * @param filter   условие поиска
     */
    @Override
    public void applyFilterCriteria(final DetachedCriteria criteria, final String filter) {
        if (StringUtils.isEmpty(filter)) {
            logger.debug("FilterCriteria: empty or null filter. Skip.");
            return;
        }
        final Disjunction disjunction = Restrictions.disjunction();
        disjunction.add(Restrictions.sqlRestriction("DATE_FORMAT(creationDate, '%d.%m.%Y') like lower(?)", filter + "%", new StringType()));
        disjunction.add(Restrictions.sqlRestriction("DATE_FORMAT(executionDate, '%d.%m.%Y') like lower(?)", filter + "%", new StringType()));
        disjunction.add(Restrictions.ilike("shortDescription", filter, MatchMode.ANYWHERE));

        disjunction.add(Restrictions.ilike("author.lastName", filter, MatchMode.ANYWHERE));
        disjunction.add(Restrictions.ilike("author.middleName", filter, MatchMode.ANYWHERE));
        disjunction.add(Restrictions.ilike("author.firstName", filter, MatchMode.ANYWHERE));

        disjunction.add(Restrictions.ilike("responsible.lastName", filter, MatchMode.ANYWHERE));
        disjunction.add(Restrictions.ilike("responsible.middleName", filter, MatchMode.ANYWHERE));
        disjunction.add(Restrictions.ilike("responsible.firstName", filter, MatchMode.ANYWHERE));

        disjunction.add(Restrictions.ilike("controller.lastName", filter, MatchMode.ANYWHERE));
        disjunction.add(Restrictions.ilike("controller.middleName", filter, MatchMode.ANYWHERE));
        disjunction.add(Restrictions.ilike("controller.firstName", filter, MatchMode.ANYWHERE));

        disjunction.add(Restrictions.ilike("form.value", filter, MatchMode.ANYWHERE));
        disjunction.add(Restrictions.ilike("registrationNumber", filter, MatchMode.ANYWHERE));

        //TODO справочник в БД
        final List<DocumentStatus> statuses = DocumentType.getInternalDocumentStatuses();
        final List<Integer> statusIdList = new ArrayList<Integer>(statuses.size());
        for (DocumentStatus current : statuses) {
            if (current.getName().contains(filter)) {
                statusIdList.add(current.getId());
            }
        }
        if (!statusIdList.isEmpty()) {
            disjunction.add(Restrictions.in("statusId", statusIdList));
        }

        criteria.add(disjunction);
    }

    /**
     * Применить ограничения допуска для документов
     *
     * @param criteria исходный критерий   (минимум LIST_CRITERIA)
     * @param auth     данные авторизации
     */
    @Override
    public void applyAccessCriteria(final DetachedCriteria criteria, final AuthorizationData auth) {
        if (!auth.isAdministrator()) {
            final Disjunction disjunction = Restrictions.disjunction();
            final Set<Integer> userIds = auth.getUserIds();
            disjunction.add(Restrictions.in("author.id", userIds));
            disjunction.add(Restrictions.in("controller.id", userIds));
            disjunction.add(Restrictions.in("responsible.id", userIds));
            //NOTE  Добавляются алиасы с fetch
            criteria.createAlias("personReaders", "personReaders", CriteriaSpecification.LEFT_JOIN);
            disjunction.add(Restrictions.in("personReaders.id", userIds));
            //NOTE  Добавляются алиасы с fetch
            criteria.createAlias("personEditors", "personEditors", CriteriaSpecification.LEFT_JOIN);
            disjunction.add(Restrictions.in("personEditors.id", userIds));
            //NOTE  Добавляются алиасы с fetch
            criteria.createAlias("recipientUsers", "recipientUsers", CriteriaSpecification.LEFT_JOIN);
            disjunction.add(Restrictions.in("recipientUsers.id", userIds));
            if (!auth.getRoles().isEmpty()) {
                //NOTE  Добавляются алиасы с fetch
                criteria.createAlias("roleReaders", "roleReaders", CriteriaSpecification.LEFT_JOIN);
                disjunction.add(Restrictions.in("roleReaders.id", auth.getRoleIds()));
                //NOTE  Добавляются алиасы с fetch
                criteria.createAlias("roleEditors", "roleEditors", CriteriaSpecification.LEFT_JOIN);
                disjunction.add(Restrictions.in("roleEditors.id", auth.getRoleIds()));
            }
            if (!auth.getGroups().isEmpty()) {
                //NOTE  Добавляются алиасы с fetch
                criteria.createAlias("recipientGroups", "recipientGroups", CriteriaSpecification.LEFT_JOIN);
                disjunction.add(Restrictions.in("recipientGroups.id", auth.getGroupIds()));
            }
            criteria.add(disjunction);
        }
    }

    /**
     * Получить список проектных статусов
     *
     * @return список идентифкаторов проектных статусов
     */
    @Override
    public Set<Integer> getDraftStatuses() {
        return ImmutableSet.of(DocumentStatus.DOC_PROJECT_1.getId());
    }
}