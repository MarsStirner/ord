package ru.efive.dms.uifaces.beans.nomenclature;

/**
 * Author: Upatov Egor <br>
 * Date: 16.02.2015, 13:05 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */

import com.github.javaplugs.jsf.SpringScopeView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentLazyDataModelBean;
import ru.efive.dms.uifaces.lazyDataModel.LazyDataModelForNomenclature;
import ru.entity.model.referenceBook.Nomenclature;

import org.springframework.stereotype.Controller;

@Controller("nomenclatures")
@SpringScopeView
public class NomenclatureListHolderBean extends AbstractDocumentLazyDataModelBean<Nomenclature> {
    @Autowired
    @Qualifier("nomenclatureLDM")
    private LazyDataModelForNomenclature ldm;
}
