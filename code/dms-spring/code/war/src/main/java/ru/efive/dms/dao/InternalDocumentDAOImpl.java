package ru.efive.dms.dao;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.hibernate.type.StringType;

import ru.efive.sql.dao.GenericDAOHibernate;
import ru.efive.sql.entity.enums.RoleType;
import ru.efive.sql.entity.user.Group;
import ru.efive.sql.entity.user.Role;
import ru.efive.sql.entity.user.User;
import ru.efive.dms.data.DocumentForm;
import ru.efive.dms.data.IncomingDocument;
import ru.efive.dms.data.OfficeKeepingVolume;
import ru.efive.dms.data.OutgoingDocument;
//import ru.efive.dms.data.IncomingDocument;
import ru.efive.dms.data.InternalDocument;
import ru.efive.dms.util.ApplicationHelper;

public class InternalDocumentDAOImpl extends GenericDAOHibernate<InternalDocument> {

	@Override
	protected Class<InternalDocument> getPersistentClass() {
		return InternalDocument.class;
	}

	public long countAllDocuments(Map<String, Object> in_map, boolean showDeleted, boolean showDrafts) {
		DetachedCriteria in_searchCriteria = DetachedCriteria.forClass(getPersistentClass());
		in_searchCriteria .setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

		if (!showDeleted) {
			in_searchCriteria.add(Restrictions.eq("deleted", false));
		}

		return getCountOf(getConjunctionSearchCriteria(in_searchCriteria, in_map));
	}

	public List<InternalDocument> findAllDocuments(Map<String, Object> in_map, boolean showDeleted, boolean showDrafts, int offset, int count) {
		DetachedCriteria in_searchCriteria = DetachedCriteria.forClass(getPersistentClass());
		in_searchCriteria .setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

		if (!showDeleted) {
			in_searchCriteria.add(Restrictions.eq("deleted", false));
		}

		return getHibernateTemplate().findByCriteria(getConjunctionSearchCriteria(in_searchCriteria, in_map), offset, count);
	}

	public List<InternalDocument> findAllDocumentsByUser(Map<String, Object> in_map, String filter, User user, boolean showDeleted, boolean showDrafts, int offset, int count, String orderBy, boolean orderAsc) {
		DetachedCriteria in_searchCriteria = getAccessControlSearchCriteriaByUser(user);

		if (!showDeleted) {
			in_searchCriteria.add(Restrictions.eq("deleted", false));
		}

		if (!showDrafts) {
			in_searchCriteria.add(Restrictions.not(Restrictions.eq("statusId", 1)));
		}

		int userId=user.getId();
		if (userId > 0) {
			String[] ords = orderBy == null ? null : orderBy.split(",");
			if (ords != null) {
				if (ords.length > 1) {
					addOrder(in_searchCriteria, ords, orderAsc);
				} else {
					addOrder(in_searchCriteria, orderBy, orderAsc);
				}
			}
			return getHibernateTemplate().findByCriteria(getSearchCriteria(getConjunctionSearchCriteria(in_searchCriteria, in_map),filter), offset, count);

		}
		else {
			return Collections.emptyList();
		}
	}

	public List<InternalDocument> findAllDocumentsByUser(Map<String, Object> in_map, String filter, User user, boolean showDeleted, boolean showDrafts) {
		DetachedCriteria in_searchCriteria = getAccessControlSearchCriteriaByUser(user);

		if (!showDeleted) {
			in_searchCriteria.add(Restrictions.eq("deleted", false));
		}

		if (!showDrafts) {
			in_searchCriteria.add(Restrictions.not(Restrictions.eq("statusId", 1)));
		}

		int userId=user.getId();
		if (userId > 0) {
			return getHibernateTemplate().findByCriteria(getSearchCriteria(getConjunctionSearchCriteria(in_searchCriteria, in_map),filter));
		}
		else {
			return Collections.emptyList();
		}
	}


