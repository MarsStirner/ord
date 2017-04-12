package ru.efive.dms.uifaces.beans.contragent;

import com.github.javaplugs.jsf.SpringScopeView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentLazyDataModelBean;
import ru.efive.dms.uifaces.lazyDataModel.referencebook.ContragentTypeLazyDataModel;
import ru.entity.model.referenceBook.ContragentType;

import org.springframework.stereotype.Controller;

/**
 * Author: Upatov Egor <br>
 * Date: 12.02.2015, 15:25 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
@Controller("contragentTypeList")
@SpringScopeView
public class ContragentTypeListHolderBean extends AbstractDocumentLazyDataModelBean<ContragentType> {

    @Autowired
    @Qualifier("contragentTypeLDM")
    private ContragentTypeLazyDataModel lazyModel;

}