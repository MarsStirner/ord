package ru.hitsl.sql.dao.impl.document;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.criterion.*;
import org.hibernate.sql.JoinType;
import org.hibernate.type.StringType;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.entity.model.document.RequestDocument;
import ru.hitsl.sql.dao.impl.mapped.DocumentDaoImpl;
import ru.hitsl.sql.dao.interfaces.document.RequestDocumentDao;
import ru.hitsl.sql.dao.util.AuthorizationData;
import ru.hitsl.sql.dao.util.DocumentSearchMapKeys;

import java.time.Duration;
import java.time.temporal.Temporal;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

import static org.hibernate.sql.JoinType.LEFT_OUTER_JOIN;


@Repository("requestDocumentDao")
@Transactional(propagation = Propagation.MANDATORY)
public class RequestDocumentDaoImpl extends DocumentDaoImpl<RequestDocument> implements RequestDocumentDao {

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
     * Получить критерий для отбора Документов с максимальным количеством FETCH
     * @return критерий для документов с DISTINCT и нужными alias (with fetch strategy)
     */
    @Override
    public DetachedCriteria getFullCriteria() {
        final DetachedCriteria result = getListCriteria();
        result.createAlias("responsible", "responsible", LEFT_OUTER_JOIN);
        result.createAlias("deliveryType", "deliveryType", LEFT_OUTER_JOIN);
        result.createAlias("senderType", "senderType", LEFT_OUTER_JOIN);
        result.createAlias("region", "region", LEFT_OUTER_JOIN);
        result.createAlias("contragent", "contragent", LEFT_OUTER_JOIN);
        result.createAlias("recipientUsers", "recipientUsers", LEFT_OUTER_JOIN);
        result.createAlias("recipientGroups", "recipientGroups", LEFT_OUTER_JOIN);
        result.createAlias("recipientGroups.members", "recipientGroups.members", LEFT_OUTER_JOIN);
        result.createAlias("personReaders", "personReaders", LEFT_OUTER_JOIN);
        result.createAlias("personEditors", "personEditors", LEFT_OUTER_JOIN);
        result.createAlias("roleReaders", "roleReaders", LEFT_OUTER_JOIN);
        result.createAlias("roleEditors", "roleEditors", LEFT_OUTER_JOIN);
        result.createAlias("history", "history", LEFT_OUTER_JOIN);
        result.createAlias("userAccessLevel", "userAccessLevel", JoinType.INNER_JOIN);
        return result;
    }

