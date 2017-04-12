package ru.efive.dms.uifaces.beans.internal;

import com.github.javaplugs.jsf.SpringScopeView;
import org.primefaces.model.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import ru.efive.dms.uifaces.beans.annotations.ViewScopedLazyDataModel;
import ru.efive.dms.uifaces.lazyDataModel.AbstractDocumentableLazyDataModel;
import ru.efive.dms.uifaces.lazyDataModel.AbstractFilterableLazyDataModel;
import ru.entity.model.document.InternalDocument;
import ru.hitsl.sql.dao.interfaces.ViewFactDao;
import ru.hitsl.sql.dao.interfaces.document.InternalDocumentDao;
import ru.hitsl.sql.dao.util.AuthorizationData;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import java.util.List;
import java.util.Map;

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
