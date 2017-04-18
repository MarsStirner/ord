package ru.efive.dms.uifaces.beans.request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.annotations.ViewScopedLazyDataModel;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentableLazyDataModel;
import ru.entity.model.document.RequestDocument;
import ru.hitsl.sql.dao.interfaces.ViewFactDao;
import ru.hitsl.sql.dao.interfaces.document.RequestDocumentDao;
import ru.hitsl.sql.dao.util.AuthorizationData;

/**
 * Author: Upatov Egor <br>
 * Date: 07.04.2015, 14:59 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
@ViewScopedLazyDataModel("requestDocumentLDM")
public class RequestDocumentLazyDataModel extends AbstractDocumentableLazyDataModel<RequestDocument> {
    @Autowired
    public RequestDocumentLazyDataModel(
            @Qualifier("requestDocumentDao") RequestDocumentDao requestDocumentDao,
            @Qualifier("authData") AuthorizationData authData,
            @Qualifier("viewFactDao") ViewFactDao viewFactDao) {
        super(requestDocumentDao, authData, viewFactDao);
    }
}