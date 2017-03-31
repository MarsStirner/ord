package ru.efive.dms.uifaces.lazyDataModel.documents;

import com.github.javaplugs.jsf.SpringScopeView;
import org.primefaces.model.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.efive.dms.uifaces.lazyDataModel.AbstractFilterableLazyDataModel;
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
@Component("incomingDocumentLDM")
@SpringScopeView
public class LazyDataModelForIncomingDocument extends AbstractFilterableLazyDataModel<IncomingDocument> {
    private static final Logger logger = LoggerFactory.getLogger("LAZY_DM_INCOMING");

    @Autowired
    @Qualifier("viewFactDao")
    private ViewFactDao viewFactDao;
    @Autowired
    @Qualifier("authData")
    private AuthorizationData authData;

    @Autowired
    public LazyDataModelForIncomingDocument(
            @Qualifier("incomingDocumentDao") IncomingDocumentDao incomingDocumentDao) {
        super(incomingDocumentDao);
    }

    @Override
    public List<IncomingDocument> load(
            int first,
            final int pageSize,
            final String sortField,
            final SortOrder sortOrder,
            final Map<String, Object> filters
    ) {
        final IncomingDocumentDao incomingDocumentDao = (IncomingDocumentDao) this.dao;
        //Используются фильтры извне, а не из параметров
        if (authData != null) {
            setRowCount(incomingDocumentDao.countDocumentListByFilters(authData, getFilter(), getFilters(), false, false));
            if (getRowCount() < first) {
                first = 0;
            }
            final List<IncomingDocument> resultList = incomingDocumentDao.getItems(
                    authData,
                    getFilter(),
                    this.filters,
                    sortField,
                    SortOrder.ASCENDING.equals(sortOrder),
                    first,
                    pageSize,
                    false,
                    false
            );
            //Проверка и выставленние классов просмотра документов пользователем
            if (!resultList.isEmpty()) {
                viewFactDao.applyViewFlagsOnIncomingDocumentList(resultList, authData.getAuthorized());
            }
            return resultList;
        } else {
            logger.error("NO AUTH DATA");
            return null;
        }
    }
}
