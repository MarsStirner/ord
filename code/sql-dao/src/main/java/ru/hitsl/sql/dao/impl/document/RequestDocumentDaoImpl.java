package ru.hitsl.sql.dao.impl.document;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.criterion.*;
import org.hibernate.sql.JoinType;
import org.hibernate.type.StringType;
import org.springframework.stereotype.Repository;
import ru.entity.model.document.RequestDocument;
import ru.entity.model.enums.DocumentStatus;
import ru.entity.model.enums.DocumentType;
import ru.entity.model.referenceBook.Contragent;
import ru.entity.model.referenceBook.DeliveryType;
import ru.entity.model.referenceBook.DocumentForm;
import ru.hitsl.sql.dao.impl.mapped.DocumentDaoImpl;
import ru.hitsl.sql.dao.interfaces.document.RequestDocumentDao;
import ru.hitsl.sql.dao.util.AuthorizationData;
import ru.hitsl.sql.dao.util.DocumentSearchMapKeys;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hibernate.criterion.CriteriaSpecification.INNER_JOIN;
import static ru.util.ApplicationHelper.getNextDayDate;

@Repository("requestDocumentDao")
public class RequestDocumentDaoImpl extends DocumentDaoImpl<RequestDocument> implements RequestDocumentDao{

    @Override
    public Class<RequestDocument> getEntityClass() {
        return RequestDocument.class;
    }

    @SuppressWarnings("unchecked")
    public List<RequestDocument> findRegistratedDocuments() {
        DetachedCriteria detachedCriteria = getSimpleCriteria();
        detachedCriteria.add(Restrictions.isNotNull("registrationNumber"));
        Calendar calendar = Calendar.getInstance();
        detachedCriteria.add(Restrictions.sqlRestriction("DATE_FORMAT(this_.registrationDate, '%Y') like lower(?)", calendar.get(Calendar.YEAR) + "%", new StringType()));
        return getItems(detachedCriteria);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Критерии для различных вариантов отображения документов
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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
        final DetachedCriteria result = getSimpleCriteria();
        result.createAlias("author", "author", CriteriaSpecification.INNER_JOIN);
        result.createAlias("form", "form", JoinType.LEFT_OUTER_JOIN);
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
        result.createAlias("responsible", "responsible", JoinType.LEFT_OUTER_JOIN);
        result.createAlias("deliveryType", "deliveryType", JoinType.LEFT_OUTER_JOIN);
        result.createAlias("senderType", "senderType", JoinType.LEFT_OUTER_JOIN);
        result.createAlias("region", "region", JoinType.LEFT_OUTER_JOIN);
        result.createAlias("contragent", "contragent", JoinType.LEFT_OUTER_JOIN);
        result.createAlias("recipientUsers", "recipientUsers", JoinType.LEFT_OUTER_JOIN);
        result.createAlias("recipientGroups", "recipientGroups", JoinType.LEFT_OUTER_JOIN);
        result.createAlias("recipientGroups.members", "recipientGroups.members", JoinType.LEFT_OUTER_JOIN);
        result.createAlias("personReaders", "personReaders", JoinType.LEFT_OUTER_JOIN);
        result.createAlias("personEditors", "personEditors", JoinType.LEFT_OUTER_JOIN);
        result.createAlias("roleReaders", "roleReaders", JoinType.LEFT_OUTER_JOIN);
        result.createAlias("roleEditors", "roleEditors", JoinType.LEFT_OUTER_JOIN);
        result.createAlias("history", "history", JoinType.LEFT_OUTER_JOIN);
        return result;
    }