	public long countAllDocumentsByUser(Map<String, Object> in_map, String filter, User user, boolean showDeleted, boolean showDrafts) {
		DetachedCriteria in_searchCriteria = getAccessControlSearchCriteriaByUser(user);

		if (!showDeleted) {
			in_searchCriteria.add(Restrictions.eq("deleted", false));
		}

		if (!showDrafts) {
			in_searchCriteria.add(Restrictions.not(Restrictions.eq("statusId", 1)));
		}

		int userId=user.getId();
		if (userId > 0) {
			return getCountOf(getSearchCriteria(getConjunctionSearchCriteria(in_searchCriteria, in_map),filter));
		}
		else {
			return 0;
		}
	}

	public List<InternalDocument> findAllDocumentsByUser(Map<String, Object> in_map, User user, boolean showDeleted, boolean showDrafts, int offset, int count, String orderBy, boolean orderAsc) {
		DetachedCriteria in_searchCriteria = getAccessControlSearchCriteriaByUser(user);

		if (!showDeleted) {
			in_searchCriteria.add(Restrictions.eq("deleted", false));
		}

		if (!showDrafts) {
			in_searchCriteria.add(Restrictions.not(Restrictions.eq("statusId", 1)));
		}

		int userId=user.getId();
		if (userId > 0) {
			String[] ords = orderBy == null ? null : orderBy.split(",");
			if (ords != null) {
				if (ords.length > 1) {
					addOrder(in_searchCriteria, ords, orderAsc);
				} else {
					addOrder(in_searchCriteria, orderBy, orderAsc);
				}
			}
			return getHibernateTemplate().findByCriteria(getConjunctionSearchCriteria(in_searchCriteria, in_map), offset, count);

		}
		else {
			return Collections.emptyList();
		}
	}

	public long countAllDocumentsByUser(String filter, User user, boolean showDeleted,boolean showDrafts) {
		DetachedCriteria in_searchCriteria = getAccessControlSearchCriteriaByUser(user);

		if (!showDeleted) {
			in_searchCriteria.add(Restrictions.eq("deleted", false));
		}

		if (!showDrafts) {
			in_searchCriteria.add(Restrictions.not(Restrictions.eq("statusId", 1)));
		}

		int userId=user.getId();
		if (userId > 0) {
			return getCountOf(getSearchCriteria(in_searchCriteria, filter));
		}
		else {
			return 0;
		}
	}

	public List<InternalDocument> findAllDocumentsByUser(String filter, User user, boolean showDeleted, boolean showDrafts) {
		DetachedCriteria in_searchCriteria = getAccessControlSearchCriteriaByUser(user);

		if (!showDeleted) {
			in_searchCriteria.add(Restrictions.eq("deleted", false));
		}

		if (!showDrafts) {
			in_searchCriteria.add(Restrictions.not(Restrictions.eq("statusId", 1)));
		}

		int userId=user.getId();
		if (userId > 0) {
			return getHibernateTemplate().findByCriteria(getSearchCriteria(in_searchCriteria, filter));

		}
		else {
			return Collections.emptyList();
		}

	}

	public List<InternalDocument> findAllDocumentsByUser(String filter, User user, boolean showDeleted, boolean showDrafts, int offset, int count, String orderBy, boolean orderAsc) {
		DetachedCriteria in_searchCriteria = getAccessControlSearchCriteriaByUser(user);

		if (!showDeleted) {
			in_searchCriteria.add(Restrictions.eq("deleted", false));
		}

		if (!showDrafts) {
			in_searchCriteria.add(Restrictions.not(Restrictions.eq("statusId", 1)));
		}

		int userId=user.getId();
		if (userId > 0) {
			String[] ords = orderBy == null ? null : orderBy.split(",");
			if (ords != null) {
				if (ords.length > 1) {
					addOrder(in_searchCriteria, ords, orderAsc);
				} else {
					addOrder(in_searchCriteria, orderBy, orderAsc);
				}
			}
			return getHibernateTemplate().findByCriteria(getSearchCriteria(in_searchCriteria, filter), offset, count);

		}
		else {
			return Collections.emptyList();
		}
	}

