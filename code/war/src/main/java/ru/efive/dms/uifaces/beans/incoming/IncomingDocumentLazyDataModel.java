package ru.efive.dms.uifaces.beans.incoming;

import com.github.javaplugs.jsf.SpringScopeView;
import org.primefaces.model.SelectableDataModel;
import org.primefaces.model.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import ru.efive.dms.uifaces.beans.annotations.ViewScopedLazyDataModel;
import ru.efive.dms.uifaces.lazyDataModel.AbstractDocumentableLazyDataModel;
import ru.entity.model.document.IncomingDocument;
import ru.hitsl.sql.dao.interfaces.ViewFactDao;
import ru.hitsl.sql.dao.interfaces.document.IncomingDocumentDao;
import ru.hitsl.sql.dao.util.AuthorizationData;

import java.util.List;
import java.util.Map;

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
