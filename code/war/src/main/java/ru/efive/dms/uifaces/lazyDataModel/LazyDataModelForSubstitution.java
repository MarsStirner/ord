package ru.efive.dms.uifaces.lazyDataModel;

import com.github.javaplugs.jsf.SpringScopeView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.entity.model.user.Substitution;
import ru.hitsl.sql.dao.interfaces.SubstitutionDao;

import javax.inject.Named;

/**
 * Author: Upatov Egor <br>
 * Date: 17.11.2014, 19:56 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
@Named("substitutions")
@SpringScopeView
public class LazyDataModelForSubstitution extends AbstractFilterableLazyDataModel<Substitution> {
    @Autowired
    public LazyDataModelForSubstitution(@Qualifier("substitutionDao")SubstitutionDao substitutionDao) {
        super(substitutionDao);
    }
}