	public long countAllDocumentsByUser(Map<String, Object> in_map, User user, boolean showDeleted, boolean showDrafts) {
		DetachedCriteria in_searchCriteria = getAccessControlSearchCriteriaByUser(user);

		if (!showDeleted) {
			in_searchCriteria.add(Restrictions.eq("deleted", false));
		}

		if (!showDrafts) {
			in_searchCriteria.add(Restrictions.not(Restrictions.eq("statusId", 1)));
		}

		int userId=user.getId();
		if (userId > 0) {
			return getCountOf(getConjunctionSearchCriteria(in_searchCriteria, in_map));
		}
		else {
			return 0;
		}
	}

	/**
	 * Поиск документов по автору
	 *
	 * @param userId - идентификатор пользователя
	 * @param showDeleted     true - show deleted, false - hide deleted
	 * @param offset          смещение
	 * @param count           кол-во результатов
	 * @param orderBy         поле для сортировки
	 * @param orderAsc        направление сортировки
	 * @return список документов
	 */
	@SuppressWarnings("unchecked")
	public List<InternalDocument> findDocumentsByAuthor(int userId, boolean showDeleted, int offset, int count, String orderBy, boolean orderAsc) {
		DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
		detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

		if (!showDeleted) {
			detachedCriteria.add(Restrictions.eq("deleted", false));
		}

		if (userId > 0) {
			detachedCriteria.add(Restrictions.eq("initiator.id", userId));
			String[] ords = orderBy == null ? null : orderBy.split(",");
			if (ords != null) {
				if (ords.length > 1) {
					addOrder(detachedCriteria, ords, orderAsc);
				} else {
					addOrder(detachedCriteria, orderBy, orderAsc);
				}
			}
			return getHibernateTemplate().findByCriteria(detachedCriteria, offset, count);
		}
		else {
			return Collections.emptyList();
		}
	}

	/**
	 * Кол-во документов по автору
	 *
	 * @param userId - идентификатор пользователя
	 * @param showDeleted     true - show deleted, false - hide deleted
	 * @return кол-во результатов
	 */
	public long countDocumentByAuthor(int userId, boolean showDeleted) {
		DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
		detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

		if (!showDeleted) {
			detachedCriteria.add(Restrictions.eq("deleted", false));
		}

		if (userId > 0) {
			detachedCriteria.add(Restrictions.eq("initiator.id", userId));
			return getCountOf(detachedCriteria);
		}
		else {
			return 0;
		}
	}

	/**
	 * Поиск документов по автору
	 *
	 * @param filter          фильтр поиска
	 * @param userId          идентификатор пользователя
	 * @param showDeleted     true - show deleted, false - hide deleted
	 * @param offset          смещение
	 * @param count           кол-во результатов
	 * @param orderBy         поле для сортировки
	 * @param orderAsc        направление сортировки
	 * @return список документов
	 */
	@SuppressWarnings("unchecked")
	public List<InternalDocument> findDocumentsByAuthor(String filter, int userId, boolean showDeleted, int offset, int count, String orderBy, boolean orderAsc) {
		DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
		detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

		if (!showDeleted) {
			detachedCriteria.add(Restrictions.eq("deleted", false));
		}

		if (userId > 0) {
			detachedCriteria.add(Restrictions.eq("initiator.id", userId));
			String[] ords = orderBy == null ? null : orderBy.split(",");
			if (ords != null) {
				if (ords.length > 1) {
					addOrder(detachedCriteria, ords, orderAsc);
				} else {
					addOrder(detachedCriteria, orderBy, orderAsc);
				}
			}
			return getHibernateTemplate().findByCriteria(getSearchCriteria(detachedCriteria, filter), offset, count);
		}
		else {
			return Collections.emptyList();
		}
	}

