package ru.hitsl.sql.dao.impl.document;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.criterion.*;
import org.hibernate.type.StringType;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.entity.model.document.IncomingDocument;
import ru.entity.model.document.OfficeKeepingVolume;
import ru.entity.model.enums.DocumentStatus;
import ru.entity.model.enums.DocumentType;
import ru.entity.model.referenceBook.Contragent;
import ru.entity.model.referenceBook.DeliveryType;
import ru.entity.model.referenceBook.DocumentForm;
import ru.hitsl.sql.dao.impl.mapped.DocumentDaoImpl;
import ru.hitsl.sql.dao.interfaces.document.IncomingDocumentDao;
import ru.hitsl.sql.dao.util.AuthorizationData;
import ru.hitsl.sql.dao.util.DocumentSearchMapKeys;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hibernate.sql.JoinType.LEFT_OUTER_JOIN;
import static org.hibernate.sql.JoinType.INNER_JOIN;



@Repository("incomingDocumentDao")
@Transactional(propagation = Propagation.MANDATORY)
public class IncomingDocumentDaoImpl extends DocumentDaoImpl<IncomingDocument> implements IncomingDocumentDao{


    @Override
    public Class<IncomingDocument> getEntityClass() {
        return IncomingDocument.class;
    }

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

    @Override
    public List<IncomingDocument> findControlledDocumentsByUser(final String filter, final AuthorizationData authData, final LocalDateTime controlDate) {
        final DetachedCriteria criteria = getListCriteria();
        applyFilter(criteria, filter);
        criteria.add(
                Restrictions.in(
                        "statusId", Stream.of(
                                DocumentStatus.ON_REGISTRATION.getId(), DocumentStatus.CHECK_IN_2.getId(), DocumentStatus.ON_EXECUTION_80.getId()
                        ).collect(Collectors.toSet())
                )
        );
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

    /**
     * Получить критерий для отбора Документов и их показа в расширенных списках
     * Исполнители - LEFT
     * Вид документа - LEFT
     * Корреспондент - LEFT
     *
     * @return критерий для документов с DISTINCT
     */
    @Override
    public DetachedCriteria getListCriteria() {
        final DetachedCriteria result = getSimpleCriteria();
        result.createAlias("author", "author", INNER_JOIN);
        result.createAlias("controller", "controller", LEFT_OUTER_JOIN);
        result.createAlias("executors", "executors", LEFT_OUTER_JOIN);
        result.createAlias("recipientUsers", "recipientUsers", LEFT_OUTER_JOIN);
        result.createAlias("form", "form", LEFT_OUTER_JOIN);
        result.createAlias("contragent", "contragent", LEFT_OUTER_JOIN);
        return result;
    }

    /**
     * Получить критерий для подгрузки из БД всех связанных сущностей
     *
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


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Работа с критериями (общая)
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Применитиь к текущим критериям ограничения сложного фильтра
     *
     * @param criteria текущий критерий, в который будут добавлены условия  (НЕ менее LIST_CRITERIA)
     * @param filters  сложный фильтр (карта)
     */
    @Override
    public void applyFilter(DetachedCriteria criteria, Map<String, Object> filters) {
        if (filters == null || filters.isEmpty()) {
            return;
        }
        final Conjunction conjunction = Restrictions.conjunction();
        for (Map.Entry<String, Object> entry : filters.entrySet()) {
            final String key = entry.getKey();
            final Object value = entry.getValue();

            if (StringUtils.isEmpty(key)) {
                // Пропустить запись с пустым ключом
                log.warn("FilterMapCriteria: empty key for \'{}\'", value);
            } else if ("parentNumeratorId".equals(key)) {
                conjunction.add(Restrictions.isNotNull("parentNumeratorId"));
            } else if (DocumentSearchMapKeys.REGISTRATION_NUMBER_KEY.equals(key)) {
                conjunction.add(Restrictions.ilike("registrationNumber", (String) value, MatchMode.ANYWHERE));
            } else if (DocumentSearchMapKeys.START_REGISTRATION_DATE_KEY.equals(key)) {
                conjunction.add(Restrictions.ge("registrationDate", value));
            } else if (DocumentSearchMapKeys.END_REGISTRATION_DATE_KEY.equals(key)) {
                conjunction.add(Restrictions.le("registrationDate",((LocalDateTime) value).plusDays(1)));
            } else if (DocumentSearchMapKeys.START_CREATION_DATE_KEY.equals(key)) {
                conjunction.add(Restrictions.ge("creationDate", value));
            } else if (DocumentSearchMapKeys.END_CREATION_DATE_KEY.equals(key)) {
                conjunction.add(Restrictions.le("creationDate", ((LocalDateTime) value).plusDays(1)));
            } else if (DocumentSearchMapKeys.START_DELIVERY_DATE_KEY.equals(key)) {
                conjunction.add(Restrictions.ge("deliveryDate", value));
            } else if (DocumentSearchMapKeys.END_DELIVERY_DATE_KEY.equals(key)) {
                conjunction.add(Restrictions.le("deliveryDate", ((LocalDateTime) value).plusDays(1)));
            } else if (DocumentSearchMapKeys.START_RECEIVED_DATE_KEY.equals(key)) {
                conjunction.add(Restrictions.ge("receivedDocumentDate", value));
            } else if (DocumentSearchMapKeys.END_RECEIVED_DATE_KEY.equals(key)) {
                conjunction.add(Restrictions.le("receivedDocumentDate", ((LocalDateTime) value).plusDays(1)));
            } else if (DocumentSearchMapKeys.RECEIVED_DOCUMENT_NUMBER_KEY.equals(key) && StringUtils.isNotEmpty((String) value)) {
                conjunction.add(Restrictions.ilike("receivedDocumentNumber", (String) value, MatchMode.ANYWHERE));
            } else if (DocumentSearchMapKeys.SHORT_DESCRIPTION_KEY.equals(key) && StringUtils.isNotEmpty((String) value)) {
                conjunction.add(Restrictions.ilike("shortDescription", (String) value, MatchMode.ANYWHERE));
            } else if (DocumentSearchMapKeys.STATUS_KEY.equals(key) && StringUtils.isNotEmpty((String) value)) {
                conjunction.add(Restrictions.eq("statusId", Integer.valueOf((String) value)));
            } else if (DocumentSearchMapKeys.CONTROLLER_KEY.equals(key)) {
                createUserEqRestriction(conjunction, "controller.id", value);
            } else if (DocumentSearchMapKeys.RECIPIENTS_KEY.equals(key)) {
                createUserListInRestriction(conjunction, "recipientUsers.id", value);
            } else if (DocumentSearchMapKeys.AUTHOR_KEY.equals(key)) {
                createUserEqRestriction(conjunction, "author.id", value);
            } else if (DocumentSearchMapKeys.AUTHORS_KEY.equals(key)) {
                createUserListInRestriction(conjunction, "author.id", value);
            } else if (DocumentSearchMapKeys.EXECUTORS_KEY.equals(key)) {
                createUserListInRestriction(conjunction, "executors.id", value);
            } else if (DocumentSearchMapKeys.DELIVERY_TYPE_KEY.equals(key)) {
                try {
                    final DeliveryType deliveryType = (DeliveryType) value;
                    // Добавляем JOIN в запрос
                    criteria.createAlias("deliveryType", "deliveryType", LEFT_OUTER_JOIN);
                    conjunction.add(Restrictions.eq("deliveryType.id", deliveryType.getId()));
                } catch (ClassCastException e) {
                    log.error("Exception while forming FilterMapCriteria: [{}]=\'{}\' IS NOT DeliveryType. Non critical, continue...", key, value);
                }
            } else if (DocumentSearchMapKeys.START_EXECUTION_DATE_KEY.equals(key)) {
                conjunction.add(Restrictions.ge("executionDate", value));
            } else if (DocumentSearchMapKeys.END_EXECUTION_DATE_KEY.equals(key)) {
                conjunction.add(Restrictions.le("executionDate", ((LocalDateTime) value).plusDays(1)));
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
            } else if (DocumentSearchMapKeys.OFFICE_KEEPING_VOLUME_KEY.equals(key)) {
                try {
                    final OfficeKeepingVolume volume = (OfficeKeepingVolume) value;
                    List<Integer> incomingDocumentsId = new ArrayList<>();
                    // http://stackoverflow.com/questions/13004142/hibernate-restriction-in-causes-an-error-if-the-list
                    // -is-empty
                    //короче: если список пустой - то будет синтаксическая ошибка в сформированном SQL,
                    // поэтому так как в данном месте мы строи конъюнцию(И), то просто добавим невыполнимое условие
                    //TODO дождаться пачта hibernate
                    if (incomingDocumentsId.isEmpty()) {
                        conjunction.add(Restrictions.eq("id", -1));
                    } else {
                        conjunction.add(Restrictions.in("id", incomingDocumentsId));
                    }
                } catch (ClassCastException e) {
                    log.error(
                            "Exception while forming FilterMapCriteria: [{}]=\'{}\' IS NOT OfficeKeepingVolume. Non critical, continue...", key, value
                    );
                }
            } else {
                log.warn("FilterMapCriteria: Unknown key \'{}\' (value =\'{}\')", key, value);
            }
        }
        criteria.add(conjunction);
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
        final List<DocumentStatus> statuses = DocumentType.getIncomingDocumentStatuses();
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
     * //NOTE  Добавляются алиасы с fetch
     *
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


    @Override
    public Set<Integer> getDraftStatuses() {
        return Stream.of(DocumentStatus.DOC_PROJECT_1.getId()).collect(Collectors.toSet());
    }


}
