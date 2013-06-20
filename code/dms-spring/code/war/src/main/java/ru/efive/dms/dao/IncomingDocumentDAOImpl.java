package ru.efive.dms.dao;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.hibernate.type.StringType;
import org.springframework.orm.hibernate3.HibernateCallback;

import ru.efive.crm.data.Contragent;
import ru.efive.sql.dao.GenericDAOHibernate;
//import ru.efive.sql.dao.user.UserDAOHibernate;
import ru.efive.sql.entity.enums.RoleType;
import ru.efive.sql.entity.user.Group;
import ru.efive.sql.entity.user.Role;
import ru.efive.sql.entity.user.User;
import ru.efive.dms.data.DeliveryType;
import ru.efive.dms.data.DocumentForm;
import ru.efive.dms.data.IncomingDocument;
import ru.efive.dms.data.Numerator;
import ru.efive.dms.data.OfficeKeepingVolume;
import ru.efive.dms.data.OutgoingDocument;
import ru.efive.dms.data.PaperCopyDocument;
import ru.efive.dms.data.RequestDocument;
import ru.efive.dms.util.ApplicationHelper;

public class IncomingDocumentDAOImpl extends GenericDAOHibernate<IncomingDocument> {

	@Override
	protected Class<IncomingDocument> getPersistentClass() {
		return IncomingDocument.class;
	}

	@SuppressWarnings("unchecked")
	public IncomingDocument findDocumentById(String id) {
		DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
		detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

		detachedCriteria.add(Restrictions.eq("id",Integer.valueOf(id)));
		List<IncomingDocument> in_results=getHibernateTemplate().findByCriteria(detachedCriteria);
		if(in_results!=null && in_results.size()>0){
			return in_results.get(0);
		}else{
			return null;	
		}		
	}

	@SuppressWarnings("unchecked")
	public IncomingDocument findDocumentByNumeratorId(String id) {
		DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
		detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

		detachedCriteria.add(Restrictions.eq("parentNumeratorId",Integer.valueOf(id)));
		List<IncomingDocument> in_results=getHibernateTemplate().findByCriteria(detachedCriteria);
		if(in_results!=null && in_results.size()>0){
			return in_results.get(0);
		}else{
			return null;	
		}		
	}


	@SuppressWarnings("unchecked")
	public List<IncomingDocument> findRegistratedDocuments() {
		DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
		detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

		detachedCriteria.add(Restrictions.isNotNull("registrationNumber"));

		return getHibernateTemplate().findByCriteria(detachedCriteria);
	}


	@SuppressWarnings("unchecked")
	public List<IncomingDocument> findRegistratedDocumentsByCriteria(String in_criteria) {
		DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
		detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

		detachedCriteria.add(Restrictions.ilike("registrationNumber","%"+in_criteria+"%"));

		return getHibernateTemplate().findByCriteria(detachedCriteria);
	}