    @Override
    public void addDocumentMapFilter(Conjunction conjunction, String key, Object value) {
        switch (key) {
            case DocumentSearchMapKeys.EXECUTION_DATE:
                //Дата исполнения не NULL
                conjunction.add(Restrictions.isNotNull("executionDate"));
                break;
            case DocumentSearchMapKeys.EXECUTION_DATE_START:
                //Дата исполнения от
                conjunction.add(Restrictions.ge("executionDate", value));
                break;
            case DocumentSearchMapKeys.EXECUTION_DATE_END:
                //Дата исполнения до
                conjunction.add(Restrictions.le("executionDate", ((Temporal) value).plus(Duration.ofDays(1))));
                break;
            case DocumentSearchMapKeys.DELIVERY_DATE_START:
                // Дата поступившего от
                conjunction.add(Restrictions.ge("deliveryDate", value));
                break;
            case DocumentSearchMapKeys.DELIVERY_DATE_END:
                // Дата поступившего до
                conjunction.add(Restrictions.le("deliveryDate", ((Temporal) value).plus(Duration.ofDays(1))));
                break;
            case DocumentSearchMapKeys.RESPONSIBLE:
                //Ответственный
                conjunction.add(Restrictions.eq("responsible", value));
                break;
            case DocumentSearchMapKeys.SENDER_FIRST_NAME:
                //Имя отправителя
                conjunction.add(Restrictions.ilike("senderFirstName", (String) value, MatchMode.ANYWHERE));
                break;
            case DocumentSearchMapKeys.SENDER_LAST_NAME:
                //Фамилия отправителя
                conjunction.add(Restrictions.ilike("senderLastName", (String) value, MatchMode.ANYWHERE));
                break;
            case DocumentSearchMapKeys.SENDER_PATR_NAME:
                //Отчество отправителя
                conjunction.add(Restrictions.ilike("senderMiddleName", (String) value, MatchMode.ANYWHERE));
                break;
            case DocumentSearchMapKeys.DELIVERY_TYPE:
                //Тип доставки
                conjunction.add(Restrictions.eq("deliveryType", value));
                break;
            case DocumentSearchMapKeys.RECIPIENTS:
                // Адресаты
                conjunction.add(Restrictions.in("recipientUsers", value));
                break;
            case DocumentSearchMapKeys.CONTRAGENT:
                // Контрагент
                conjunction.add(Restrictions.eq("contragent", value));
                break;
//            case DocumentSearchMapKeys.OFFICE_KEEPING_VOLUME:
//                // Том дела TODO ахинея, надо переделывать всю архивацию
//                conjunction.add(Restrictions.in("id", ((OfficeKeepingVolume) value).getId()));
//                break;
            default:
                log.warn("FilterMapCriteria: Unknown key \'{}\' (value =\'{}\')", key, value);
        }
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
        disjunction.add(Restrictions.ilike("registrationNumber", filter, MatchMode.ANYWHERE));
        disjunction.add(createDateLikeTextRestriction("executionDate", filter));
        disjunction.add(createDateLikeTextRestriction("deliveryDate", filter));
        disjunction.add(Restrictions.ilike("shortDescription", filter, MatchMode.ANYWHERE));
        disjunction.add(Restrictions.ilike("senderFirstName", filter, MatchMode.ANYWHERE));
        disjunction.add(Restrictions.ilike("senderMiddleName", filter, MatchMode.ANYWHERE));
        disjunction.add(Restrictions.ilike("senderLastName", filter, MatchMode.ANYWHERE));
        disjunction.add(Restrictions.ilike("form.value", filter, MatchMode.ANYWHERE));
        disjunction.add(Restrictions.in("status.value", filter, MatchMode.START));
        criteria.add(disjunction);
    }


    /**
     * Применить ограничения допуска для документов
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
            criteria.createAlias("controller", "controller", LEFT_OUTER_JOIN);
            disjunction.add(Restrictions.in("controller.id", userIds));
            //NOTE  Добавляются алиасы с fetch
            criteria.createAlias("recipientUsers", "recipientUsers", LEFT_OUTER_JOIN);
            disjunction.add(Restrictions.in("recipientUsers.id", userIds));
            //NOTE  Добавляются алиасы с fetch
            criteria.createAlias("personReaders", "personReaders", LEFT_OUTER_JOIN);
            disjunction.add(Restrictions.in("personReaders.id", userIds));
            //NOTE  Добавляются алиасы с fetch
            criteria.createAlias("personEditors", "personEditors", LEFT_OUTER_JOIN);
            disjunction.add(Restrictions.in("personEditors.id", userIds));
            if (!auth.getGroups().isEmpty()) {
                //NOTE  Добавляются алиасы с fetch
                criteria.createAlias("recipientGroups", "recipientGroups", LEFT_OUTER_JOIN);
                disjunction.add(Restrictions.in("recipientGroups.id", auth.getGroupIds()));
            }
            if (!auth.getRoles().isEmpty()) {
                //NOTE  Добавляются алиасы с fetch
                criteria.createAlias("roleReaders", "roleReaders", LEFT_OUTER_JOIN);
                disjunction.add(Restrictions.in("roleReaders.id", auth.getRoleIds()));
                //NOTE  Добавляются алиасы с fetch
                criteria.createAlias("roleEditors", "roleEditors", LEFT_OUTER_JOIN);
                disjunction.add(Restrictions.in("roleEditors.id", auth.getRoleIds()));
            }
            criteria.add(disjunction);
        }
    }
}