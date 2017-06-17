package ru.hitsl.sql.dao.impl.document;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.criterion.*;
import org.hibernate.type.StringType;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.entity.model.document.IncomingDocument;
import ru.entity.model.document.OfficeKeepingVolume;
import ru.entity.model.workflow.Status;
import ru.hitsl.sql.dao.impl.mapped.DocumentDaoImpl;
import ru.hitsl.sql.dao.interfaces.document.IncomingDocumentDao;
import ru.hitsl.sql.dao.util.AuthorizationData;
import ru.hitsl.sql.dao.util.DocumentSearchMapKeys;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.Temporal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hibernate.sql.JoinType.INNER_JOIN;
import static org.hibernate.sql.JoinType.LEFT_OUTER_JOIN;


@Repository("incomingDocumentDao")
@Transactional(propagation = Propagation.MANDATORY)
public class IncomingDocumentDaoImpl extends DocumentDaoImpl<IncomingDocument> implements IncomingDocumentDao {

    @Override
    public void addDocumentMapFilter(Conjunction conjunction, String key, Object value) {
        switch (key) {
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
            case DocumentSearchMapKeys.RECEIVED_DATE_START:
                //Дата доставки от
                conjunction.add(Restrictions.ge("receivedDocumentDate", value));
                break;
            case DocumentSearchMapKeys.RECEIVED_DATE_END:
                //Дата доставки до
                conjunction.add(Restrictions.le("receivedDocumentDate", ((Temporal) value).plus(Duration.ofDays(1))));
                break;
            case DocumentSearchMapKeys.RECEIVED_DOCUMENT_NUMBER:
                //Номер поступившего
                conjunction.add(Restrictions.ilike("receivedDocumentNumber", (String) value, MatchMode.ANYWHERE));
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
     * Получить критерий для отбора Документов и их показа в расширенных списках
     * Исполнители - LEFT
     * Вид документа - LEFT
     * Корреспондент - LEFT
     * @return критерий для документов с DISTINCT
     */
    @Override
    public DetachedCriteria getListCriteria() {
        final DetachedCriteria result = super.getListCriteria();
        result.createAlias("executors", "executors", LEFT_OUTER_JOIN);
        result.createAlias("recipientUsers", "recipientUsers", LEFT_OUTER_JOIN);
        result.createAlias("contragent", "contragent", LEFT_OUTER_JOIN);
        return result;
    }

    @Override
    public Class<IncomingDocument> getEntityClass() {
        return IncomingDocument.class;
    }

    @Override
    public List<IncomingDocument> findControlledDocumentsByUser(final String filter, final AuthorizationData authData, final LocalDateTime controlDate) {
        final DetachedCriteria criteria = getListCriteria();
        applyFilter(criteria, filter);
        criteria.add(Restrictions.in("status", Stream.of(Status.REGISTERED, Status.ON_EXECUTION).collect(Collectors.toSet())));
        filterDeleted(criteria);
        if (!authData.isAdministrator()) {
            criteria.add(Restrictions.in("controller.id", authData.getUserIds()));
        }
        criteria.add(Restrictions.isNotNull("executionDate"));
        if (controlDate != null) {
            criteria.add(Restrictions.le("executionDate", controlDate));
        }
        criteria.addOrder(Order.desc("executionDate"));
        return getItems(criteria);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Критерии для различных вариантов отображения документов
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // @Deprecated ПОЛНАЯ АХИНЕЯ
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Deprecated
    @Override
    public List<IncomingDocument> findRegistratedDocumentsByCriteria(String in_criteria) {
        DetachedCriteria detachedCriteria = getSimpleCriteria().add(Restrictions.ilike("registrationNumber", in_criteria, MatchMode.ANYWHERE));
        final LocalDate currentDate = LocalDate.now();
        detachedCriteria.add(
                Restrictions.sqlRestriction(
                        "DATE_FORMAT(this_.registrationDate, '%Y') like lower(?)", currentDate.getYear() + "%", new StringType()
                )
        );
        return getItems(detachedCriteria);
    }

    /**
     * Производит поиск заданной строки в (по условию ИЛИ [дизъюнкция]):
     * регистрационном номере,
     * Номеру поступившего,
     * дате регистрации (формат - 'DD.MM.YYYY'),
     * сроке исполнения (формат - 'DD.MM.YYYY'),
     * кратком описании,
     * ФИО исполнителей,
     * виде документа,
     * названии корреспондента (FULL или SHORT),
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
        disjunction.add(Restrictions.ilike("receivedDocumentNumber", filter, MatchMode.ANYWHERE));
        disjunction.add(createDateLikeTextRestriction("registrationDate", filter));
        disjunction.add(createDateLikeTextRestriction("executionDate", filter));
        disjunction.add(Restrictions.ilike("shortDescription", filter, MatchMode.ANYWHERE));

        disjunction.add(Restrictions.ilike("executors.lastName", filter, MatchMode.ANYWHERE));
        disjunction.add(Restrictions.ilike("executors.middleName", filter, MatchMode.ANYWHERE));
        disjunction.add(Restrictions.ilike("executors.firstName", filter, MatchMode.ANYWHERE));

        disjunction.add(Restrictions.ilike("form.value", filter, MatchMode.ANYWHERE));

        disjunction.add(Restrictions.ilike("contragent.value", filter, MatchMode.ANYWHERE));
        disjunction.add(Restrictions.ilike("contragent.shortName", filter, MatchMode.ANYWHERE));

        //TODO справочник в БД
//        final List<DocumentStatus> statuses = DocumentType.getIncomingDocumentStatuses();
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


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Работа с критериями (общая)
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Получить критерий для подгрузки из БД всех связанных сущностей
     * @return критерий для документов с DISTINCT
     */
    @Override
    public DetachedCriteria getFullCriteria() {
        final DetachedCriteria result = getListCriteria();
        result.createAlias("deliveryType", "deliveryType", LEFT_OUTER_JOIN);
        result.createAlias("personReaders", "personReaders", LEFT_OUTER_JOIN);
        result.createAlias("personEditors", "personEditors", LEFT_OUTER_JOIN);
        result.createAlias("recipientGroups", "recipientGroups", LEFT_OUTER_JOIN);
        result.createAlias("recipientGroups.members", "recipientGroups.members", LEFT_OUTER_JOIN);
        result.createAlias("roleReaders", "roleReaders", LEFT_OUTER_JOIN);
        result.createAlias("roleEditors", "roleEditors", LEFT_OUTER_JOIN);
        result.createAlias("userAccessLevel", "userAccessLevel", INNER_JOIN);
        result.createAlias("history", "history", LEFT_OUTER_JOIN);
        return result;
    }

    /**
     * Применить ограничения допуска для документов
     * //NOTE  Добавляются алиасы с fetch
     * @param criteria исходный критерий   (AUTH_CRITERIA)
     * @param auth     данные авторизации
     */
    @Override
    public void applyAccessCriteria(DetachedCriteria criteria, AuthorizationData auth) {
        if (!auth.isAdministrator()) {
            final Disjunction disjunction = Restrictions.disjunction();
            final Set<Integer> userIds = auth.getUserIds();
            disjunction.add(Restrictions.in("author.id", userIds));
            disjunction.add(Restrictions.in("executors.id", userIds));
            disjunction.add(Restrictions.in("controller.id", userIds));
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
