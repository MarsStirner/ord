package ru.efive.dms.uifaces.beans.contragent;

import ru.efive.uifaces.bean.AbstractDocumentListHolderBean;
import ru.entity.model.crm.ContragentType;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.util.List;

/**
 * Author: Upatov Egor <br>
 * Date: 12.02.2015, 15:25 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
@ManagedBean(name = "contragentTypeList")
@ViewScoped
public class ContragentTypeListHolderBean extends AbstractDocumentListHolderBean<ContragentType> {
    @Override
    protected int getTotalCount() {
        return 0;
    }

    @Override
    protected List<ContragentType> loadDocuments() {
        return null;
    }
}
