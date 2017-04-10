package ru.hitsl.sql.dao.impl.referencebook;

import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.entity.model.enums.RoleType;
import ru.entity.model.referenceBook.Role;
import ru.hitsl.sql.dao.impl.mapped.ReferenceBookDaoImpl;
import ru.hitsl.sql.dao.interfaces.referencebook.RoleDao;

/**
 * Author: Upatov Egor <br>
 * Date: 27.03.2017, 16:30 <br>
 * Company: Bars Group [ www.bars.open.ru ]
 * Description:
 */
@Repository("roleDao")
@Transactional(propagation = Propagation.MANDATORY)
public class RoleDaoImpl extends ReferenceBookDaoImpl<Role> implements RoleDao{
    @Override
    public Class<Role> getEntityClass() {
        return Role.class;
    }

    @Override
    public Role findRoleByType(RoleType roleType) {
        return getFirstItem(getFullCriteria().add(Restrictions.eq("roleType", roleType)));
    }

}
