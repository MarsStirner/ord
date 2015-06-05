package ru.efive.dms.uifaces.lazyDataModel.documents;

import org.primefaces.model.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.efive.dms.uifaces.lazyDataModel.AbstractFilterableLazyDataModel;
import ru.entity.model.document.RecordBookDocument;
import ru.external.AuthorizationData;
import ru.hitsl.sql.dao.RecordBookDocumentDAOImpl;

import java.util.List;
import java.util.Map;

/**
 * Author: Upatov Egor <br>
 * Date: 20.04.2015, 13:43 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
public class LazyDataModelForRecordBookDocument extends AbstractFilterableLazyDataModel<RecordBookDocument> {
    private static final Logger logger = LoggerFactory.getLogger("LAZY_DM_RECORD_BOOK");
    // DAO доступа к БД
    private final RecordBookDocumentDAOImpl dao;
    //Авторизационные данные пользователя
    private AuthorizationData authData;

    /**
     * Создает модель для заданного пользователя
     *
     * @param dao      доступ к БД
     * @param authData данные авторизации по которым будет определяться доступ
     */
    public LazyDataModelForRecordBookDocument(final RecordBookDocumentDAOImpl dao, final AuthorizationData authData) {
        this.dao = dao;
        this.authData = authData;
    }



    @Override
    public List<RecordBookDocument> load(
            int first, final int pageSize, final String sortField, final SortOrder sortOrder, final Map<String, Object> filters
    ) {
        //Используются фильтры извне, а не из параметров
        if (authData != null) {
            setRowCount(dao.countDocuments(authData, getFilter()));
            if(getRowCount() < first){
                first = 0;
            }
            return dao.findDocuments(authData, getFilter(), sortField, SortOrder.ASCENDING.equals(sortOrder), first, pageSize);
        } else {
            logger.error("NO AUTH DATA");
            return null;
        }
    }

    @Override
    public RecordBookDocument getRowData(String rowKey) {
        final Integer identifier;
        try {
            identifier = Integer.valueOf(rowKey);
        } catch (NumberFormatException e) {
            logger.error("Try to get Item by nonInteger identifier \'{}\'. Return NULL", rowKey);
            return null;
        }
        return dao.get(identifier);
    }

}