	/**
	 * Кол-во документов по автору
	 *
	 * @param filter          фильтр поиска
	 * @param userId          идентификатор пользователя
	 * @param showDeleted     true - show deleted, false - hide deleted
	 * @return кол-во результатов
	 */
	public long countDocumentByAuthor(String filter, int userId, boolean showDeleted) {
		DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
		detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

		if (!showDeleted) {
			detachedCriteria.add(Restrictions.eq("deleted", false));
		}

		if (userId > 0) {
			detachedCriteria.add(Restrictions.eq("initiator.id", userId));
			return getCountOf(getSearchCriteria(detachedCriteria, filter));
		}
		else {
			return 0;
		}
	}

	@SuppressWarnings("unchecked")
	public List<InternalDocument> findDraftDocumentsByAuthor(String filter, User user, boolean showDeleted, int offset, int count, String orderBy, boolean orderAsc) {
		DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
		detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

		if (!showDeleted) {
			detachedCriteria.add(Restrictions.eq("deleted", false));
		}

		int userId=user.getId();
		if (userId > 0) {
			detachedCriteria.add(Restrictions.eq("initiator.id", userId));
			List<Integer> statuses=new ArrayList<Integer>();
			statuses.add(1);
			statuses.add(150);

			detachedCriteria.add(Restrictions.in("statusId", statuses));

			String[] ords = orderBy == null ? null : orderBy.split(",");
			if (ords != null) {
				if (ords.length > 1) {
					addOrder(detachedCriteria, ords, orderAsc);
				} else {
					addOrder(detachedCriteria, orderBy, orderAsc);
				}
			}
			return getHibernateTemplate().findByCriteria(getSearchCriteria(detachedCriteria,filter), offset, count);
		}
		else {
			return Collections.emptyList();
		}
	}

	/**
	 * Кол-во документов по автору
	 *
	 * @param userId - идентификатор пользователя
	 * @param showDeleted     true - show deleted, false - hide deleted
	 * @return кол-во результатов
	 */
	public long countDraftDocumentsByAuthor(User user, boolean showDeleted) {
		DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
		detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

		if (!showDeleted) {
			detachedCriteria.add(Restrictions.eq("deleted", false));
		}

		int userId=user.getId();
		if (userId > 0) {
			detachedCriteria.add(Restrictions.eq("initiator.id", userId));
			detachedCriteria.add(Restrictions.eq("statusId", 1));
			return getCountOf(detachedCriteria);
		}
		else {
			return 0;
		}
	}

	@Override
	protected DetachedCriteria getSearchCriteria(DetachedCriteria criteria, String filter) {
		if (StringUtils.isNotEmpty(filter)) {			
			Disjunction disjunction = Restrictions.disjunction();
			disjunction.add(Restrictions.sqlRestriction("DATE_FORMAT(creationDate, '%d.%m.%Y') like lower(?)", filter + "%", new StringType()));
			disjunction.add(Restrictions.sqlRestriction("DATE_FORMAT(executionDate, '%d.%m.%Y') like lower(?)", filter + "%", new StringType()));
			disjunction.add(Restrictions.ilike("shortDescription", filter, MatchMode.ANYWHERE));
			//criteria.createAlias("initiator", "initiator", CriteriaSpecification.LEFT_JOIN);
			disjunction.add(Restrictions.ilike("initiator.lastName", filter, MatchMode.ANYWHERE));
			disjunction.add(Restrictions.ilike("initiator.middleName", filter, MatchMode.ANYWHERE));
			disjunction.add(Restrictions.ilike("initiator.firstName", filter, MatchMode.ANYWHERE));
			//criteria.createAlias("controller", "controller", CriteriaSpecification.LEFT_JOIN);
			disjunction.add(Restrictions.ilike("signer.lastName", filter, MatchMode.ANYWHERE));
			disjunction.add(Restrictions.ilike("signer.middleName", filter, MatchMode.ANYWHERE));
			disjunction.add(Restrictions.ilike("signer.firstName", filter, MatchMode.ANYWHERE));
			//criteria.createAlias("form", "form", CriteriaSpecification.LEFT_JOIN);
			disjunction.add(Restrictions.ilike("form.value", filter, MatchMode.ANYWHERE));
			disjunction.add(Restrictions.ilike("registrationNumber", filter, MatchMode.ANYWHERE));

			String docType=getPersistentClass().getName();			
			List<Integer> statusIdList=ApplicationHelper.getStatusIdListByStrKey((docType.indexOf(".")>=0?docType.substring(docType.lastIndexOf(".")+1):docType), filter);
			if(statusIdList.size()>0){					
				disjunction.add(Restrictions.in("statusId", statusIdList));				
			}
			//TODO: поиск по адресатам

			criteria.add(disjunction);
		}
		return criteria;
	}

