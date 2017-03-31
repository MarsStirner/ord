package ru.efive.dms.uifaces.lazyDataModel.documents;

import com.github.javaplugs.jsf.SpringScopeView;
import org.primefaces.model.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
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
public class LazyDataModelForOutgoingDocument extends AbstractFilterableLazyDataModel<OutgoingDocument> {
    private static final Logger logger = LoggerFactory.getLogger("LAZY_DM_OUTGOING");

    @Autowired
    @Qualifier("viewFactDao")
    private ViewFactDao viewFactDao;
    @Autowired
    @Qualifier("authData")
    private AuthorizationData authData;


    @Autowired
    public LazyDataModelForOutgoingDocument(
            @Qualifier("outgoingDocumentDao") OutgoingDocumentDao outgoingDocumentDao) {
        super(outgoingDocumentDao);
    }

    @Override
    public List<OutgoingDocument> load(
            int first,
            final int pageSize,
            final String sortField,
            final SortOrder sortOrder,
            final Map<String, Object> filters
    ) {
        final OutgoingDocumentDao dao = (OutgoingDocumentDao) this.dao;
        //Используются фильтры извне, а не из параметров
        if (authData != null) {
            setRowCount(dao.countDocumentListByFilters(authData, filter, filters, false, false));
            if (getRowCount() < first) {
                first = 0;
            }
            final List<OutgoingDocument> resultList = dao.getItems(
                    authData,
                    filter,
                    filters,
                    sortField,
                    SortOrder.ASCENDING.equals(sortOrder),
                    first,
                    pageSize,
                    false,
                    false
            );
            //Проверка и выставленние классов просмотра документов пользователем
            if (!resultList.isEmpty()) {
                viewFactDao.applyViewFlagsOnOutgoingDocumentList(resultList, authData.getAuthorized());
            }

            return resultList;
        } else {
            logger.error("NO AUTH DATA");
            return null;
        }
    }

}