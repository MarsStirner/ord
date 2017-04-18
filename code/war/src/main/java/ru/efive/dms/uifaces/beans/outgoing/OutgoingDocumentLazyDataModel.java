package ru.efive.dms.uifaces.beans.outgoing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.annotations.ViewScopedLazyDataModel;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentableLazyDataModel;
import ru.entity.model.document.OutgoingDocument;
import ru.hitsl.sql.dao.interfaces.ViewFactDao;
import ru.hitsl.sql.dao.interfaces.document.OutgoingDocumentDao;
import ru.hitsl.sql.dao.util.AuthorizationData;

/**
 * Author: Upatov Egor <br>
 * Date: 02.04.2015, 13:37 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
@ViewScopedLazyDataModel("outgoingDocumentLDM")
public class OutgoingDocumentLazyDataModel extends AbstractDocumentableLazyDataModel<OutgoingDocument> {
    @Autowired
    public OutgoingDocumentLazyDataModel(
            @Qualifier("outgoingDocumentDao") OutgoingDocumentDao outgoingDocumentDao,
            @Qualifier("authData") AuthorizationData authData,
            @Qualifier("viewFactDao") ViewFactDao viewFactDao) {
        super(outgoingDocumentDao, authData, viewFactDao);
    }

}