package ru.efive.dms.dao;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.*;
import org.hibernate.type.StringType;
import org.joda.time.LocalDate;
import org.slf4j.LoggerFactory;
import ru.efive.dms.util.security.AuthorizationData;
import ru.entity.model.crm.Contragent;
import ru.entity.model.document.*;
import ru.entity.model.enums.DocumentStatus;
import ru.entity.model.enums.DocumentType;

import java.util.*;

import static ru.efive.dms.util.DocumentSearchMapKeys.*;
import static ru.util.ApplicationHelper.getNextDayDate;

@SuppressWarnings("unchecked")
public class IncomingDocumentDAOImpl extends DocumentDAO<IncomingDocument> {

    static {
        logger = LoggerFactory.getLogger("INCOMING_DAO");
    }


    @Override
    protected Class<IncomingDocument> getPersistentClass() {
        return IncomingDocument.class;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // @Deprecated ПОЛНАЯ АХИНЕЯ
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Deprecated
    public List<IncomingDocument> findRegistratedDocumentsByCriteria(String in_criteria) {
        DetachedCriteria detachedCriteria = getSimplestCriteria().add(Restrictions.ilike("registrationNumber", in_criteria, MatchMode.ANYWHERE));
        final LocalDate currentDate = new LocalDate();
        detachedCriteria.add(
                Restrictions.sqlRestriction(
                        "DATE_FORMAT(this_.registrationDate, '%Y') like lower(?)", currentDate.getYear() + "%", new StringType()
                )
        );
        return getHibernateTemplate().findByCriteria(detachedCriteria);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Работа со списками документов
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public List<IncomingDocument> findControlledDocumentsByUser(final String filter, final AuthorizationData authData, final Date controlDate) {
        final DetachedCriteria criteria = getListCriteria();
        applyFilterCriteria(criteria, filter);
        criteria.add(
                Restrictions.in(
                        "statusId", ImmutableList.of(
                                DocumentStatus.ON_REGISTRATION.getId(), DocumentStatus.CHECK_IN_2.getId(), DocumentStatus.ON_EXECUTION_80.getId()
                        )
                )
        );
        addDeletedRestriction(criteria, true);
        if (!authData.isAdministrator()) {
            criteria.add(Restrictions.in("controller.id", authData.getUserIds()));
        }
        criteria.add(Restrictions.isNotNull("executionDate"));
        if(controlDate != null){
           criteria.add(Restrictions.le("executionDate", controlDate));
        }
        criteria.addOrder(Order.desc("executionDate"));
        return getHibernateTemplate().findByCriteria(criteria);
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Критерии для различных вариантов отображения документов
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Получить самый простой критерий для отбора документов, без лишних FETCH
     *
     * @return критерий для документов с DISTINCT
     */
    @Override
    public DetachedCriteria getSimplestCriteria() {
        return DetachedCriteria.forClass(IncomingDocument.class, "this").setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
    }

    /**
     * Получить критерий для отбора Документов и их показа в расширенных списках
     * Автор - INNER
     * Руководитель - LEFT
     * Исполнители - LEFT
     * Тип доставки - LEFT
     * Вид документа - LEFT
     * Корреспондент - LEFT
     * Адресаты  - LEFT
     *
     * @return критерий для документов с DISTINCT
     */
    @Override
    public DetachedCriteria getListCriteria() {
        final DetachedCriteria result = getSimplestCriteria();
        result.createAlias("author", "author", CriteriaSpecification.INNER_JOIN);
        result.createAlias("controller", "controller", CriteriaSpecification.LEFT_JOIN);
        result.createAlias("executors", "executors", CriteriaSpecification.LEFT_JOIN);
        result.createAlias("deliveryType", "deliveryType", CriteriaSpecification.LEFT_JOIN);
        result.createAlias("form", "form", CriteriaSpecification.LEFT_JOIN);
        result.createAlias("contragent", "contragent", CriteriaSpecification.LEFT_JOIN);
        result.createAlias("recipientUsers", "recipientUsers", CriteriaSpecification.LEFT_JOIN);
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
        result.createAlias("personReaders", "personReaders", CriteriaSpecification.LEFT_JOIN);
        result.createAlias("personEditors", "personEditors", CriteriaSpecification.LEFT_JOIN);
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
    public void applyFilterMapCriteria(DetachedCriteria criteria, Map<String, Object> filters) {
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
            } else if ("parentNumeratorId".equals(key)) {
                conjunction.add(Restrictions.isNotNull("parentNumeratorId"));
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
            } else if (START_DELIVERY_DATE_KEY.equals(key)) {
                conjunction.add(Restrictions.ge("deliveryDate", value));
            } else if (END_DELIVERY_DATE_KEY.equals(key)) {
                conjunction.add(Restrictions.le("deliveryDate", getNextDayDate((Date) value)));
            } else if (START_RECEIVED_DATE_KEY.equals(key)) {
                conjunction.add(Restrictions.ge("receivedDate", value));
            } else if (END_RECEIVED_DATE_KEY.equals(key)) {
                conjunction.add(Restrictions.le("receivedDate", getNextDayDate((Date) value)));
            } else if (RECEIVED_DOCUMENT_NUMBER_KEY.equals(key) && StringUtils.isNotEmpty((String) value)) {
                conjunction.add(Restrictions.ilike("receivedDocumentNumber", (String) value, MatchMode.ANYWHERE));
            } else if (SHORT_DESCRIPTION_KEY.equals(key) && StringUtils.isNotEmpty((String) value)) {
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
            } else if (DELIVERY_TYPE_KEY.equals(key)) {
                try {
                    final DeliveryType deliveryType = (DeliveryType) value;
                    conjunction.add(Restrictions.eq("deliveryType.id", deliveryType.getId()));
                } catch (ClassCastException e) {
                    logger.error("Exception while forming FilterMapCriteria: [{}]=\'{}\' IS NOT DeliveryType. Non critical, continue...", key, value);
                }
            } else if (START_EXECUTION_DATE_KEY.equals(key)) {
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
            } else if (CONTRAGENT_KEY.equals(key)) {
                try {
                    final Contragent contragent = (Contragent) value;
                    conjunction.add(Restrictions.eq("contragent.id", contragent.getId()));
                } catch (ClassCastException e) {
                    logger.error("Exception while forming FilterMapCriteria: [{}]=\'{}\' IS NOT Contragent. Non critical, continue...", key, value);
                }
            } else if (OFFICE_KEEPING_VOLUME_KEY.equals(key)) {
                try {
                    final OfficeKeepingVolume volume = (OfficeKeepingVolume) value;
                    final DetachedCriteria subCriterion = DetachedCriteria.forClass(PaperCopyDocument.class);
                    subCriterion.add(Restrictions.eq("officeKeepingVolume.id", volume.getId()));
                    subCriterion.add(Restrictions.ilike("parentDocumentId", "incoming_", MatchMode.ANYWHERE));
                    List<PaperCopyDocument> in_results = getHibernateTemplate().findByCriteria(subCriterion);
                    List<Integer> incomingDocumentsId = new ArrayList<Integer>();
                    for (PaperCopyDocument paper : in_results) {
                        String parentId = paper.getParentDocumentId();
                        incomingDocumentsId.add(Integer.parseInt(parentId.substring(9)));
                    }
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
                    logger.error(
                            "Exception while forming FilterMapCriteria: [{}]=\'{}\' IS NOT OfficeKeepingVolume. Non critical, continue...", key, value
                    );
                }
            } else {
                logger.warn("FilterMapCriteria: Unknown key \'\' (value =\'{}\')", key, value);
            }
        }
        criteria.add(conjunction);
    }

    /**
     * Производит поиск заданной строки в (по условию ИЛИ [дизъюнкция]):
     * регистрационном номере,
     * дате доставки (формат - 'DD.MM.YYYY'),
     * дате получения (формат - 'DD.MM.YYYY'),
     * номере поступившего,
     * кратком описании,
     * ФИО автора,
     * ФИО руководителя,
     * ФИО исполнителей,
     * типе доставки,
     * виде документа,
     * названии корреспондента (FULL или SHORT),
     * ФИО адресатов,
     * наименовании статуса документа
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
        disjunction.add(Restrictions.ilike("registrationNumber", filter, MatchMode.ANYWHERE));
        disjunction.add(
                Restrictions.sqlRestriction(
                        "DATE_FORMAT(deliveryDate, '%d.%m.%Y') like lower(?)", filter + "%", new StringType()
                )
        );
        disjunction.add(
                Restrictions.sqlRestriction(
                        "DATE_FORMAT(receivedDocumentDate, '%d.%m.%Y') like lower(?)", filter + "%", new StringType()
                )
        );
        disjunction.add(Restrictions.ilike("receivedDocumentNumber", filter, MatchMode.ANYWHERE));
        disjunction.add(Restrictions.ilike("shortDescription", filter, MatchMode.ANYWHERE));

        disjunction.add(Restrictions.ilike("author.lastName", filter, MatchMode.ANYWHERE));
        disjunction.add(Restrictions.ilike("author.middleName", filter, MatchMode.ANYWHERE));
        disjunction.add(Restrictions.ilike("author.firstName", filter, MatchMode.ANYWHERE));

        disjunction.add(Restrictions.ilike("controller.lastName", filter, MatchMode.ANYWHERE));
        disjunction.add(Restrictions.ilike("controller.middleName", filter, MatchMode.ANYWHERE));
        disjunction.add(Restrictions.ilike("controller.firstName", filter, MatchMode.ANYWHERE));

        disjunction.add(Restrictions.ilike("executors.lastName", filter, MatchMode.ANYWHERE));
        disjunction.add(Restrictions.ilike("executors.middleName", filter, MatchMode.ANYWHERE));
        disjunction.add(Restrictions.ilike("executors.firstName", filter, MatchMode.ANYWHERE));

        disjunction.add(Restrictions.ilike("deliveryType.value", filter, MatchMode.ANYWHERE));

        disjunction.add(Restrictions.ilike("form.value", filter, MatchMode.ANYWHERE));

        disjunction.add(Restrictions.ilike("contragent.fullName", filter, MatchMode.ANYWHERE));
        disjunction.add(Restrictions.ilike("contragent.shortName", filter, MatchMode.ANYWHERE));

        disjunction.add(Restrictions.ilike("recipientUsers.lastName", filter, MatchMode.ANYWHERE));
        disjunction.add(Restrictions.ilike("recipientUsers.firstName", filter, MatchMode.ANYWHERE));
        disjunction.add(Restrictions.ilike("recipientUsers.middleName", filter, MatchMode.ANYWHERE));

        //TODO справочник в БД
        final List<DocumentStatus> statuses = DocumentType.getIncomingDocumentStatuses();
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
     * //NOTE  Добавляются алиасы с fetch
     *
     * @param criteria исходный критерий   (AUTH_CRITERIA)
     * @param auth     данные авторизации
     */
    public void applyAccessCriteria(DetachedCriteria criteria, AuthorizationData auth) {
        if (!auth.isAdministrator()) {
            final Disjunction disjunction = Restrictions.disjunction();
            final Set<Integer> userIds = auth.getUserIds();
            disjunction.add(Restrictions.in("author.id", userIds));
            disjunction.add(Restrictions.in("executors.id", userIds));
            disjunction.add(Restrictions.in("controller.id", userIds));
            disjunction.add(Restrictions.in("recipientUsers.id", userIds));
            //NOTE  Добавляются алиасы с fetch
            criteria.createAlias("personReaders", "personReaders", CriteriaSpecification.LEFT_JOIN);
            disjunction.add(Restrictions.in("personReaders.id", userIds));
            //NOTE  Добавляются алиасы с fetch
            criteria.createAlias("personEditors", "personEditors", CriteriaSpecification.LEFT_JOIN);
            disjunction.add(Restrictions.in("personEditors.id", userIds));
            if (!auth.getGroups().isEmpty()) {
                //NOTE  Добавляются алиасы с fetch
                criteria.createAlias("recipientGroups", "recipientGroups", CriteriaSpecification.LEFT_JOIN);
                disjunction.add(Restrictions.in("recipientGroups.id", auth.getGroupIds()));
            }
            if (!auth.getRoles().isEmpty()) {
                //NOTE  Добавляются алиасы с fetch
                criteria.createAlias("roleReaders", "roleReaders", CriteriaSpecification.LEFT_JOIN);
                disjunction.add(Restrictions.in("roleReaders.id", auth.getRoleIds()));
                //NOTE  Добавляются алиасы с fetch
                criteria.createAlias("roleEditors", "roleEditors", CriteriaSpecification.LEFT_JOIN);
                disjunction.add(Restrictions.in("roleEditors.id", auth.getRoleIds()));
            }
            criteria.add(disjunction);
        }
    }


    @Override
    public Set<Integer> getDraftStatuses() {
        return ImmutableSet.of(DocumentStatus.DOC_PROJECT_1.getId());
    }


}
