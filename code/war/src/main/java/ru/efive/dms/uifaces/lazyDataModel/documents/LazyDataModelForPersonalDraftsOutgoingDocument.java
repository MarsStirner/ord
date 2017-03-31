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
import ru.hitsl.sql.dao.interfaces.document.OutgoingDocumentDao;
import ru.hitsl.sql.dao.util.AuthorizationData;

import java.util.List;
import java.util.Map;

/**
 * Author: Upatov Egor <br>
 * Date: 02.04.2015, 14:26 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
@Component("personalOutgoingDocumentsLDM")
@SpringScopeView
public class LazyDataModelForPersonalDraftsOutgoingDocument extends AbstractFilterableLazyDataModel<OutgoingDocument> {
    private static final Logger logger = LoggerFactory.getLogger("LAZY_DM_INCOMING");
    @Autowired
    @Qualifier("authData")
    private AuthorizationData authData;

    /**
     * Создает модель для заданного пользователя
     *
     * @param outgoingDocumentDao доступ к БД
     */
    @Autowired
    public LazyDataModelForPersonalDraftsOutgoingDocument(
            @Qualifier("outgoingDocumentDao") final OutgoingDocumentDao outgoingDocumentDao) {
        super(outgoingDocumentDao);
    }


    @Override
    public List<OutgoingDocument> load(
            int first, final int pageSize, final String sortField, final SortOrder sortOrder, final Map<String, Object> filters
    ) {

        final OutgoingDocumentDao outgoingDocumentDao = (OutgoingDocumentDao) this.dao;
        //Используются фильтры извне, а не из параметров
        if (authData != null) {
            setRowCount(outgoingDocumentDao.countPersonalDraftDocumentListByFilters(authData, filter));
            if (getRowCount() < first) {
                first = 0;
            }
            return outgoingDocumentDao.getPersonalDraftDocumentListByFilters(
                    authData, getFilter(), sortField, SortOrder.ASCENDING.equals(sortOrder), first, pageSize
            );
        } else {
            logger.error("NO AUTH DATA");
            return null;
        }
    }

}