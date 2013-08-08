package ru.efive.dms.dao;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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

import ru.efive.crm.data.Contragent;
import ru.efive.sql.dao.GenericDAOHibernate;
import ru.efive.sql.dao.user.RoleDAOHibernate;
import ru.efive.sql.entity.enums.RoleType;
import ru.efive.sql.entity.user.Role;
import ru.efive.sql.entity.user.User;

import ru.efive.dms.data.DeliveryType;
import ru.efive.dms.data.DocumentForm;
//import ru.efive.dms.data.IncomingDocument;
import ru.efive.dms.data.Task;
import ru.efive.dms.data.Task;

public class TaskDAOImpl extends GenericDAOHibernate<Task> {

	@Override
	protected Class<Task> getPersistentClass() {
		return Task.class;
	}

	public List<Task> findAllDocuments(Map<String, Object> in_map, boolean showDeleted, boolean showDrafts) {
		DetachedCriteria in_searchCriteria = DetachedCriteria.forClass(getPersistentClass());

		if (!showDeleted) {
			in_searchCriteria.add(Restrictions.eq("deleted", false));
		}	

		if (!showDrafts) {
			in_searchCriteria.add(Restrictions.not(Restrictions.eq("statusId", 1)));
		}	

		
		return getHibernateTemplate().findByCriteria(getConjunctionSearchCriteria(in_searchCriteria, in_map));

		
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

	public List<Task> findAllDocumentsByUser(String filter, User user, boolean showDeleted, boolean showDrafts, int offset, int count, String orderBy, boolean orderAsc) {
		DetachedCriteria in_searchCriteria = getAccessControlSearchCriteriaByUser(user);

		if (!showDeleted) {
			in_searchCriteria.add(Restrictions.eq("deleted", false));
		}	

		if (!showDrafts) {
			in_searchCriteria.add(Restrictions.not(Restrictions.eq("statusId", 1)));
		}	

		int userId=user.getId();
		if (userId > 0) {
			/*String[] ords = orderBy == null ? null : orderBy.split(",");
			if (ords != null) {
				if (ords.length > 1) {
					addOrder(in_searchCriteria, ords, orderAsc);
				} else {
					addOrder(in_searchCriteria, orderBy, orderAsc);
				}
			}*/			
			return getHibernateTemplate().findByCriteria(getSearchCriteria(in_searchCriteria, filter), offset, count);

		}
		else {
			return Collections.emptyList();
		}
	}

	public List<Task> findAllDocumentsByUser(Map<String, Object> in_map, String filter, User user, boolean showDeleted, boolean showDrafts) {
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
	
	public List<Task> findAllDocumentsOnExecutionByUser(String filter, User user, boolean showDeleted, int offset, int count, String orderBy, boolean orderAsc) {
		DetachedCriteria in_searchCriteria = getAccessControlSearchCriteriaByUser(user);

		if (!showDeleted) {
			in_searchCriteria.add(Restrictions.eq("deleted", false));
		}	

		//if (!showDrafts) {
		//in_searchCriteria.add(Restrictions.not(Restrictions.eq("statusId", 1)));
		//}	
		in_searchCriteria.add(Restrictions.eq("statusId", 2));
		//in_searchCriteria.add(Restrictions.isNull("executionDate"));
		//in_searchCriteria.add(Restrictions.isNotNull("controlDate"));

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

	public List<Task> findAllExecutedDocumentsByUser(String filter, User user, boolean showDeleted, int offset, int count, String orderBy, boolean orderAsc) {
		DetachedCriteria in_searchCriteria = getAccessControlSearchCriteriaByUser(user);

		if (!showDeleted) {
			in_searchCriteria.add(Restrictions.eq("deleted", false));
		}	

		in_searchCriteria.add(Restrictions.eq("statusId", 3));

		//in_searchCriteria.add(Restrictions.isNotNull("executionDate"));
		//in_searchCriteria.add(Restrictions.isNotNull("controlDate"));

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

	/**
	 * Поиск резолюций
	 * @param parentId - идентификатор родительского документа
	 * @return - список резолюций
	 */
	@SuppressWarnings("unchecked")
	public List<Task> findResolutionsByParent(String parentId) {
		DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
		detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

		if (parentId != null && !parentId.equals("")) {
			detachedCriteria.add(Restrictions.eq("parentId", parentId));
		}

		addOrder(detachedCriteria, "id", true);

		return getHibernateTemplate().findByCriteria(detachedCriteria);
	}

	/**
	 * Поиск резолюций по исполнителю
	 * 
	 * @param executorId - идентификатор пользователя
	 * @param showDeleted     true - show deleted, false - hide deleted
	 * @param offset          смещение
	 * @param count           кол-во результатов
	 * @param orderBy         поле для сортировки
	 * @param orderAsc        направление сортировки
	 * @return - список резолюций
	 */
	@SuppressWarnings("unchecked")
	public List<Task> findResolutionsByExecutor(int executorId, boolean showDeleted, int offset, int count, String orderBy, boolean orderAsc) {
		DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
		detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

		if (!showDeleted) {
			detachedCriteria.add(Restrictions.eq("deleted", false));
		}

		if (executorId > 0) {
			detachedCriteria.add(Restrictions.disjunction().add(Restrictions.eq("author.id", executorId)).add(Restrictions.eq("executor.id", executorId)));
		}

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

	/**
	 * Кол-во резолюций по исполнителю
	 * 
	 * @param executorId - идентификатор пользователя
	 * @param showDeleted     true - show deleted, false - hide deleted
	 * @return кол-во резолюций
	 */
	public long countResolutionsByExecutor(int executorId, boolean showDeleted) {
		DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
		detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

		if (!showDeleted) {
			detachedCriteria.add(Restrictions.eq("deleted", false));
		}

		if (executorId > 0) {
			detachedCriteria.add(Restrictions.disjunction().add(Restrictions.eq("author.id", executorId)).add(Restrictions.eq("executor.id", executorId)));
		}

		return getCountOf(detachedCriteria);
	}

	/**
	 * Поиск резолюций по исполнителю
	 * 
	 * @param filter          поисковый фильтр
	 * @param executorId      идентификатор пользователя
	 * @param showDeleted     true - show deleted, false - hide deleted
	 * @param offset          смещение
	 * @param count           кол-во результатов
	 * @param orderBy         поле для сортировки
	 * @param orderAsc        направление сортировки
	 * @return - список резолюций
	 */
	@SuppressWarnings("unchecked")
	public List<Task> findResolutionsByExecutor(String filter, int executorId, boolean showDeleted, int offset, int count, String orderBy, boolean orderAsc) {
		DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
		detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

		if (!showDeleted) {
			detachedCriteria.add(Restrictions.eq("deleted", false));
		}

		if (executorId > 0) {
			detachedCriteria.add(Restrictions.disjunction().add(Restrictions.eq("author.id", executorId)).add(Restrictions.eq("executor.id", executorId)));
		}

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

	/**
	 * Кол-во резолюций по исполнителю
	 * 
	 * @param filter          поисковый фильтр
	 * @param executorId      идентификатор пользователя
	 * @param showDeleted     true - show deleted, false - hide deleted
	 * @return кол-во резолюций
	 */
	public long countResolutionsByExecutor(String filter, int executorId, boolean showDeleted) {
		DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
		detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

		if (!showDeleted) {
			detachedCriteria.add(Restrictions.eq("deleted", false));
		}

		if (executorId > 0) {
			detachedCriteria.add(Restrictions.disjunction().add(Restrictions.eq("author.id", executorId)).add(Restrictions.eq("executor.id", executorId)));
		}

		return getCountOf(getSearchCriteria(detachedCriteria, filter));
	}

	/**
	 * Поиск резолюций
	 * 
	 * @param executorId - идентификатор пользователя
	 * @param parentId - идентификатор родительского документа
	 * @return - список резолюций
	 */
	@SuppressWarnings("unchecked")
	public List<Task> findResolutionsByParent(int executorId, String parentId) {
		DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
		detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

		if (parentId != null && !parentId.equals("")) {
			detachedCriteria.add(Restrictions.eq("parentId", parentId));
		}

		if (executorId > 0) {
			detachedCriteria.add(Restrictions.disjunction().add(Restrictions.eq("author.id", executorId)).add(Restrictions.eq("executor.id", executorId)));
		}

		addOrder(detachedCriteria, "id", true);

		return getHibernateTemplate().findByCriteria(detachedCriteria);
	}


	@SuppressWarnings("unchecked")
	public List<Task> findDraftDocumentsByAuthor(String filter, User user, boolean showDeleted, int offset, int count, String orderBy, boolean orderAsc) {
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

	@SuppressWarnings("unchecked")
	public List<Task> findAllRegistratedDocuments(String filter, boolean showDeleted, int offset, int count, String orderBy, boolean orderAsc) {
		DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
		detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

		if (!showDeleted) {
			detachedCriteria.add(Restrictions.eq("deleted", false));
		}

		detachedCriteria.add(Restrictions.not(Restrictions.eq("statusId", 1)));

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

	@SuppressWarnings("unchecked")
	public List<Task> findAllRegistratedDocumentsByRootFormat(String key, boolean showDeleted) {
		DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
		detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

		if (!showDeleted) {
			detachedCriteria.add(Restrictions.eq("deleted", false));
		}

		detachedCriteria.add(Restrictions.not(Restrictions.eq("statusId", 1)));
		if(key==null || key.isEmpty() || key.indexOf("task")==-1 ){
			detachedCriteria.add(Restrictions.ilike("rootDocumentId",key+"%"));
		}else{
			detachedCriteria.add(Restrictions.eq("rootDocumentId",key));
		}
		return getHibernateTemplate().findByCriteria(detachedCriteria);					
	}
	
	/**
	 * Кол-во всех зарегистрированных документов по 
	 *
	 * @param userId - идентификатор пользователя
	 * @param showDeleted     true - show deleted, false - hide deleted
	 * @return кол-во результатов
	 */
	public long countAllRegistratedDocuments(String filter, boolean showDeleted) {
		DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
		detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

		if (!showDeleted) {
			detachedCriteria.add(Restrictions.eq("deleted", false));
		}

		detachedCriteria.add(Restrictions.not(Restrictions.eq("statusId", 1)));
		return getCountOf(getSearchCriteria(detachedCriteria,filter));		
	}


	@Override
	protected DetachedCriteria getSearchCriteria(DetachedCriteria criteria, String filter) {
		if (StringUtils.isNotEmpty(filter)) {
			Disjunction disjunction = Restrictions.disjunction();
			disjunction.add(Restrictions.sqlRestriction("DATE_FORMAT(creationDate, '%d.%m.%Y') like lower(?)", filter + "%", new StringType()));
			disjunction.add(Restrictions.sqlRestriction("DATE_FORMAT(controlDate, '%d.%m.%Y') like lower(?)", filter + "%", new StringType()));
			disjunction.add(Restrictions.ilike("shortDescription", filter, MatchMode.ANYWHERE));
			disjunction.add(Restrictions.ilike("taskNumber", filter, MatchMode.ANYWHERE));
			//criteria.createAlias("author", "author", CriteriaSpecification.LEFT_JOIN);
			disjunction.add(Restrictions.ilike("author.lastName", filter, MatchMode.ANYWHERE));
			disjunction.add(Restrictions.ilike("author.middleName", filter, MatchMode.ANYWHERE));
			disjunction.add(Restrictions.ilike("author.firstName", filter, MatchMode.ANYWHERE));
			//criteria.createAlias("executor", "executor", CriteriaSpecification.LEFT_JOIN);
			disjunction.add(Restrictions.ilike("executor.lastName", filter, MatchMode.ANYWHERE));
			disjunction.add(Restrictions.ilike("executor.middleName", filter, MatchMode.ANYWHERE));
			disjunction.add(Restrictions.ilike("executor.firstName", filter, MatchMode.ANYWHERE));
			disjunction.add(Restrictions.ilike("initiator.lastName", filter, MatchMode.ANYWHERE));
			disjunction.add(Restrictions.ilike("initiator.middleName", filter, MatchMode.ANYWHERE));
			disjunction.add(Restrictions.ilike("initiator.firstName", filter, MatchMode.ANYWHERE));
			disjunction.add(Restrictions.ilike("controller.lastName", filter, MatchMode.ANYWHERE));
			disjunction.add(Restrictions.ilike("controller.middleName", filter, MatchMode.ANYWHERE));
			disjunction.add(Restrictions.ilike("controller.firstName", filter, MatchMode.ANYWHERE));
			criteria.add(disjunction);
		}
		return criteria;
	}

	protected DetachedCriteria getAccessControlSearchCriteriaByUser(User user){
		DetachedCriteria in_result=null;
		DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
		//in_result=detachedCriteria;
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
			detachedCriteria.createAlias("executor", "executor", CriteriaSpecification.LEFT_JOIN);
			detachedCriteria.createAlias("initiator", "initiator", CriteriaSpecification.LEFT_JOIN);
			detachedCriteria.createAlias("controller", "controller", CriteriaSpecification.LEFT_JOIN);
			if(!isAdminRole){				
				disjunction.add(Restrictions.eq("author.id", userId));				
				disjunction.add(Restrictions.eq("executor.id", userId));
				disjunction.add(Restrictions.eq("initiator.id", userId));
				disjunction.add(Restrictions.eq("controller.id", userId));
				
				detachedCriteria.add(disjunction);
				/*detachedCriteria.setProjection(Projections.groupProperty("id"));

			DetachedCriteria resultCriteria = DetachedCriteria.forClass(getPersistentClass());
			resultCriteria.add(Subqueries.propertyIn("id", detachedCriteria));*/				
			}			
		}	
		in_result=detachedCriteria;
		return in_result;

	}

	public Task findDocumentById(String id) {
		DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
		detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

		detachedCriteria.add(Restrictions.eq("id",Integer.valueOf(id)));
		List<Task> in_results=getHibernateTemplate().findByCriteria(detachedCriteria);		
		if(in_results!=null && in_results.size()>0){			
			return in_results.get(0);
		}else{			
			return null;	
		}		
	}
	
	protected DetachedCriteria getConjunctionSearchCriteria(DetachedCriteria criteria, Map<String, Object> in_map) {
		if((in_map!=null)&&(in_map.size()>0)){
			Conjunction conjunction = Restrictions.conjunction();
			String in_key="";		

			in_key="taskNumber";
			if(in_map.get(in_key)!=null){				
				conjunction.add(Restrictions.ilike(in_key, in_map.get(in_key).toString(), MatchMode.ANYWHERE));
			}
			
			in_key="rootDocumentId";
			if(in_map.get(in_key)!=null && in_map.get(in_key).toString().length()>0){				
				conjunction.add(Restrictions.eq(in_key, in_map.get(in_key).toString()));
			}

			in_key="taskDocumentId";
			if(in_map.get(in_key)!=null && in_map.get(in_key).toString().length()>0){				
				Disjunction disjunction = Restrictions.disjunction();
				disjunction.add(Restrictions.ilike("rootDocumentId", "task_%"));
				disjunction.add(Restrictions.isEmpty("rootDocumentId"));
				disjunction.add(Restrictions.isNull("rootDocumentId"));				
				conjunction.add(disjunction);
			}
			
			SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");

			in_key="creationDate";
			if(in_map.get(in_key)!=null){				
				conjunction.add(Restrictions.sqlRestriction("DATE_FORMAT("+in_key+", '%d.%m.%Y') like lower(?)", format.format(in_map.get(in_key)) + "%", new StringType()));
			}
	
			if((in_map.get("form")!=null)||(in_map.get("formValue")!=null)||(in_map.get("formCategory")!=null)){				
				criteria.createAlias("form", "form", CriteriaSpecification.LEFT_JOIN);
			}
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
			in_key="formDescription";
			if(in_map.get(in_key)!=null){						
				conjunction.add(Restrictions.ilike("form.description", in_map.get(in_key).toString(), MatchMode.ANYWHERE));
			}
			if((in_map.get("exerciseType")!=null)||(in_map.get("exerciseTypeValue")!=null)||(in_map.get("exerciseTypeCategory")!=null)){				
				criteria.createAlias("exerciseType", "exerciseType", CriteriaSpecification.LEFT_JOIN);
			}
			in_key="exerciseType";
			if(in_map.get(in_key)!=null){							
				conjunction.add(Restrictions.ilike(in_key+".value", ((DocumentForm)in_map.get(in_key)).getValue(), MatchMode.ANYWHERE));
			}
			in_key="exerciseTypeValue";
			if(in_map.get(in_key)!=null){							
				conjunction.add(Restrictions.ilike("exerciseType.value", in_map.get(in_key).toString(), MatchMode.ANYWHERE));
			}			
			in_key="exerciseTypeCategory";
			if(in_map.get(in_key)!=null){						
				conjunction.add(Restrictions.ilike("exerciseType.category", in_map.get(in_key).toString(), MatchMode.ANYWHERE));
			}
			in_key="exerciseTypeDescription";
			if(in_map.get(in_key)!=null){						
				conjunction.add(Restrictions.ilike("exerciseType.description", in_map.get(in_key).toString(), MatchMode.ANYWHERE));
			}
			//TODO: поиск по адресатам

			criteria.add(conjunction);
		}
		return criteria;
	}
}