	@SuppressWarnings("unchecked")
	public List<InternalDocument> findRegistratedDocumentsByCriteria(String in_criteria) {
		DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
		detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

		detachedCriteria.add(Restrictions.ilike("registrationNumber","%"+in_criteria+"%"));

		return getHibernateTemplate().findByCriteria(detachedCriteria);
	}

	@SuppressWarnings("unchecked")
	public List<InternalDocument> findDocumentsByCriteria(Map<String, Object> in_map, boolean showDeleted, boolean showDrafts) {
		DetachedCriteria in_searchCriteria = getInitiateCriteriaForPersistentClass();

		if (!showDeleted) {
			in_searchCriteria.add(Restrictions.eq("deleted", false));
		}

		if (!showDrafts) {
			in_searchCriteria.add(Restrictions.not(Restrictions.eq("statusId", 1)));
		}

		return getHibernateTemplate().findByCriteria(getConjunctionSearchCriteria(in_searchCriteria, in_map));
	}

	@SuppressWarnings("unchecked")
	public List<InternalDocument> findRegistratedDocumentsByForm(String in_criteria) {
		DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
		detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

		detachedCriteria.add(Restrictions.isNotNull("registrationNumber"));
		detachedCriteria.createAlias("form", "form", CriteriaSpecification.LEFT_JOIN);
		detachedCriteria.add(Restrictions.eq("form.value",in_criteria));

		return getHibernateTemplate().findByCriteria(detachedCriteria);
	}

