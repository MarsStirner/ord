package ru.efive.dms.uifaces.lazyDataModel;

import com.github.javaplugs.jsf.SpringScopeView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.entity.model.referenceBook.Contragent;
import ru.hitsl.sql.dao.interfaces.referencebook.ContragentDao;

/**
 * Author: Upatov Egor <br>
 * Date: 19.11.2014, 16:33 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
@Component("contragentLDM")
@SpringScopeView
public class LazyDataModelForContragent extends AbstractFilterableLazyDataModel<Contragent> {
    @Autowired
    public LazyDataModelForContragent(@Qualifier("contragentDao") ContragentDao contragentDao) {
        super(contragentDao);
    }
}