    /**
     * Применитиь к текущим критериям огарничения сложного фильтра
     *
     * @param criteria текущий критерий, в который будут добавлены условия  (НЕ менее LIST_CRITERIA)
     * @param filters  сложный фильтр (карта)
     */
    @Override
    public void applyFilter(final DetachedCriteria criteria, final Map<String, Object> filters) {
        if (filters == null || filters.isEmpty()) {
            log.debug("FilterMapCriteria: null or empty. Skip.");
            return;
        }
        final Conjunction conjunction = Restrictions.conjunction();
        for (Map.Entry<String, Object> entry : filters.entrySet()) {
            final String key = entry.getKey();
            final Object value = entry.getValue();

            if (StringUtils.isEmpty(key)) {
                // Пропустить запись с пустым ключом
                log.warn("FilterMapCriteria: empty key for \'{}\'", value);
            } else if (DocumentSearchMapKeys.REGISTRATION_NUMBER_KEY.equals(key)) {
                conjunction.add(Restrictions.ilike("registrationNumber", (String) value, MatchMode.ANYWHERE));
            } else if (DocumentSearchMapKeys.START_REGISTRATION_DATE_KEY.equals(key)) {
                conjunction.add(Restrictions.ge("registrationDate", value));
            } else if (DocumentSearchMapKeys.END_REGISTRATION_DATE_KEY.equals(key)) {
                conjunction.add(Restrictions.le("registrationDate", getNextDayDate((Date) value)));
            } else if (DocumentSearchMapKeys.START_CREATION_DATE_KEY.equals(key)) {
                conjunction.add(Restrictions.ge("creationDate", value));
            } else if (DocumentSearchMapKeys.END_CREATION_DATE_KEY.equals(key)) {
                conjunction.add(Restrictions.le("creationDate", getNextDayDate((Date) value)));
            } else if (DocumentSearchMapKeys.START_DELIVERY_DATE_KEY.equals(key)) {
                conjunction.add(Restrictions.ge("deliveryDate", value));
            } else if (DocumentSearchMapKeys.END_DELIVERY_DATE_KEY.equals(key)) {
                conjunction.add(Restrictions.le("deliveryDate", getNextDayDate((Date) value)));
            } else if (DocumentSearchMapKeys.SHORT_DESCRIPTION_KEY.equals(key) && StringUtils.isNotEmpty((String) value)) {
                conjunction.add(Restrictions.ilike("shortDescription", (String) value, MatchMode.ANYWHERE));
            } else if (DocumentSearchMapKeys.SENDER_FIRST_NAME_KEY.equals(key) && StringUtils.isNotEmpty((String) value)) {
                conjunction.add(Restrictions.ilike("senderFirstName", (String) value, MatchMode.ANYWHERE));
            } else if (DocumentSearchMapKeys.SENDER_LAST_NAME_KEY.equals(key) && StringUtils.isNotEmpty((String) value)) {
                conjunction.add(Restrictions.ilike("senderLastName", (String) value, MatchMode.ANYWHERE));
            } else if (DocumentSearchMapKeys.SENDER_PATR_NAME_KEY.equals(key) && StringUtils.isNotEmpty((String) value)) {
                conjunction.add(Restrictions.ilike("senderMiddleName", (String) value, MatchMode.ANYWHERE));
            } else if (DocumentSearchMapKeys.STATUS_KEY.equals(key) && StringUtils.isNotEmpty((String) value)) {
                conjunction.add(Restrictions.eq("statusId", Integer.valueOf((String) value)));
            } else if (DocumentSearchMapKeys.STATUS_LIST_KEY.equals(key)) {
                conjunction.add(Restrictions.in("statusId", (Set<Integer>) value));
            } else if (DocumentSearchMapKeys.CONTROLLER_KEY.equals(key)) {
                createUserEqRestriction(conjunction, "controller.id", value);
            } else if (DocumentSearchMapKeys.RECIPIENTS_KEY.equals(key)) {
                createUserListInRestriction(conjunction, "recipientUsers.id", value);
            } else if (DocumentSearchMapKeys.AUTHOR_KEY.equals(key)) {
                createUserEqRestriction(conjunction, "author.id", value);
            } else if (DocumentSearchMapKeys.AUTHORS_KEY.equals(key)) {
                createUserListInRestriction(conjunction, "author.id", value);
            } else if (DocumentSearchMapKeys.RESPONSIBLE_KEY.equals(key)) {
                createUserEqRestriction(conjunction, "responsible.id", value);
            } else if (DocumentSearchMapKeys.DELIVERY_TYPE_KEY.equals(key)) {
                try {
                    final DeliveryType deliveryType = (DeliveryType) value;
                    conjunction.add(Restrictions.eq("deliveryType.id", deliveryType.getId()));
                } catch (ClassCastException e) {
                    log.error("Exception while forming FilterMapCriteria: [{}]=\'{}\' IS NOT DeliveryType. Non critical, continue...", key, value);
                }
            } else if (DocumentSearchMapKeys.START_EXECUTION_DATE_KEY.equals(key)) {
                conjunction.add(Restrictions.ge("executionDate", value));
            } else if (DocumentSearchMapKeys.END_EXECUTION_DATE_KEY.equals(key)) {
                conjunction.add(Restrictions.le("executionDate", getNextDayDate((Date) value)));
            } else if ("executionDate".equals(key)) {
                conjunction.add(Restrictions.isNotNull("executionDate"));
            } else if (DocumentSearchMapKeys.FORM_KEY.equals(key)) {
                try {
                    final DocumentForm form = (DocumentForm) value;
                    conjunction.add(Restrictions.eq("form.id", form.getId()));
                } catch (ClassCastException e) {
                    log.error("Exception while forming FilterMapCriteria: [{}]=\'{}\' IS NOT DocumentForm. Non critical, continue...", key, value);
                }
            } else if (DocumentSearchMapKeys.CONTRAGENT_KEY.equals(key)) {
                try {
                    final Contragent contragent = (Contragent) value;
                    conjunction.add(Restrictions.eq("contragent.id", contragent.getId()));
                } catch (ClassCastException e) {
                    log.error("Exception while forming FilterMapCriteria: [{}]=\'{}\' IS NOT Contragent. Non critical, continue...", key, value);
                }
            } else if (DocumentSearchMapKeys.FORM_VALUE_KEY.equals(key)) {
                conjunction.add(Restrictions.eq("form.value", value));
            } else if (DocumentSearchMapKeys.FORM_CATEGORY_KEY.equals(key)) {
                criteria.createAlias("form.documentType", "documentType", INNER_JOIN);
                conjunction.add(Restrictions.eq("documentType.code", value));
            } else {
                log.warn("FilterMapCriteria: Unknown key \'{}\' (value =\'{}\')", key, value);
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
    public void applyFilter(final DetachedCriteria criteria, final String filter) {
        if (StringUtils.isEmpty(filter)) {
            log.debug("FilterCriteria: empty or null filter. Skip.");
            return;
        }
        final Disjunction disjunction = Restrictions.disjunction();
        disjunction.add(Restrictions.ilike("registrationNumber", filter, MatchMode.ANYWHERE));
        disjunction.add(createDateLikeTextRestriction("executionDate", filter));
        disjunction.add(createDateLikeTextRestriction("deliveryDate", filter));
        disjunction.add(Restrictions.ilike("shortDescription", filter, MatchMode.ANYWHERE));
        disjunction.add(Restrictions.ilike("senderFirstName", filter, MatchMode.ANYWHERE));
        disjunction.add(Restrictions.ilike("senderMiddleName", filter, MatchMode.ANYWHERE));
        disjunction.add(Restrictions.ilike("senderLastName", filter, MatchMode.ANYWHERE));

        disjunction.add(Restrictions.ilike("form.value", filter, MatchMode.ANYWHERE));

        //TODO справочник в БД
        final List<DocumentStatus> statuses = DocumentType.getRequestDocumentStatuses();
        final List<Integer> statusIdList = new ArrayList<>(statuses.size());
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
            disjunction.add(Restrictions.in("responsible.id", userIds));
            //NOTE  Добавляются алиасы с fetch
            criteria.createAlias("controller", "controller", JoinType.LEFT_OUTER_JOIN);
            disjunction.add(Restrictions.in("controller.id", userIds));
            //NOTE  Добавляются алиасы с fetch
            criteria.createAlias("recipientUsers", "recipientUsers", JoinType.LEFT_OUTER_JOIN);
            disjunction.add(Restrictions.in("recipientUsers.id", userIds));
            //NOTE  Добавляются алиасы с fetch
            criteria.createAlias("personReaders", "personReaders", JoinType.LEFT_OUTER_JOIN);
            disjunction.add(Restrictions.in("personReaders.id", userIds));
            //NOTE  Добавляются алиасы с fetch
            criteria.createAlias("personEditors", "personEditors", JoinType.LEFT_OUTER_JOIN);
            disjunction.add(Restrictions.in("personEditors.id", userIds));
            if (!auth.getGroups().isEmpty()) {
                //NOTE  Добавляются алиасы с fetch
                criteria.createAlias("recipientGroups", "recipientGroups", JoinType.LEFT_OUTER_JOIN);
                disjunction.add(Restrictions.in("recipientGroups.id", auth.getGroupIds()));
            }
            if (!auth.getRoles().isEmpty()) {
                //NOTE  Добавляются алиасы с fetch
                criteria.createAlias("roleReaders", "roleReaders", JoinType.LEFT_OUTER_JOIN);
                disjunction.add(Restrictions.in("roleReaders.id", auth.getRoleIds()));
                //NOTE  Добавляются алиасы с fetch
                criteria.createAlias("roleEditors", "roleEditors", JoinType.LEFT_OUTER_JOIN);
                disjunction.add(Restrictions.in("roleEditors.id", auth.getRoleIds()));
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
        return Stream.of(DocumentStatus.DOC_PROJECT_1.getId()).collect(Collectors.toSet());
    }
}