	protected DetachedCriteria getConjunctionSearchCriteria(DetachedCriteria criteria, Map<String, Object> in_map) {
		if((in_map!=null)&&(in_map.size()>0)){
			Conjunction conjunction = Restrictions.conjunction();
			String in_key="";
			in_key="registrationNumber";
			if(in_map.get(in_key)!=null){
				conjunction.add(Restrictions.isNotNull(in_key));
				conjunction.add(Restrictions.ilike(in_key, in_map.get(in_key).toString(), MatchMode.ANYWHERE));
			}

			SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");

			in_key="startRegistrationDate";
			if(in_map.get(in_key)!=null){
				//conjunction.add(Restrictions.sqlRestriction("DATE_FORMAT("+in_key.charAt(5)+in_key.substring(6)+", '%d.%m.%Y') >= lower(?)", format.format(in_map.get(in_key)) + "%", new StringType()));
				conjunction.add(Restrictions.ge(in_key.substring(5, 6).toLowerCase()+in_key.substring(6), in_map.get(in_key)));
			}

			in_key="endRegistrationDate";
			if(in_map.get(in_key)!=null){
				//conjunction.add(Restrictions.sqlRestriction("DATE_FORMAT("+in_key.charAt(3)+in_key.substring(4)+", '%d.%m.%Y') <= lower(?)", format.format(in_map.get(in_key)) + "%", new StringType()));
				conjunction.add(Restrictions.le(in_key.substring(3, 4).toLowerCase()+in_key.substring(4), in_map.get(in_key)));
			}

			in_key="startCreationDate";
			if(in_map.get(in_key)!=null){
				//conjunction.add(Restrictions.sqlRestriction("DATE_FORMAT("+in_key.charAt(5)+in_key.substring(6)+", '%d.%m.%Y') >= lower(?)", format.format(in_map.get(in_key)) + "%", new StringType()));
				conjunction.add(Restrictions.ge(in_key.substring(5, 6).toLowerCase()+in_key.substring(6), in_map.get(in_key)));
			}

			in_key="endCreationDate";
			if(in_map.get(in_key)!=null){
				//conjunction.add(Restrictions.sqlRestriction("DATE_FORMAT("+in_key.charAt(3)+in_key.substring(4)+", '%d.%m.%Y') <= lower(?)", format.format(in_map.get(in_key)) + "%", new StringType()));
				conjunction.add(Restrictions.le(in_key.substring(3, 4).toLowerCase()+in_key.substring(4), in_map.get(in_key)));
			}

			in_key="startSignatureDate";
			if(in_map.get(in_key)!=null){
				//conjunction.add(Restrictions.sqlRestriction("DATE_FORMAT("+in_key.charAt(5)+in_key.substring(6)+", '%d.%m.%Y') >= lower(?)", format.format(in_map.get(in_key)) + "%", new StringType()));
				conjunction.add(Restrictions.ge(in_key.substring(5, 6).toLowerCase()+in_key.substring(6), in_map.get(in_key)));
			}

			in_key="endSignatureDate";
			if(in_map.get(in_key)!=null){
				//conjunction.add(Restrictions.sqlRestriction("DATE_FORMAT("+in_key.charAt(3)+in_key.substring(4)+", '%d.%m.%Y') <= lower(?)", format.format(in_map.get(in_key)) + "%", new StringType()));
				conjunction.add(Restrictions.le(in_key.substring(3, 4).toLowerCase()+in_key.substring(4), in_map.get(in_key)));
			}

			in_key="shortDescription";
			if(in_map.get(in_key)!=null && in_map.get(in_key).toString().length()>0){
				conjunction.add(Restrictions.ilike(in_key, in_map.get(in_key).toString(), MatchMode.ANYWHERE));
			}

			in_key="controller";
			if(in_map.get(in_key)!=null ){
				User controller=(User)in_map.get(in_key);
				//criteria.createAlias(in_key, in_key, CriteriaSpecification.LEFT_JOIN);
				conjunction.add(Restrictions.ilike(in_key+".lastName", controller.getLastName(), MatchMode.ANYWHERE));
				conjunction.add(Restrictions.ilike(in_key+".middleName", controller.getMiddleName(), MatchMode.ANYWHERE));
				conjunction.add(Restrictions.ilike(in_key+".firstName", controller.getFirstName(), MatchMode.ANYWHERE));
			}

			in_key="recipientUsers";
			if(in_map.get(in_key)!=null ){
				List<User> recipients=(List<User>)in_map.get(in_key);
				if(recipients.size()!=0){
					List<Integer> recipientsId=new ArrayList<Integer>();
					Iterator itr=recipients.iterator();
					while(itr.hasNext()) {
						User user=(User)itr.next();
						recipientsId.add(user.getId());
					}
					//criteria.createAlias("recipientUsers", "recipients", CriteriaSpecification.LEFT_JOIN);
					conjunction.add(Restrictions.in("recipientUsers.id", recipientsId));
				}
			}

			in_key="statusId";
			if(in_map.get(in_key)!=null && in_map.get(in_key).toString().length()>0){
				conjunction.add(Restrictions.eq(in_key, Integer.parseInt(in_map.get(in_key).toString())));
			}

			in_key="initiator";
			if(in_map.get(in_key)!=null ){
				User author=(User)in_map.get(in_key);
				//criteria.createAlias(in_key, in_key, CriteriaSpecification.LEFT_JOIN);
				conjunction.add(Restrictions.ilike(in_key+".lastName", author.getLastName(), MatchMode.ANYWHERE));
				conjunction.add(Restrictions.ilike(in_key+".middleName", author.getMiddleName(), MatchMode.ANYWHERE));
				conjunction.add(Restrictions.ilike(in_key+".firstName", author.getFirstName(), MatchMode.ANYWHERE));
			}

			in_key="responsible";
			if(in_map.get(in_key)!=null ){
				User executor=(User)in_map.get(in_key);
				//criteria.createAlias(in_key, in_key, CriteriaSpecification.LEFT_JOIN);
				conjunction.add(Restrictions.ilike(in_key+".lastName", executor.getLastName(), MatchMode.ANYWHERE));
				conjunction.add(Restrictions.ilike(in_key+".middleName", executor.getMiddleName(), MatchMode.ANYWHERE));
				conjunction.add(Restrictions.ilike(in_key+".firstName", executor.getFirstName(), MatchMode.ANYWHERE));
			}

			in_key="startExecutionDate";
			if(in_map.get(in_key)!=null){
				//conjunction.add(Restrictions.sqlRestriction("DATE_FORMAT("+in_key.charAt(5)+in_key.substring(6)+", '%d.%m.%Y') >= lower(?)", format.format(in_map.get(in_key)) + "%", new StringType()));
				conjunction.add(Restrictions.ge(in_key.substring(5, 6).toLowerCase()+in_key.substring(6), in_map.get(in_key)));
			}

			in_key="endExecutionDate";
			if(in_map.get(in_key)!=null){
				//conjunction.add(Restrictions.sqlRestriction("DATE_FORMAT("+in_key.charAt(3)+in_key.substring(4)+", '%d.%m.%Y') <= lower(?)", format.format(in_map.get(in_key)) + "%", new StringType()));
				conjunction.add(Restrictions.le(in_key.substring(3, 4).toLowerCase()+in_key.substring(4), in_map.get(in_key)));
			}

			//if((in_map.get("form")!=null)||(in_map.get("formValue")!=null)||(in_map.get("formCategory")!=null)){
				//criteria.createAlias("form", "form", CriteriaSpecification.LEFT_JOIN);
			//}
			in_key="form";
			if(in_map.get(in_key)!=null){
				conjunction.add(Restrictions.ilike(in_key+".value", ((DocumentForm)in_map.get(in_key)).getValue(), MatchMode.ANYWHERE));
			}
			in_key="formValue";
			if(in_map.get(in_key)!=null){
				conjunction.add(Restrictions.ilike("form.value", in_map.get(in_key).toString(), MatchMode.ANYWHERE));
			}
			in_key="formCategory";
			if(in_map.get(in_key)!=null){
				conjunction.add(Restrictions.ilike("form.category", in_map.get(in_key).toString(), MatchMode.ANYWHERE));
			}

			in_key="officeKeepingVolume";
			//if(in_map.get(in_key)!=null && in_map.get(in_key).toString().length()>0){
			if(in_map.get(in_key)!=null){
				criteria.createAlias(in_key, in_key, CriteriaSpecification.LEFT_JOIN);
				conjunction.add(Restrictions.eq(in_key+".id", ((OfficeKeepingVolume)in_map.get(in_key)).getId()));
			}

			//TODO: поиск по адресатам

			criteria.add(conjunction);
		}
		return criteria;
	}

