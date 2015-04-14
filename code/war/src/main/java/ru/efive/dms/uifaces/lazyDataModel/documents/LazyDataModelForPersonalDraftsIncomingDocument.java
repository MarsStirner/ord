package ru.efive.dms.uifaces.lazyDataModel.documents;

import org.primefaces.model.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.efive.dms.dao.IncomingDocumentDAOImpl;
import ru.efive.dms.uifaces.lazyDataModel.AbstractFilterableLazyDataModel;
import ru.efive.dms.util.security.AuthorizationData;
import ru.entity.model.document.IncomingDocument;

import java.util.List;
import java.util.Map;

/**
 * Author: Upatov Egor <br>
 * Date: 01.04.2015, 15:58 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
public class LazyDataModelForPersonalDraftsIncomingDocument extends AbstractFilterableLazyDataModel<IncomingDocument> {
    private static final Logger logger = LoggerFactory.getLogger("LAZY_DM_INCOMING");
    // DAO доступа к БД
    private final IncomingDocumentDAOImpl dao;
    //Авторизационные данные пользователя
    private AuthorizationData authData;

    /**
     * Создает модель для заданного пользователя
     *
     * @param dao      доступ к БД
     * @param authData данные авторизации по которым будет определяться доступ
     */
    public LazyDataModelForPersonalDraftsIncomingDocument(final IncomingDocumentDAOImpl dao, final AuthorizationData authData) {
        this.dao = dao;
        this.authData = authData;
    }

    @Override
    public IncomingDocument getRowData(String rowKey) {
        final Integer identifier;
        try {
            identifier = Integer.valueOf(rowKey);
        } catch (NumberFormatException e) {
            logger.error("Try to get Item by nonInteger identifier \'{}\'. Return NULL", rowKey);
            return null;
        }
        return dao.getItemByIdForListView(identifier);
    }


    @Override
    public List<IncomingDocument> load(
            final int first,
            final int pageSize,
            final String sortField,
            final SortOrder sortOrder,
            final Map<String, Object> filters
    ) {
        //Используются фильтры извне, а не из параметров
        if (authData != null) {
            setRowCount(dao.countPersonalDraftDocumentListByFilters(authData, getFilter()));
            return dao.getPersonalDraftDocumentListByFilters(
                    authData,
                    getFilter(),
                    sortField,
                    SortOrder.ASCENDING.equals(sortOrder),
                    first,
                    pageSize
            );
        } else {
            logger.error("NO AUTH DATA");
            return null;
        }
    }

}