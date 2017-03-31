package ru.efive.dms.uifaces.lazyDataModel.documents;

import com.github.javaplugs.jsf.SpringScopeView;
import org.primefaces.model.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
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
public class LazyDataModelForRequestDocument extends AbstractFilterableLazyDataModel<RequestDocument> {
    private static final Logger logger = LoggerFactory.getLogger("LAZY_DM_REQUEST");
    @Autowired
    @Qualifier("viewFactDao")
    private ViewFactDao viewFactDao;
    @Autowired
    @Qualifier("authData")
    private AuthorizationData authData;

    @Autowired
    public LazyDataModelForRequestDocument(
            @Qualifier("requestDocumentDao") RequestDocumentDao requestDocumentDao) {
        super(requestDocumentDao);
    }

    @Override
    public List<RequestDocument> load(
            int first, final int pageSize, final String sortField, final SortOrder sortOrder, final Map<String, Object> filters
    ) {
        final RequestDocumentDao requestDocumentDao = (RequestDocumentDao) this.dao;
        //Используются фильтры извне, а не из параметров
        if (authData != null) {
            setRowCount(requestDocumentDao.countDocumentListByFilters(authData, getFilter(), getFilters(), false, false));
            if (getRowCount() < first) {
                first = 0;
            }
            final List<RequestDocument> resultList = requestDocumentDao.getItems(
                    authData, filter, this.filters, sortField, SortOrder.ASCENDING.equals(sortOrder), first, pageSize, false, false
            );
            //Проверка и выставленние классов просмотра документов пользователем
            if (!resultList.isEmpty()) {
                viewFactDao.applyViewFlagsOnRequestDocumentList(resultList, authData.getAuthorized());
            }
            return resultList;
        } else {
            logger.error("NO AUTH DATA");
            return null;
        }
    }


}