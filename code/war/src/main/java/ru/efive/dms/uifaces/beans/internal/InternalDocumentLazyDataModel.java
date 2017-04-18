package ru.efive.dms.uifaces.beans.internal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.annotations.ViewScopedLazyDataModel;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentableLazyDataModel;
import ru.entity.model.document.InternalDocument;
import ru.hitsl.sql.dao.interfaces.ViewFactDao;
import ru.hitsl.sql.dao.interfaces.document.InternalDocumentDao;
import ru.hitsl.sql.dao.util.AuthorizationData;

/**
 * Author: Upatov Egor <br>
 * Date: 03.04.2015, 13:06 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */

@ViewScopedLazyDataModel("internalDocumentLDM")
public class InternalDocumentLazyDataModel extends AbstractDocumentableLazyDataModel<InternalDocument> {
    @Autowired
    public InternalDocumentLazyDataModel(
            @Qualifier("internalDocumentDao") final InternalDocumentDao internalDocumentDao,
            @Qualifier("authData") AuthorizationData authData,
            @Qualifier("viewFactDao") ViewFactDao viewFactDao) {
        super(internalDocumentDao, authData, viewFactDao);
    }
}
