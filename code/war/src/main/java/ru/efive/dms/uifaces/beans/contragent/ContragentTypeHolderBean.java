package ru.efive.dms.uifaces.beans.contragent;

/**
 * Author: Upatov Egor <br>
 * Date: 12.02.2015, 20:35 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentHolderBean;
import ru.efive.dms.uifaces.beans.annotations.ViewScopedController;
import ru.efive.dms.util.message.MessageUtils;
import ru.entity.model.referenceBook.ContragentType;
import ru.hitsl.sql.dao.interfaces.referencebook.ContragentDao;
import ru.hitsl.sql.dao.interfaces.referencebook.ContragentTypeDao;
import ru.hitsl.sql.dao.util.AuthorizationData;

import static ru.efive.dms.util.message.MessageHolder.MSG_RB_CONTRAGENT_TYPE_IS_USED_BY_SOME_CONTRAGENTS;

@ViewScopedController("contragentType")
public class ContragentTypeHolderBean extends AbstractDocumentHolderBean<ContragentType, ContragentTypeDao> {


    @Autowired
    @Qualifier("contragentDao")
    private ContragentDao contragentDao;


    public ContragentTypeHolderBean(@Qualifier("contragentTypeDao") ContragentTypeDao dao, @Qualifier("authData") AuthorizationData authData) {
        super(dao, authData);
    }

    @Override
    public boolean beforeDelete(ContragentType document, AuthorizationData authData) {
        if (contragentDao.getByType(document).isEmpty()) {
            return true;
        } else {
            MessageUtils.addMessage(MSG_RB_CONTRAGENT_TYPE_IS_USED_BY_SOME_CONTRAGENTS);
            return false;
        }
    }


    @Override
    protected ContragentType newModel(AuthorizationData authData) {
        final ContragentType document = new ContragentType();
        document.setDeleted(false);
        return document;
    }
}
