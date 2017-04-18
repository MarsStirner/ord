package ru.efive.dms.uifaces.beans.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.annotations.ViewScopedLazyDataModel;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractFilterableLazyDataModel;
import ru.entity.model.user.Substitution;
import ru.hitsl.sql.dao.interfaces.SubstitutionDao;

/**
 * Author: Upatov Egor <br>
 * Date: 17.11.2014, 19:56 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
@ViewScopedLazyDataModel("substitutionLDM")
public class SubstitutionLazyDataModel extends AbstractFilterableLazyDataModel<Substitution> {
    @Autowired
    public SubstitutionLazyDataModel(@Qualifier("substitutionDao")SubstitutionDao substitutionDao) {
        super(substitutionDao);
    }
}
