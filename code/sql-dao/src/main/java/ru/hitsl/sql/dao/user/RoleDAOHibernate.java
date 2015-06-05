package ru.hitsl.sql.dao.user;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import ru.entity.model.enums.RoleType;
import ru.entity.model.user.Role;
import ru.hitsl.sql.dao.GenericDAOHibernate;

import java.util.List;

public class RoleDAOHibernate extends GenericDAOHibernate<Role> {

    @Override
    protected Class<Role> getPersistentClass() {
        return Role.class;
    }

    /**
     * Находит доступные роли
     *
     * @param offset  номер начального элемента списка
     * @param count   количество возвращаемых элементов
     * @param orderBy поле для сортировки списка
     * @param asc     указывает направление сортировки. true = по возрастанию
     * @return коллекция ролей
     */
    public List<Role> findRoles(int offset, int count, String orderBy, boolean asc) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

        if (StringUtils.isNotEmpty(orderBy)) {
            detachedCriteria.addOrder(asc ? Order.asc(orderBy) : Order.desc(orderBy));
        }

        return getHibernateTemplate().findByCriteria(detachedCriteria, offset, count);
    }

    /**
     * Считает количество ролей
     *
     * @return количество ролей
     */
    public long countRoles() {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
        return getCountOf(detachedCriteria);
    }

    /**
     * Поиск ролей по названию
     *
     * @param value название
     * @return список ролей
     */
    @SuppressWarnings("unchecked")
    public List<Role> findByValue(String name) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

        if (StringUtils.isNotEmpty(name)) {
            detachedCriteria.add(Restrictions.eq("name", name));
        }
        return getHibernateTemplate().findByCriteria(detachedCriteria);
    }

    @SuppressWarnings("unchecked")
    public Role findRoleByType(RoleType roleType) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
        if (roleType != null) {
            detachedCriteria.add(Restrictions.eq("roleType", roleType));
        }
        List<Role> in_roles = getHibernateTemplate().findByCriteria(detachedCriteria);
        if(in_roles == null || !in_roles.isEmpty()) {
            return (Role) in_roles.get(0);
        } else {
            return null;
        }
    }


}