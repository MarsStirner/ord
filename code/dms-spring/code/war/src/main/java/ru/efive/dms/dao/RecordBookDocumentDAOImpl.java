package ru.efive.dms.dao;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.StringType;

import ru.efive.sql.dao.GenericDAOHibernate;
import ru.efive.dms.data.RecordBookDocument;

public class RecordBookDocumentDAOImpl extends GenericDAOHibernate<RecordBookDocument> {

	@Override
	protected Class<RecordBookDocument> getPersistentClass() {
		return RecordBookDocument.class;
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
	public List<RecordBookDocument> findDocumentsByAuthor(int userId, boolean showDeleted, int offset, int count, String orderBy, boolean orderAsc) {
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
	public List<RecordBookDocument> findDocumentsByAuthor(String pattern, int userId, boolean showDeleted, int offset, int count, String orderBy, boolean orderAsc) {
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
		if (StringUtils.isNotEmpty(filter)) {
			Disjunction disjunction = Restrictions.disjunction();
			disjunction.add(Restrictions.ilike("shortDescription", filter, MatchMode.ANYWHERE));
			disjunction.add(Restrictions.ilike("description", filter, MatchMode.ANYWHERE));
			//disjunction.add(Restrictions.sqlRestriction("DATE_FORMAT(creationDate, '%d.%m.%Y') like lower(?)", filter + "%", new StringType()));
			disjunction.add(Restrictions.sqlRestriction("DATE_FORMAT(plannedDate, '%d.%m.%Y') like lower(?)", filter + "%", new StringType()));
			criteria.createAlias("author", "author", CriteriaSpecification.LEFT_JOIN);
			disjunction.add(Restrictions.ilike("author.lastName", filter, MatchMode.ANYWHERE));
			disjunction.add(Restrictions.ilike("author.middleName", filter, MatchMode.ANYWHERE));
			disjunction.add(Restrictions.ilike("author.firstName", filter, MatchMode.ANYWHERE));
			criteria.add(disjunction);
		}
		return criteria;
	}

}