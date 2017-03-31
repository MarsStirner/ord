package ru.efive.dms.uifaces.lazyDataModel;

import com.github.javaplugs.jsf.SpringScopeView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.entity.model.referenceBook.Nomenclature;
import ru.hitsl.sql.dao.interfaces.referencebook.NomenclatureDao;

/**
 * Author: Upatov Egor <br>
 * Date: 20.04.2015, 18:32 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
@Component("nomenclatureLDM")
@SpringScopeView
public class LazyDataModelForNomenclature extends AbstractFilterableLazyDataModel<Nomenclature> {

    @Autowired
    public LazyDataModelForNomenclature(@Qualifier("nomenclatureDao") NomenclatureDao nomenclatureDao) {
        super(nomenclatureDao);
    }
}
