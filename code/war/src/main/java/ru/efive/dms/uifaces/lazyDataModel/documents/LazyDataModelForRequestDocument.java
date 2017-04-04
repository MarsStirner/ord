package ru.efive.dms.uifaces.lazyDataModel.documents;

import com.github.javaplugs.jsf.SpringScopeView;
import org.primefaces.model.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.efive.dms.uifaces.lazyDataModel.AbstractDocumentableLazyDataModel;
import ru.efive.dms.uifaces.lazyDataModel.AbstractFilterableLazyDataModel;
import ru.entity.model.document.RequestDocument;
import ru.hitsl.sql.dao.interfaces.ViewFactDao;
import ru.hitsl.sql.dao.interfaces.document.RequestDocumentDao;
import ru.hitsl.sql.dao.util.AuthorizationData;

import java.util.List;
import java.util.Map;

/**
 * Author: Upatov Egor <br>
 * Date: 07.04.2015, 14:59 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
@Component("requestDocumentLDM")
@SpringScopeView
public class LazyDataModelForRequestDocument extends AbstractDocumentableLazyDataModel<RequestDocument> {
    private static final Logger logger = LoggerFactory.getLogger("LAZY_DM_REQUEST");

    @Autowired
    public LazyDataModelForRequestDocument(
            @Qualifier("requestDocumentDao") RequestDocumentDao requestDocumentDao,
            @Qualifier("authData") AuthorizationData authData,
            @Qualifier("viewFactDao") ViewFactDao viewFactDao) {
        super(requestDocumentDao, authData, viewFactDao);
    }
}