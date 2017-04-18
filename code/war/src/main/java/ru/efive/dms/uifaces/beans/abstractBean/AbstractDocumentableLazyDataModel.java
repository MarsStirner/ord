package ru.efive.dms.uifaces.beans.abstractBean;

import org.primefaces.model.SortOrder;
import org.springframework.transaction.annotation.Transactional;
import ru.entity.model.mapped.DocumentEntity;
import ru.hitsl.sql.dao.interfaces.ViewFactDao;
import ru.hitsl.sql.dao.interfaces.mapped.DocumentDao;
import ru.hitsl.sql.dao.util.AuthorizationData;

import java.util.List;
import java.util.Map;


public abstract class AbstractDocumentableLazyDataModel<T extends DocumentEntity> extends AbstractFilterableLazyDataModel<T> {

    private final AuthorizationData authData;
    private final ViewFactDao viewFactDao;
    private boolean personalMode = false;

    public AbstractDocumentableLazyDataModel(final DocumentDao<T> dao, final AuthorizationData authData, final ViewFactDao viewFactDao) {
        super(dao);
        this.authData = authData;
        this.viewFactDao = viewFactDao;
    }

    /**
     * Загрузить порцию списка
     *
     * @param first     начальное смещение порции списка
     * @param pageSize  размер старницы выборки
     * @param sortField поле сортировки
     * @param sortOrder порядок сортировки
     * @param filters   - @deprecated Primefaces filters (unsued)
     * @return список документов (страница)
     */
    @Override
    @Transactional("ordTransactionManager")
    @SuppressWarnings("unchecked")
    public List<T> load(
            int first,
            final int pageSize,
            final String sortField,
            final SortOrder sortOrder,
            @Deprecated final  Map<String, Object> filters) {
        final DocumentDao documentDao = (DocumentDao) this.dao;
        //Используются фильтры извне, а не из параметров
        if (personalMode) {
            //Режим работы с собственными проектами
            setRowCount(documentDao.countPersonalDraftDocumentListByFilters(authData, getFilter()));
            if (getRowCount() < first) {
                first = 0;
            }
            return documentDao.getPersonalDraftDocumentListByFilters(
                    authData,
                    getFilter(),
                    sortField,
                    SortOrder.ASCENDING.equals(sortOrder),
                    first,
                    pageSize
            );
        } else {
            //Режим работы с общим списком
            setRowCount(documentDao.countDocumentListByFilters(authData, getFilter(), getFilters(), false, false));
            if (getRowCount() < first) {
                first = 0;
            }
            final List resultList = documentDao.getItems(
                    authData,
                    getFilter(),
                    getFilters(),
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
        }
    }


    public void setPersonalMode() {
        this.personalMode = true;
    }
}