	public List<IncomingDocument> findAllDocumentsByUser(Map<String, Object> in_map, String filter, User user, boolean showDeleted, boolean showDrafts) {
		DetachedCriteria detachedCriteria = getAccessControlSearchCriteriaByUser(user);

		if (!showDeleted) {
			detachedCriteria.add(Restrictions.eq("deleted", false));
		}	

		if (!showDrafts) {
			detachedCriteria.add(Restrictions.not(Restrictions.eq("statusId", 1)));
		}	

		int userId=user.getId();
		if (userId > 0) {	
			System.out.println("subSrart");
			return getHibernateTemplate().findByCriteria(getSearchCriteria(getConjunctionSearchCriteria(detachedCriteria, in_map),filter), -1, 0);			

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

	public long countAllDocuments(Map<String, Object> in_map, boolean showDeleted, boolean showDrafts) {
		DetachedCriteria in_searchCriteria = DetachedCriteria.forClass(getPersistentClass());
		in_searchCriteria .setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

		if (!showDeleted) {
			in_searchCriteria.add(Restrictions.eq("deleted", false));
		}	

		return getCountOf(getConjunctionSearchCriteria(in_searchCriteria, in_map));			
	}	

	public List<IncomingDocument> findAllDocuments(Map<String, Object> in_map, boolean showDeleted, boolean showDrafts, int offset, int count) {
		DetachedCriteria in_searchCriteria = DetachedCriteria.forClass(getPersistentClass());
		in_searchCriteria .setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

		if (!showDeleted) {
			in_searchCriteria.add(Restrictions.eq("deleted", false));
		}	

		return getHibernateTemplate().findByCriteria(getConjunctionSearchCriteria(in_searchCriteria, in_map), offset, count);		
	}

	public List<IncomingDocument> findAllDocuments(Map<String, Object> in_map, String filter, boolean showDeleted, boolean showDrafts) {
		DetachedCriteria detachedCriteria=DetachedCriteria.forClass(getPersistentClass());

		if (!showDeleted) {
			detachedCriteria.add(Restrictions.eq("deleted", false));
		}	

		if (!showDrafts) {
			detachedCriteria.add(Restrictions.not(Restrictions.eq("statusId", 1)));
		}			
		return getHibernateTemplate().findByCriteria(getSearchCriteria(getConjunctionSearchCriteria(detachedCriteria, in_map),filter), -1, 0);			
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

	public List<IncomingDocument> findAllDocumentsByUser(Map<String, Object> in_map, User user, boolean showDeleted, boolean showDrafts, int offset, int count, String orderBy, boolean orderAsc) {
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

	public List<IncomingDocument> findAllDocumentsByUser(String filter, User user, boolean showDeleted, boolean showDrafts) {
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


	public List<IncomingDocument> findAllDocumentsByUser(String filter, User user, boolean showDeleted, boolean showDrafts, int offset, int count, String orderBy, boolean orderAsc) {
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

	@SuppressWarnings("unchecked")
	public List<IncomingDocument> findControlledDocumentsByUser(String filter, User user, boolean showDeleted) {
		int userId=user.getId();
		if (userId > 0) {
			DetachedCriteria in_searchCriteria = getAccessControlSearchCriteriaByUser(user);

			DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
			detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

			detachedCriteria.add(Restrictions.isNotNull("executionDate"));
			List<Integer> ids=new ArrayList<Integer>();
			ids.add(1);
			ids.add(2);
			ids.add(80);			
			detachedCriteria.add(Restrictions.in("statusId",ids));
			String[] ords = "executionDate,id".split(",");
			addOrder(detachedCriteria, ords, true);
			return getHibernateTemplate().findByCriteria(getSearchCriteria(in_searchCriteria, filter));
		}
		else{
			return Collections.emptyList();
		}
	}

	@SuppressWarnings("unchecked")
	public long countControlledDocumentsByUser(String filter, User user, boolean showDeleted) {
		int userId=user.getId();
		if (userId > 0) {
			DetachedCriteria in_searchCriteria = getAccessControlSearchCriteriaByUser(user);

			DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
			detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

			detachedCriteria.add(Restrictions.isNotNull("executionDate"));
			List<Integer> ids=new ArrayList<Integer>();
			ids.add(1);
			ids.add(2);
			ids.add(80);			
			detachedCriteria.add(Restrictions.in("statusId",ids));
			String[] ords = "executionDate,id".split(",");
			addOrder(detachedCriteria, ords, true);
			return getCountOf(getSearchCriteria(in_searchCriteria, filter));
		}else{
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
	public List<IncomingDocument> findDocumentsByAuthor(int userId, boolean showDeleted, int offset, int count, String orderBy, boolean orderAsc) {
		DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
		detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

		if (!showDeleted) {
			detachedCriteria.add(Restrictions.eq("deleted", false));
		}

		if (userId > 0) {
			detachedCriteria.add(Restrictions.eq("author.id", userId));
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
			detachedCriteria.add(Restrictions.eq("author.id", userId));
			return getCountOf(detachedCriteria);
		}
		else {
			return 0;
		}
	}

	@SuppressWarnings("unchecked")
	public List<IncomingDocument> findDraftDocumentsByAuthor(String filter, User user, boolean showDeleted, int offset, int count, String orderBy, boolean orderAsc) {
		DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
		detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

		if (!showDeleted) {
			detachedCriteria.add(Restrictions.eq("deleted", false));
		}

		int userId=user.getId();
		if (userId > 0) {
			detachedCriteria.add(Restrictions.eq("author.id", userId));
			detachedCriteria.add(Restrictions.eq("statusId", 1));

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
			detachedCriteria.add(Restrictions.eq("author.id", userId));
			detachedCriteria.add(Restrictions.eq("statusId", 1));
			return getCountOf(detachedCriteria);
		}
		else {
			return 0;
		}
	}

	/**
	 * Поиск документов по автору
	 *
	 * @param pattern         поисковый запрос
	 * @param userId          идентификатор пользователя
	 * @param showDeleted     true - show deleted, false - hide deleted
	 * @param offset          смещение
	 * @param count           кол-во результатов
	 * @param orderBy         поле для сортировки
	 * @param orderAsc        направление сортировки
	 * @return список документов
	 */
	@SuppressWarnings("unchecked")
	public List<IncomingDocument> findDocumentsByAuthor(String pattern, int userId, boolean showDeleted, int offset, int count, String orderBy, boolean orderAsc) {
		DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
		detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

		if (!showDeleted) {
			detachedCriteria.add(Restrictions.eq("deleted", false));
		}

		if (userId > 0) {
			detachedCriteria.add(Restrictions.eq("author.id", userId));
			String[] ords = orderBy == null ? null : orderBy.split(",");
			if (ords != null) {
				if (ords.length > 1) {
					addOrder(detachedCriteria, ords, orderAsc);
				} else {
					addOrder(detachedCriteria, orderBy, orderAsc);
				}
			}
			return getHibernateTemplate().findByCriteria(getSearchCriteria(detachedCriteria, pattern), offset, count);
		}
		else {
			return Collections.emptyList();
		}
	}

	public long countTemplateDocuments(boolean showDeleted) {
		DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
		detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

		if (!showDeleted) {
			detachedCriteria.add(Restrictions.eq("deleted", false));
		}

		return getCountOf(detachedCriteria);
		
	}

	/**
	 * Кол-во документов по автору
	 *
	 * @param pattern         поисковый запрос
	 * @param userId          идентификатор пользователя
	 * @param showDeleted     true - show deleted, false - hide deleted
	 * @return кол-во результатов
	 */
	public long countDocumentByAuthor(String pattern, int userId, boolean showDeleted) {
		DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
		detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

		if (!showDeleted) {
			detachedCriteria.add(Restrictions.eq("deleted", false));
		}

		if (userId > 0) {
			detachedCriteria.add(Restrictions.eq("author.id", userId));
			return getCountOf(getSearchCriteria(detachedCriteria, pattern));
		}
		else {
			return 0;
		}
	}


	@Override
	protected DetachedCriteria getSearchCriteria(DetachedCriteria criteria, String filter) {	
		//DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
		//detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
		if (StringUtils.isNotEmpty(filter)) {			
			Disjunction disjunction = Restrictions.disjunction();			
			disjunction.add(Restrictions.ilike("registrationNumber", filter, MatchMode.ANYWHERE));
			disjunction.add(Restrictions.sqlRestriction("DATE_FORMAT(deliveryDate, '%d.%m.%Y') like lower(?)", filter + "%", new StringType()));
			disjunction.add(Restrictions.sqlRestriction("DATE_FORMAT(receivedDocumentDate, '%d.%m.%Y') like lower(?)", filter + "%", new StringType()));
			disjunction.add(Restrictions.ilike("receivedDocumentNumber", filter, MatchMode.ANYWHERE));
			disjunction.add(Restrictions.ilike("shortDescription", filter, MatchMode.ANYWHERE));
			//detachedCriteria.createAlias("author", "author", CriteriaSpecification.LEFT_JOIN);
			disjunction.add(Restrictions.ilike("author.lastName", filter, MatchMode.ANYWHERE));
			disjunction.add(Restrictions.ilike("author.middleName", filter, MatchMode.ANYWHERE));
			disjunction.add(Restrictions.ilike("author.firstName", filter, MatchMode.ANYWHERE));
			//detachedCriteria.createAlias("controller", "controller", CriteriaSpecification.LEFT_JOIN);
			disjunction.add(Restrictions.ilike("controller.lastName", filter, MatchMode.ANYWHERE));
			disjunction.add(Restrictions.ilike("controller.middleName", filter, MatchMode.ANYWHERE));
			disjunction.add(Restrictions.ilike("controller.firstName", filter, MatchMode.ANYWHERE));
			//detachedCriteria.createAlias("executors", "executors", CriteriaSpecification.LEFT_JOIN);
			disjunction.add(Restrictions.ilike("executors.lastName", filter, MatchMode.ANYWHERE));
			disjunction.add(Restrictions.ilike("executors.middleName", filter, MatchMode.ANYWHERE));
			disjunction.add(Restrictions.ilike("executors.firstName", filter, MatchMode.ANYWHERE));
			criteria.createAlias("deliveryType", "deliveryType", CriteriaSpecification.LEFT_JOIN);
			disjunction.add(Restrictions.ilike("deliveryType.value", filter, MatchMode.ANYWHERE));
			criteria.createAlias("form", "form", CriteriaSpecification.LEFT_JOIN);
			disjunction.add(Restrictions.ilike("form.value", filter, MatchMode.ANYWHERE));
			criteria.createAlias("contragent", "contragent", CriteriaSpecification.LEFT_JOIN);
			disjunction.add(Restrictions.ilike("contragent.fullName", filter, MatchMode.ANYWHERE));
			disjunction.add(Restrictions.ilike("contragent.shortName", filter, MatchMode.ANYWHERE));
			disjunction.add(Restrictions.ilike("contragent.fullName", filter, MatchMode.ANYWHERE));
			//criteria.createAlias("recipientUsers", "recipientUsers", CriteriaSpecification.LEFT_JOIN);
			//List<String> recipientsName=new ArrayList<String>();
			//recipientsName.add(filter);
			disjunction.add(Restrictions.ilike("recipientUsers.lastName", filter,MatchMode.ANYWHERE));
			disjunction.add(Restrictions.ilike("recipientUsers.firstName", filter,MatchMode.ANYWHERE));
			disjunction.add(Restrictions.ilike("recipientUsers.middleName", filter,MatchMode.ANYWHERE));
			//TODO: поиск по адресатам

			String docType=getPersistentClass().getName();			
			List<Integer> statusIdList=ApplicationHelper.getStatusIdListByStrKey((docType.indexOf(".")>=0?docType.substring(docType.lastIndexOf(".")+1):docType), filter);
			if(statusIdList.size()>0){					
				disjunction.add(Restrictions.in("statusId", statusIdList));				
			}
			
			criteria.add(disjunction);



		}
		return criteria;
		//if(conjunction!=null){
		//return criteria.add(conjunction.add(disjunction));
		//}else{
		//criteria.add(disjunction);
		//return criteria;
		//}

	}

	protected DetachedCriteria getConjunctionSearchCriteria(DetachedCriteria criteria, Map<String, Object> in_map) {
		//DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
		if((in_map!=null)&&(in_map.size()>0)){
			Conjunction conjunction = Restrictions.conjunction();
			String in_key="";		

			in_key="parentNumeratorId";
			if(in_map.get(in_key)!=null){				
				conjunction.add(Restrictions.isNotNull(in_key));
				//conjunction.add(Restrictions.ilike(in_key, in_map.get(in_key).toString(), MatchMode.ANYWHERE));
			}
			
			in_key="registrationNumber";
			if(in_map.get(in_key)!=null){				
				conjunction.add(Restrictions.ilike(in_key, in_map.get(in_key).toString(), MatchMode.ANYWHERE));
			}

			SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");

			in_key="startRegistrationDate";		
			if(in_map.get(in_key)!=null){				
				conjunction.add(Restrictions.ge(in_key.substring(5, 6).toLowerCase()+in_key.substring(6), in_map.get(in_key)));
			}

			in_key="endRegistrationDate";
			if(in_map.get(in_key)!=null){				
				conjunction.add(Restrictions.le(in_key.substring(3, 4).toLowerCase()+in_key.substring(4), in_map.get(in_key)));
			}

			in_key="startCreationDate";		
			if(in_map.get(in_key)!=null){				
				conjunction.add(Restrictions.ge(in_key.substring(5, 6).toLowerCase()+in_key.substring(6), in_map.get(in_key)));
			}

			in_key="endCreationDate";
			if(in_map.get(in_key)!=null){				
				conjunction.add(Restrictions.le(in_key.substring(3, 4).toLowerCase()+in_key.substring(4), in_map.get(in_key)));
			}

			in_key="startDeliveryDate";		
			if(in_map.get(in_key)!=null){								
				conjunction.add(Restrictions.ge(in_key.substring(5, 6).toLowerCase()+in_key.substring(6), in_map.get(in_key)));
			}

			in_key="endDeliveryDate";
			if(in_map.get(in_key)!=null){
				conjunction.add(Restrictions.le(in_key.substring(3, 4).toLowerCase()+in_key.substring(4), in_map.get(in_key)));
			}

			in_key="startReceivedDocumentDate";		
			if(in_map.get(in_key)!=null){								
				conjunction.add(Restrictions.ge(in_key.substring(5, 6).toLowerCase()+in_key.substring(6), in_map.get(in_key)));
			}

			in_key="endReceivedDocumentDate";
			if(in_map.get(in_key)!=null){
				conjunction.add(Restrictions.le(in_key.substring(3, 4).toLowerCase()+in_key.substring(4), in_map.get(in_key)));
			}

			in_key="receivedDocumentNumber";
			if(in_map.get(in_key)!=null && in_map.get(in_key).toString().length()>0){				
				conjunction.add(Restrictions.ilike(in_key, in_map.get(in_key).toString(), MatchMode.ANYWHERE));
			}

			in_key="shortDescription";
			if(in_map.get(in_key)!=null && in_map.get(in_key).toString().length()>0){				
				conjunction.add(Restrictions.ilike(in_key, in_map.get(in_key).toString(), MatchMode.ANYWHERE));
			}

			in_key="statusId";
			if(in_map.get(in_key)!=null && in_map.get(in_key).toString().length()>0){				
				conjunction.add(Restrictions.eq(in_key, Integer.parseInt(in_map.get(in_key).toString())));
			}

			in_key="controller";
			if(in_map.get(in_key)!=null ){
				User controller=(User)in_map.get(in_key);					
				conjunction.add(Restrictions.eq(in_key+".id", controller.getId()));
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
					conjunction.add(Restrictions.in("recipientUsers.id", recipientsId));
				}							
			}

			in_key="author";
			if(in_map.get(in_key)!=null ){
				User author=(User)in_map.get(in_key);								
				conjunction.add(Restrictions.eq(in_key+".id", author.getId()));
			}

			in_key="executors";
			if(in_map.get(in_key)!=null ){
				List<User> executors=(List<User>)in_map.get(in_key);												
				if(executors.size()!=0){
					List<Integer> executorsId=new ArrayList<Integer>();					
					Iterator itr=executors.iterator(); 			
					while(itr.hasNext()) {
						User user=(User)itr.next(); 
						executorsId.add(user.getId());	
					}
					//detachedCriteria.createAlias("executors", "executors", CriteriaSpecification.LEFT_JOIN);
					conjunction.add(Restrictions.in("executors.id", executorsId));
				}							
			}

			in_key="deliveryType";
			//if(in_map.get(in_key)!=null && in_map.get(in_key).toString().length()>0){			
			if(in_map.get(in_key)!=null){
				criteria.createAlias(in_key, in_key, CriteriaSpecification.LEFT_JOIN);					
				conjunction.add(Restrictions.eq(in_key+".id", ((DeliveryType)in_map.get(in_key)).getId()));
			}	

			in_key="startExecutionDate";		
			if(in_map.get(in_key)!=null){								
				conjunction.add(Restrictions.ge(in_key.substring(5, 6).toLowerCase()+in_key.substring(6), in_map.get(in_key)));
			}

			in_key="endExecutionDate";
			if(in_map.get(in_key)!=null){
				conjunction.add(Restrictions.le(in_key.substring(3, 4).toLowerCase()+in_key.substring(4), in_map.get(in_key)));
			}

			in_key="form";
			//if(in_map.get(in_key)!=null && in_map.get(in_key).toString().length()>0){			
			if(in_map.get(in_key)!=null){
				criteria.createAlias(in_key, in_key, CriteriaSpecification.LEFT_JOIN);
				conjunction.add(Restrictions.eq(in_key+".id", ((DocumentForm)in_map.get(in_key)).getId()));
			}

			in_key="templateFlag";					
			if(in_map.get(in_key)!=null){
				criteria.createAlias(in_key, in_key, CriteriaSpecification.LEFT_JOIN);
				conjunction.add(Restrictions.eq(in_key, Boolean.parseBoolean(in_map.get(in_key).toString())));
			}
			
			in_key="contragent";
			if(in_map.get(in_key)!=null ){				
				Contragent contragent=(Contragent)in_map.get(in_key);				
				criteria.createAlias(in_key, in_key, CriteriaSpecification.LEFT_JOIN);
				conjunction.add(Restrictions.eq(in_key+".id", contragent.getId()));				
			}

			in_key="officeKeepingVolume";						
			if(in_map.get(in_key)!=null){
				DetachedCriteria subCriterion=DetachedCriteria.forClass(PaperCopyDocument.class);
				subCriterion.createAlias("officeKeepingVolume","officeKeepingVolume", CriteriaSpecification.LEFT_JOIN);				
				subCriterion.add(Restrictions.eq("officeKeepingVolume.id", ((OfficeKeepingVolume)in_map.get(in_key)).getId()));			
				subCriterion.add(Restrictions.ilike("parentDocumentId", "incoming_", MatchMode.ANYWHERE));
				List<PaperCopyDocument> in_results=getHibernateTemplate().findByCriteria(subCriterion);
				List<Integer> incomingDocumentsId=new ArrayList<Integer>();
				for(PaperCopyDocument paper: in_results){
					String parentId=paper.getParentDocumentId();					
					incomingDocumentsId.add(Integer.parseInt(parentId.substring(9)));
				}
				conjunction.add(Restrictions.in("id",incomingDocumentsId));
			}

			//TODO: поиск по адресатам

			criteria.add(conjunction);
		}
		return criteria;
	}

	protected DetachedCriteria getAccessControlSearchCriteriaByUser(User user){
		DetachedCriteria in_result=null;
		DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
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

			detachedCriteria.createAlias("author", "author", CriteriaSpecification.LEFT_JOIN);
			detachedCriteria.createAlias("executors", "executors", CriteriaSpecification.LEFT_JOIN);
			detachedCriteria.createAlias("controller", "controller", CriteriaSpecification.LEFT_JOIN);
			
			detachedCriteria.createAlias("recipientUsers", "recipientUsers", CriteriaSpecification.LEFT_JOIN);
			detachedCriteria.createAlias("recipientGroups", "recipientGroups", CriteriaSpecification.LEFT_JOIN);
			
			detachedCriteria.createAlias("personReaders", "readers", CriteriaSpecification.LEFT_JOIN);
			detachedCriteria.createAlias("personEditors", "editors", CriteriaSpecification.LEFT_JOIN);
			detachedCriteria.createAlias("roleReaders", "roleReaders", CriteriaSpecification.LEFT_JOIN);
			detachedCriteria.createAlias("roleEditors", "roleEditros", CriteriaSpecification.LEFT_JOIN);

			if(!isAdminRole){				
				disjunction.add(Restrictions.eq("author.id", userId));				
				disjunction.add(Restrictions.eq("executors.id", userId));				
				disjunction.add(Restrictions.eq("controller.id", userId));				
				disjunction.add(Restrictions.eq("recipientUsers.id", userId));				
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

				//disjunction.add(Restrictions.le("userAccessLevel.level", accessLevel));
				//detachedCriteria.setProjection(Projections.groupProperty("id"));

				//DetachedCriteria resultCriteria = DetachedCriteria.forClass(getPersistentClass());
				//resultCriteria.add(Subqueries.propertyIn("id", detachedCriteria));
			}
			int accessLevel=((user.getCurrentUserAccessLevel()!=null)?user.getCurrentUserAccessLevel().getLevel():1);
			detachedCriteria.createAlias("userAccessLevel", "userAccessLevel", CriteriaSpecification.LEFT_JOIN);			
			detachedCriteria.add(Restrictions.conjunction().add(Restrictions.le("userAccessLevel.level", accessLevel)));
			in_result=detachedCriteria;
		}	
		return in_result;

	}

}
