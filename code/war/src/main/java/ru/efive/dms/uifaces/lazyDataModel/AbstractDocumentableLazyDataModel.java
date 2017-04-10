package ru.efive.dms.uifaces.lazyDataModel;

import org.primefaces.model.SortOrder;
import org.springframework.transaction.annotation.Transactional;
import ru.entity.model.mapped.DocumentEntity;
import ru.hitsl.sql.dao.interfaces.ViewFactDao;
import ru.hitsl.sql.dao.interfaces.mapped.DocumentDao;
import ru.hitsl.sql.dao.util.AuthorizationData;

import java.util.List;
import java.util.Map;

/**
 * Created by EUpatov on 03.04.2017.
 */
public abstract class AbstractDocumentableLazyDataModel<T extends DocumentEntity> extends AbstractFilterableLazyDataModel<T> {

    private final AuthorizationData authData;
    private final ViewFactDao viewFactDao;

    public AbstractDocumentableLazyDataModel(final DocumentDao<T> dao, final AuthorizationData authData, final ViewFactDao viewFactDao) {
        super(dao);
        this.authData = authData;
        this.viewFactDao  = viewFactDao;
    }

    @Override
    @Transactional("ordTransactionManager")
    public List<T> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> unusedPrimefacesFilters) {
        final DocumentDao documentDao = (DocumentDao) this.dao;
        //Используются фильтры извне, а не из параметров
        if (authData != null) {
            setRowCount(documentDao.countDocumentListByFilters(authData, getFilter(), getFilters(), false, false));
            if (getRowCount() < first) {
                first = 0;
            }
            final List resultList = documentDao.getItems(
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
                viewFactDao.applyViewFlagsOnDocumentList(resultList, authData.getAuthorized());
            }
            return resultList;
        } else {
            log.error("NO AUTH DATA");
            return null;
        }
    }
}
