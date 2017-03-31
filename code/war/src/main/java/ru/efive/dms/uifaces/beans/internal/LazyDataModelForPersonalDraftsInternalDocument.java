package ru.efive.dms.uifaces.beans.internal;

import com.github.javaplugs.jsf.SpringScopeView;
import org.primefaces.model.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import ru.efive.dms.uifaces.lazyDataModel.AbstractFilterableLazyDataModel;
import ru.entity.model.document.InternalDocument;
import ru.hitsl.sql.dao.interfaces.document.InternalDocumentDao;
import ru.hitsl.sql.dao.util.AuthorizationData;

import java.util.List;
import java.util.Map;

/**
 * Author: Upatov Egor <br>
 * Date: 03.04.2015, 13:36 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
@Controller("personalInternalDocumentsLDM")
@SpringScopeView
public class LazyDataModelForPersonalDraftsInternalDocument extends AbstractFilterableLazyDataModel<InternalDocument> {
    private static final Logger logger = LoggerFactory.getLogger("LAZY_DM_INTERNAL");

    @Autowired
    @Qualifier("authData")
    private AuthorizationData authData;

    @Autowired
    public LazyDataModelForPersonalDraftsInternalDocument(
            @Qualifier("internalDocumentDao") final InternalDocumentDao internalDocumentDao) {
        super(internalDocumentDao);
    }

    @Override
    public List<InternalDocument> load(
            int first, final int pageSize, final String sortField, final SortOrder sortOrder, final Map<String, Object> filters
    ) {
        final InternalDocumentDao internalDocumentDao = (InternalDocumentDao) this.dao;
        //Используются фильтры извне, а не из параметров
        if (authData != null) {
            setRowCount(internalDocumentDao.countPersonalDraftDocumentListByFilters(authData, filter));
            if (getRowCount() < first) {
                first = 0;
            }
            return internalDocumentDao.getPersonalDraftDocumentListByFilters(
                    authData, filter, sortField, SortOrder.ASCENDING.equals(sortOrder), first, pageSize
            );
        } else {
            logger.error("NO AUTH DATA");
            return null;
        }
    }
}