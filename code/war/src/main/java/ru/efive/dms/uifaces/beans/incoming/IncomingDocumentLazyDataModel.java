package ru.efive.dms.uifaces.beans.incoming;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.annotations.ViewScopedLazyDataModel;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentableLazyDataModel;
import ru.entity.model.document.IncomingDocument;
import ru.hitsl.sql.dao.interfaces.ViewFactDao;
import ru.hitsl.sql.dao.interfaces.document.IncomingDocumentDao;
import ru.hitsl.sql.dao.util.AuthorizationData;

/**
 * Author: Upatov Egor <br>
 * Date: 23.03.2015, 18:30 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
@ViewScopedLazyDataModel("incomingDocumentLDM")
public class IncomingDocumentLazyDataModel extends AbstractDocumentableLazyDataModel<IncomingDocument> {
    @Autowired
    public IncomingDocumentLazyDataModel(
            @Qualifier("incomingDocumentDao") IncomingDocumentDao incomingDocumentDao,
            @Qualifier("authData") AuthorizationData authData,
            @Qualifier("viewFactDao") ViewFactDao viewFactDao) {
        super(incomingDocumentDao, authData, viewFactDao);
    }
}
