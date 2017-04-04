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
import ru.entity.model.document.OutgoingDocument;
import ru.hitsl.sql.dao.interfaces.ViewFactDao;
import ru.hitsl.sql.dao.interfaces.document.OutgoingDocumentDao;
import ru.hitsl.sql.dao.util.AuthorizationData;

import java.util.List;
import java.util.Map;

/**
 * Author: Upatov Egor <br>
 * Date: 02.04.2015, 13:37 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
@Component("outgoingDocumentLDM")
@SpringScopeView
public class LazyDataModelForOutgoingDocument extends AbstractDocumentableLazyDataModel<OutgoingDocument> {
    @Autowired
    public LazyDataModelForOutgoingDocument(
            @Qualifier("outgoingDocumentDao") OutgoingDocumentDao outgoingDocumentDao,
            @Qualifier("authData") AuthorizationData authData,
            @Qualifier("viewFactDao") ViewFactDao viewFactDao) {
        super(outgoingDocumentDao, authData, viewFactDao);
    }

}