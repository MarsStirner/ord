package ru.efive.dms.uifaces.lazyDataModel.documents;

import org.primefaces.model.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.efive.dms.dao.RequestDocumentDAOImpl;
import ru.efive.dms.uifaces.lazyDataModel.AbstractFilterableLazyDataModel;
import ru.efive.dms.util.security.AuthorizationData;
import ru.entity.model.document.RequestDocument;

import java.util.List;
import java.util.Map;

/**
 * Author: Upatov Egor <br>
 * Date: 07.04.2015, 13:46 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
public class LazyDataModelForPersonalDraftsRequestDocument  extends AbstractFilterableLazyDataModel<RequestDocument> {
    private static final Logger logger = LoggerFactory.getLogger("LAZY_DM_REQUEST");
    // DAO доступа к БД
    private final RequestDocumentDAOImpl dao;
    //Авторизационные данные пользователя
    private AuthorizationData authData;

    /**
     * Создает модель для заданного пользователя
     *
     * @param dao      доступ к БД
     * @param authData данные авторизации по которым будет определяться доступ
     */
    public LazyDataModelForPersonalDraftsRequestDocument(final RequestDocumentDAOImpl dao, final AuthorizationData authData) {
        this.dao = dao;
        this.authData = authData;
    }

    @Override
    public List<RequestDocument> load(
            int first, final int pageSize, final String sortField, final SortOrder sortOrder, final Map<String, Object> filters
    ) {
        //Используются фильтры извне, а не из параметров
        if (authData != null) {
            setRowCount(dao.countPersonalDraftDocumentListByFilters(authData, getFilter()));
            if(getRowCount() < first){
                first = 0;
            }
            return dao.getPersonalDraftDocumentListByFilters(
                    authData, getFilter(), sortField, SortOrder.ASCENDING.equals(sortOrder), first, pageSize
            );
        } else {
            logger.error("NO AUTH DATA");
            return null;
        }
    }

    @Override
    public RequestDocument getRowData(String rowKey) {
        final Integer identifier;
        try {
            identifier = Integer.valueOf(rowKey);
        } catch (NumberFormatException e) {
            logger.error("Try to get Item by nonInteger identifier \'{}\'. Return NULL", rowKey);
            return null;
        }
        return dao.getItemByIdForListView(identifier);
    }

}