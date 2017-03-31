package ru.hitsl.sql.dao.interfaces;

import ru.entity.model.user.Substitution;
import ru.entity.model.user.User;
import ru.hitsl.sql.dao.interfaces.mapped.CommonDao;

import java.util.List;

/**
 * Author: Upatov Egor <br>
 * Date: 30.03.2017, 19:33 <br>
 * Company: Bars Group [ www.bars.open.ru ]
 * Description:
 */
public interface SubstitutionDao extends CommonDao<Substitution> {
    List<Substitution> getItemsOnPerson(User person, boolean showDeleted);

    List<Substitution> getCurrentItemsOnPerson(User person, boolean showDeleted);

    List<Substitution> getItemsOnSubstitution(User substitution, boolean showDeleted);

    List<Substitution> getCurrentItemsOnSubstitution(User substitution, boolean showDeleted);
}
