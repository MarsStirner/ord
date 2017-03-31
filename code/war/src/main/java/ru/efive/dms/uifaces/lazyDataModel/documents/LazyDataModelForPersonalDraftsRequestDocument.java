package ru.efive.dms.uifaces.lazyDataModel.documents;

import com.github.javaplugs.jsf.SpringScopeView;
import org.primefaces.model.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.lazyDataModel.AbstractFilterableLazyDataModel;
import ru.entity.model.document.RequestDocument;
import ru.hitsl.sql.dao.interfaces.document.RequestDocumentDao;
import ru.hitsl.sql.dao.util.AuthorizationData;

import javax.inject.Named;
import java.util.List;
import java.util.Map;

/**
 * Author: Upatov Egor <br>
 * Date: 07.04.2015, 13:46 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */

@Named("personal_request_documents")
@SpringScopeView
public class LazyDataModelForPersonalDraftsRequestDocument extends AbstractFilterableLazyDataModel<RequestDocument> {
    @Autowired
    @Qualifier("authData")
    private AuthorizationData authData;

    @Autowired
    public LazyDataModelForPersonalDraftsRequestDocument(
            @Qualifier("requestDocumentDao") RequestDocumentDao requestDocumentDao) {
        super(requestDocumentDao);
    }

    @Override
    public List<RequestDocument> load(
            int first, final int pageSize, final String sortField, final SortOrder sortOrder, final Map<String, Object> filters
    ) {
        RequestDocumentDao requestDocumentDao = (RequestDocumentDao) this.dao;
        //Используются фильтры извне, а не из параметров
        if (authData != null) {
            setRowCount(requestDocumentDao.countPersonalDraftDocumentListByFilters(authData, getFilter()));
            if (getRowCount() < first) {
                first = 0;
            }
            return requestDocumentDao.getPersonalDraftDocumentListByFilters(
                    authData, getFilter(), sortField, SortOrder.ASCENDING.equals(sortOrder), first, pageSize
            );
        } else {
            log.error("NO AUTH DATA");
            return null;
        }
    }
}