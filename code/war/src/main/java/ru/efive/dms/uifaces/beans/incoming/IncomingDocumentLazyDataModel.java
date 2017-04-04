package ru.efive.dms.uifaces.beans.incoming;

import com.github.javaplugs.jsf.SpringScopeView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.efive.dms.uifaces.lazyDataModel.AbstractDocumentableLazyDataModel;
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
@Component("incomingDocumentLDM")
@SpringScopeView
public class IncomingDocumentLazyDataModel extends AbstractDocumentableLazyDataModel<IncomingDocument> {
    private static final Logger logger = LoggerFactory.getLogger("LAZY_DM_INCOMING");

    @Autowired
    public IncomingDocumentLazyDataModel(
            @Qualifier("incomingDocumentDao") IncomingDocumentDao incomingDocumentDao,
            @Qualifier("authData") AuthorizationData authData,
            @Qualifier("viewFactDao") ViewFactDao viewFactDao) {
        super(incomingDocumentDao, authData, viewFactDao);
    }
}
