package ru.hitsl.sql.dao.impl.document;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.criterion.*;
import org.hibernate.type.StringType;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.entity.model.document.InternalDocument;
import ru.hitsl.sql.dao.impl.mapped.DocumentDaoImpl;
import ru.hitsl.sql.dao.interfaces.document.InternalDocumentDao;
import ru.hitsl.sql.dao.util.AuthorizationData;
import ru.hitsl.sql.dao.util.DocumentSearchMapKeys;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.Temporal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hibernate.sql.JoinType.INNER_JOIN;
import static org.hibernate.sql.JoinType.LEFT_OUTER_JOIN;


@Repository("internalDocumentDao")
@Transactional(propagation = Propagation.MANDATORY)
public class InternalDocumentDaoImpl extends DocumentDaoImpl<InternalDocument> implements InternalDocumentDao {


    @Override
    public Class<InternalDocument> getEntityClass() {
        return InternalDocument.class;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // @Deprecated ПОЛНАЯ АХИНЕЯ
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Deprecated
    @SuppressWarnings("unchecked")
    public List<InternalDocument> findDocumentsByCriteria(Map<String, Object> in_map, boolean showDeleted) {
        DetachedCriteria in_searchCriteria = getListCriteria();
        applyDeletedRestriction(in_searchCriteria, showDeleted);
        LocalDateTime currentDate;
        if (in_map.get("DEPRECATED_REGISTRATION_DATE") != null) {
            currentDate = (LocalDateTime) in_map.get("DEPRECATED_REGISTRATION_DATE");
        } else {
            currentDate = LocalDateTime.now();
        }
        in_searchCriteria.add(Restrictions.sqlRestriction("DATE_FORMAT(registrationDate, '%Y') like lower(?)",
                currentDate.getYear() + "%", new StringType()));
        applyFilter(in_searchCriteria, in_map);
        return getItems(in_searchCriteria);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Критерии для различных вариантов отображения документов
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Получить критерий для отбора Документов с максимальным количеством FETCH
     * @return критерий для документов с DISTINCT и нужными alias (with fetch strategy)
     */
    @Override
    public DetachedCriteria getFullCriteria() {
        final DetachedCriteria result = getListCriteria();
        result.createAlias("personEditors", "personEditors", LEFT_OUTER_JOIN);
        result.createAlias("personReaders", "personReaders", LEFT_OUTER_JOIN);
        result.createAlias("recipientGroups", "recipientGroups", LEFT_OUTER_JOIN);
        result.createAlias("recipientUsers", "recipientUsers", LEFT_OUTER_JOIN);
        result.createAlias("recipientGroups.members", "recipientGroups.members", LEFT_OUTER_JOIN);
        result.createAlias("roleReaders", "roleReaders", LEFT_OUTER_JOIN);
        result.createAlias("roleEditors", "roleEditors", LEFT_OUTER_JOIN);
        result.createAlias("userAccessLevel", "userAccessLevel", INNER_JOIN);
        result.createAlias("history", "history", LEFT_OUTER_JOIN);
        return result;
    }

    @Override
    public void addDocumentMapFilter(Conjunction conjunction, String key, Object value) {
        switch (key) {
            case DocumentSearchMapKeys.SIGNATURE_DATE_START:
                //Дата подписания от
                conjunction.add(Restrictions.ge("signatureDate", value));
                break;
            case DocumentSearchMapKeys.SIGNATURE_DATE_END:
                //Дата подписания до
                conjunction.add(Restrictions.le("signatureDate", ((Temporal) value).plus(Duration.ofDays(1))));
                break;
            case DocumentSearchMapKeys.EXECUTION_DATE_START:
                //Дата исполнения от
                conjunction.add(Restrictions.ge("executionDate", value));
                break;
            case DocumentSearchMapKeys.EXECUTION_DATE_END:
                //Дата исполнения до
                conjunction.add(Restrictions.le("executionDate", ((Temporal) value).plus(Duration.ofDays(1))));
                break;
            case DocumentSearchMapKeys.RESPONSIBLE:
                //Ответственный
                conjunction.add(Restrictions.eq("responsible", value));
                break;
            case DocumentSearchMapKeys.RECIPIENTS:
                //Адресаты
                conjunction.add(Restrictions.in("recipientUsers", value));
                break;
            case DocumentSearchMapKeys.EXECUTORS:
                //Исполнители
                conjunction.add(Restrictions.in("executors", value));
                break;
            case DocumentSearchMapKeys.CLOSE_PERIOD_REGISTRATION_FLAG:
                //Регистрация закрытого периода
                conjunction.add(Restrictions.eq("closePeriodRegistrationFlag", Boolean.valueOf(value.toString())));
                break;
            default:
                log.warn("FilterMapCriteria: Unknown key \'{}\' (value =\'{}\')", key, value);
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Работа с критериями (общая)
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Получить критерий для отбора Документов и их показа в расширенных списках
     * Обычно:
     * Автор - INNER
     * Руководитель - LEFT
     * Вид документа - LEFT
     * ++ В зависимости от вида
     * @return критерий для документов с DISTINCT и нужными alias (with fetch strategy)
     */
    @Override
    public DetachedCriteria getListCriteria() {
        final DetachedCriteria result = super.getListCriteria();
        result.createAlias("responsible", "responsible", LEFT_OUTER_JOIN);
        return result;
    }

    /**
     * Производит поиск заданной строки в (по условию ИЛИ [дизъюнкция]):
     * заданных полях сущности
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
        disjunction.add(createDateLikeTextRestriction("signatureDate", filter));
        disjunction.add(createDateLikeTextRestriction("executionDate", filter));
        disjunction.add(Restrictions.ilike("shortDescription", filter, MatchMode.ANYWHERE));
        disjunction.add(Restrictions.ilike("responsible.lastName", filter, MatchMode.ANYWHERE));
        disjunction.add(Restrictions.ilike("responsible.middleName", filter, MatchMode.ANYWHERE));
        disjunction.add(Restrictions.ilike("responsible.firstName", filter, MatchMode.ANYWHERE));
        disjunction.add(Restrictions.ilike("form.value", filter, MatchMode.ANYWHERE));
        disjunction.add(Restrictions.ilike("registrationNumber", filter, MatchMode.ANYWHERE));

        //TODO справочник в БД
//        final List<DocumentStatus> statuses = DocumentType.getInternalDocumentStatuses();
//        final List<Integer> statusIdList = new ArrayList<>(statuses.size());
//        for (DocumentStatus current : statuses) {
//            if (current.getName().contains(filter)) {
//                statusIdList.add(current.getId());
//            }
//        }
//        if (!statusIdList.isEmpty()) {
//            disjunction.add(Restrictions.in("statusId", statusIdList));
//        }

        criteria.add(disjunction);
    }

    /**
     * Применить ограничения допуска для документов
     * @param criteria исходный критерий   (минимум LIST_CRITERIA)
     * @param auth     данные авторизации
     */
    @Override
    public void applyAccessCriteria(final DetachedCriteria criteria, final AuthorizationData auth) {
        //NOTE  Добавляются алиасы с fetch
        criteria.createAlias("recipientUsers", "recipientUsers", LEFT_OUTER_JOIN);
        criteria.createAlias("recipientGroups", "recipientGroups", LEFT_OUTER_JOIN);

        if (!auth.isAdministrator()) {
            final Disjunction disjunction = Restrictions.disjunction();
            final Set<Integer> userIds = auth.getUserIds();
            disjunction.add(Restrictions.in("author.id", userIds));
            disjunction.add(Restrictions.in("controller.id", userIds));
            disjunction.add(Restrictions.in("responsible.id", userIds));
            disjunction.add(Restrictions.in("recipientUsers.id", userIds));
            //NOTE  Добавляются алиасы с fetch
            criteria.createAlias("personReaders", "personReaders", LEFT_OUTER_JOIN);
            disjunction.add(Restrictions.in("personReaders.id", userIds));
            //NOTE  Добавляются алиасы с fetch
            criteria.createAlias("personEditors", "personEditors", LEFT_OUTER_JOIN);
            disjunction.add(Restrictions.in("personEditors.id", userIds));
            if (!auth.getRoles().isEmpty()) {
                //NOTE  Добавляются алиасы с fetch
                criteria.createAlias("roleReaders", "roleReaders", LEFT_OUTER_JOIN);
                disjunction.add(Restrictions.in("roleReaders.id", auth.getRoleIds()));
                //NOTE  Добавляются алиасы с fetch
                criteria.createAlias("roleEditors", "roleEditors", LEFT_OUTER_JOIN);
                disjunction.add(Restrictions.in("roleEditors.id", auth.getRoleIds()));
            }
            if (!auth.getGroups().isEmpty()) {
                disjunction.add(Restrictions.in("recipientGroups.id", auth.getGroupIds()));
            }
            criteria.add(disjunction);
        }
    }

}