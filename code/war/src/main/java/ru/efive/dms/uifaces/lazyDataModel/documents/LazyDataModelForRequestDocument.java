package ru.efive.dms.uifaces.lazyDataModel.documents;

import org.primefaces.model.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.efive.dms.dao.RequestDocumentDAOImpl;
import ru.efive.dms.dao.ViewFactDaoImpl;
import ru.efive.dms.uifaces.lazyDataModel.AbstractFilterableLazyDataModel;
import ru.efive.dms.util.security.AuthorizationData;
import ru.entity.model.document.RequestDocument;

import java.util.List;
import java.util.Map;

/**
 * Author: Upatov Egor <br>
 * Date: 07.04.2015, 14:59 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
public class LazyDataModelForRequestDocument extends AbstractFilterableLazyDataModel<RequestDocument> {
    private static final Logger logger = LoggerFactory.getLogger("LAZY_DM_REQUEST");
    // DAO доступа к БД
    private final RequestDocumentDAOImpl dao;
    private final ViewFactDaoImpl viewFactDao;
    //Авторизационные данные пользователя
    private AuthorizationData authData;
    private Map<String, Object> filters;

    /**
     * Создает модель для заданного пользователя
     *
     * @param dao      доступ к БД
     * @param authData данные авторизации по которым будет определяться доступ
     */
    public LazyDataModelForRequestDocument(final RequestDocumentDAOImpl dao, final ViewFactDaoImpl viewFactDao, final AuthorizationData authData) {
        this.dao = dao;
        this.viewFactDao = viewFactDao;
        this.authData = authData;
    }


    @Override
    public List<RequestDocument> load(
           int first, final int pageSize, final String sortField, final SortOrder sortOrder, final Map<String, Object> filters
    ) {
        //Используются фильтры извне, а не из параметров
        if (authData != null) {
            setRowCount(dao.countDocumentListByFilters(authData, getFilter(), getFilters(), false, false));
            if(getRowCount() < first){
                first = 0;
            }
            final List<RequestDocument> resultList = dao.getDocumentListByFilters(
                    authData, getFilter(), this.filters, sortField, SortOrder.ASCENDING.equals(sortOrder), first, pageSize, false, false
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


////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// GET & SET
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    public Map<String, Object> getFilters() {
        return filters;
    }

    public void setFilters(Map<String, Object> filters) {
        this.filters = filters;
    }

}