	protected DetachedCriteria getInitiateCriteriaForPersistentClass(){
		DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());

		detachedCriteria.createAlias("form", "form", CriteriaSpecification.LEFT_JOIN);

		detachedCriteria.createAlias("initiator", "initiator", CriteriaSpecification.LEFT_JOIN);
		detachedCriteria.createAlias("recipientUsers", "recipients", CriteriaSpecification.LEFT_JOIN);
		detachedCriteria.createAlias("signer", "signer", CriteriaSpecification.LEFT_JOIN);
		detachedCriteria.createAlias("responsible", "responsible", CriteriaSpecification.LEFT_JOIN);
		detachedCriteria.createAlias("controller", "controller", CriteriaSpecification.LEFT_JOIN);

		detachedCriteria.createAlias("personEditors", "editors", CriteriaSpecification.LEFT_JOIN);
		detachedCriteria.createAlias("personReaders", "readers", CriteriaSpecification.LEFT_JOIN);

		detachedCriteria.createAlias("recipientGroups", "recipientGroups", CriteriaSpecification.LEFT_JOIN);
		detachedCriteria.createAlias("roleReaders", "roleReaders", CriteriaSpecification.LEFT_JOIN);
		detachedCriteria.createAlias("roleEditors", "roleEditros", CriteriaSpecification.LEFT_JOIN);

