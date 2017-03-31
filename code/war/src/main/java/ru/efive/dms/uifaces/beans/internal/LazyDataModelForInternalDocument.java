package ru.efive.dms.uifaces.beans.internal;

import com.github.javaplugs.jsf.SpringScopeView;
import org.primefaces.model.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import ru.efive.dms.uifaces.lazyDataModel.AbstractFilterableLazyDataModel;
import ru.entity.model.document.InternalDocument;
import ru.hitsl.sql.dao.interfaces.ViewFactDao;
import ru.hitsl.sql.dao.interfaces.document.InternalDocumentDao;
import ru.hitsl.sql.dao.util.AuthorizationData;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import java.util.List;
import java.util.Map;

/**
 * Author: Upatov Egor <br>
 * Date: 03.04.2015, 13:06 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */

@Controller("internal_documents")
@SpringScopeView
public class LazyDataModelForInternalDocument extends AbstractFilterableLazyDataModel<InternalDocument> {
    private static final Logger logger = LoggerFactory.getLogger("LAZY_DM_INTERNAL");

    @Autowired
    @Qualifier("viewFactDao")
    private ViewFactDao viewFactDao;
    @Autowired
    @Qualifier("authData")
    private AuthorizationData authData;

    @Autowired
    public LazyDataModelForInternalDocument(
            @Qualifier("internalDocumentDao") final InternalDocumentDao internalDocumentDao
    ) {
        super(internalDocumentDao);
    }

    /**
     * При каждом запросе страницы (нового view) инициализировать список фильтров
     */
    @PostConstruct
    public void initInternalDocumentList() {
        if (!FacesContext.getCurrentInstance().isPostback()) {
            final Map<String, String> parameterMap = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
            if (!parameterMap.isEmpty()) {
                logger.info("List initialize with {} params", parameterMap.size());
                filters.clear();
                for (Map.Entry<String, String> entry : parameterMap.entrySet()) {
                    logger.info("{} = {}", entry.getKey(), entry.getValue());
                    filters.put(entry.getKey(), entry.getValue());
                }
            } else {
                filters.put("registrationNumber", "%");
            }
        }
    }

    @Override
    public List<InternalDocument> load(
            int first,
            final int pageSize,
            final String sortField,
            final SortOrder sortOrder,
            final Map<String, Object> filters
    ) {

        InternalDocumentDao internalDocumentDao = (InternalDocumentDao) this.dao;
        //Используются фильтры извне, а не из параметров
        if (authData != null) {
            setRowCount(internalDocumentDao.countItems(authData, getFilter(), getFilters(), false, false));
            if (getRowCount() < first) {
                first = 0;
            }
            final List<InternalDocument> resultList = internalDocumentDao.getItems(
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
                viewFactDao.applyViewFlagsOnInternalDocumentList(resultList, authData.getAuthorized());
            }
            return resultList;
        } else {
            logger.error("NO AUTH DATA");
            return null;
        }
    }


}
