package ru.hitsl.sql.dao.impl.document;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.criterion.*;
import org.hibernate.type.StringType;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.entity.model.document.OutgoingDocument;
import ru.entity.model.referenceBook.Contragent;
import ru.entity.model.referenceBook.DeliveryType;
import ru.hitsl.sql.dao.impl.mapped.DocumentDaoImpl;
import ru.hitsl.sql.dao.interfaces.document.OutgoingDocumentDao;
import ru.hitsl.sql.dao.util.AuthorizationData;
import ru.hitsl.sql.dao.util.DocumentSearchMapKeys;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.Temporal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hibernate.sql.JoinType.INNER_JOIN;
import static org.hibernate.sql.JoinType.LEFT_OUTER_JOIN;


@Repository("outgoingDocumentDao")
@Transactional(propagation = Propagation.MANDATORY)
public class OutgoingDocumentDaoImpl extends DocumentDaoImpl<OutgoingDocument> implements OutgoingDocumentDao {

    @Override
    public Class<OutgoingDocument> getEntityClass() {
        return OutgoingDocument.class;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // @Deprecated ПОЛНАЯ АХИНЕЯ
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Deprecated
    @Override
    public List<OutgoingDocument> findRegistratedDocumentsByCriteria(String in_criteria) {
        DetachedCriteria detachedCriteria = getSimpleCriteria().add(Restrictions.ilike("registrationNumber", in_criteria, MatchMode.ANYWHERE));
        final LocalDate currentDate = LocalDate.now();
        detachedCriteria.add(
                Restrictions.sqlRestriction(
                        "DATE_FORMAT(registrationDate, '%Y') like lower(?)", currentDate.getYear() + "%", new StringType()
                )
        );
        return getItems(detachedCriteria);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Работа со списками документов
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public List<OutgoingDocument> findAllDocumentsByReasonDocumentId(final String rootDocumentId) {
        return getItems(getFullCriteria().add(Restrictions.eq("reasonDocumentId", rootDocumentId)));
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
        result.createAlias("deliveryType", "deliveryType", LEFT_OUTER_JOIN);
        result.createAlias("personReaders", "personReaders", LEFT_OUTER_JOIN);
        result.createAlias("personEditors", "personEditors", LEFT_OUTER_JOIN);
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
            case DocumentSearchMapKeys.EXECUTORS:
                //Исполнители
                conjunction.add(Restrictions.in("executors", value));
                break;
            case DocumentSearchMapKeys.DELIVERY_TYPE:
                //Тип доставки
                conjunction.add(Restrictions.eq("deliveryType", value));
                break;
            case DocumentSearchMapKeys.CONTRAGENT:
                // Контрагент
                conjunction.add(Restrictions.eq("contragent", value));
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
        result.createAlias("executor", "executor", LEFT_OUTER_JOIN);
        result.createAlias("contragent", "contragent", LEFT_OUTER_JOIN);
        return result;
    }

    /**
     * Производит поиск заданной строки в (по условию ИЛИ [дизъюнкция]):
     * заданных полях сущности
     * регистрационном номере,
     * кратком наименовании адресата (контрагент),
     * дате регистрации (формат - 'DD.MM.YYYY'),
     * кратком описании,
     * ФИО исполнителя,
     * виде документа,
     * наименовании статуса документа
     * @param criteria критерий отбора в который будет добавлено поисковое условие (НЕ менее LIST_CRITERIA)
     * @param filter   условие поиска
     */
    @Override
    public void applyFilter(final DetachedCriteria criteria, final String filter) {
        if (StringUtils.isEmpty(filter)) {
            return;
        }
        final Disjunction disjunction = Restrictions.disjunction();
        disjunction.add(Restrictions.ilike("registrationNumber", filter, MatchMode.ANYWHERE));
        disjunction.add(Restrictions.ilike("contragent.shortName", filter, MatchMode.ANYWHERE));
        disjunction.add(Restrictions.ilike("contragent.value", filter, MatchMode.ANYWHERE));
        disjunction.add(createDateLikeTextRestriction("registrationDate", filter));
        disjunction.add(Restrictions.ilike("shortDescription", filter, MatchMode.ANYWHERE));

        disjunction.add(Restrictions.ilike("executor.lastName", filter, MatchMode.ANYWHERE));
        disjunction.add(Restrictions.ilike("executor.middleName", filter, MatchMode.ANYWHERE));
        disjunction.add(Restrictions.ilike("executor.firstName", filter, MatchMode.ANYWHERE));
        disjunction.add(Restrictions.ilike("form.value", filter, MatchMode.ANYWHERE));

        //TODO справочник в БД
//        final List<DocumentStatus> statuses = DocumentType.getOutgoingDocumentStatuses();
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
     * //NOTE  Добавляются алиасы с fetch
     * @param criteria исходный критерий   (минимум LIST_CRITERIA)
     * @param auth     данные авторизации
     */
    @Override
    public void applyAccessCriteria(final DetachedCriteria criteria, final AuthorizationData auth) {
        if (!auth.isAdministrator()) {
            final Disjunction disjunction = Restrictions.disjunction();
            final Set<Integer> userIds = auth.getUserIds();
            disjunction.add(Restrictions.in("author.id", userIds));
            disjunction.add(Restrictions.in("executor.id", userIds));
            disjunction.add(Restrictions.in("controller.id", userIds));
            //NOTE  Добавляются алиасы с fetch
            criteria.createAlias("personReaders", "personReaders", LEFT_OUTER_JOIN);
            disjunction.add(Restrictions.in("personReaders.id", userIds));
            //NOTE  Добавляются алиасы с fetch
            criteria.createAlias("personEditors", "personEditors", LEFT_OUTER_JOIN);
            disjunction.add(Restrictions.in("personEditors.id", userIds));
            //NOTE  Добавляются алиасы с fetch
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