		return detachedCriteria;
	}

	protected DetachedCriteria getAccessControlSearchCriteriaByUser(User user){
		DetachedCriteria in_result=null;
		DetachedCriteria detachedCriteria = getInitiateCriteriaForPersistentClass();//DetachedCriteria.forClass(getPersistentClass());
		//in_result=detachedCriteria;
		//if(true){return in_result;}
		Disjunction disjunction = Restrictions.disjunction();

		int userId=user.getId();
		if (userId > 0) {
			boolean isAdminRole=false;
			List<Role> in_roles=user.getRoleList();
			if(in_roles!=null){
				for(Role in_role:in_roles){
					if(in_role.getRoleType().equals(RoleType.ADMINISTRATOR)){
						isAdminRole=true;
						break;
					}
				}
			}

			if(!isAdminRole){
				disjunction.add(Restrictions.eq("initiator.id", userId));
				disjunction.add(Restrictions.eq("signer.id", userId));
				disjunction.add(Restrictions.eq("controller.id", userId));
				disjunction.add(Restrictions.eq("recipients.id", userId));
				disjunction.add(Restrictions.eq("readers.id", userId));
				disjunction.add(Restrictions.eq("editors.id", userId));
				List<Integer> rolesId=new ArrayList<Integer>();
				List<Role> roles=user.getRoleList();
				if(roles.size()!=0){
					Iterator itr=roles.iterator();
					while(itr.hasNext()) {
						Role role=(Role)itr.next();
						rolesId.add(role.getId());
					}
					disjunction.add(Restrictions.in("roleReaders.id", rolesId));
					disjunction.add(Restrictions.in("roleEditros.id", rolesId));
				}

				List<Integer> recipientGroupsId=new ArrayList<Integer>();
				Set<Group> recipientGroups=user.getGroups();
				if(recipientGroups.size()!=0){
					Iterator itr=recipientGroups.iterator();
					while(itr.hasNext()) {
						Group group=(Group)itr.next();
						recipientGroupsId.add(group.getId());
					}
					disjunction.add(Restrictions.in("recipientGroups.id", recipientGroupsId));

				}
				detachedCriteria.add(disjunction);
				//detachedCriteria.setProjection(Projections.groupProperty("id"));

				//DetachedCriteria resultCriteria = DetachedCriteria.forClass(getPersistentClass());
				//resultCriteria.add(Subqueries.propertyIn("id", detachedCriteria));
			}
			int accessLevel=((user.getCurrentUserAccessLevel()!=null)?user.getCurrentUserAccessLevel().getLevel():1);
			detachedCriteria.createAlias("userAccessLevel", "userAccessLevel", CriteriaSpecification.LEFT_JOIN);
			detachedCriteria.add(Restrictions.conjunction().add(Restrictions.le("userAccessLevel.level", accessLevel)));
			in_result=detachedCriteria;//resultCriteria;
		}
		return in_result;

	}

	@SuppressWarnings("unchecked")
	public InternalDocument findDocumentById(String id) {
		DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
		detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

		detachedCriteria.add(Restrictions.eq("id",Integer.valueOf(id)));
		List<InternalDocument> in_results=getHibernateTemplate().findByCriteria(detachedCriteria);
		if(in_results!=null && in_results.size()>0){
			return in_results.get(0);
		}else{
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public InternalDocument findDocumentByNumeratorId(String id) {
		DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
		detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

		detachedCriteria.add(Restrictions.eq("parentNumeratorid",Integer.valueOf(id)));
		List<InternalDocument> in_results=getHibernateTemplate().findByCriteria(detachedCriteria);
		if(in_results!=null && in_results.size()>0){
			return in_results.get(0);
		}else{
			return null;
		}
	}


}