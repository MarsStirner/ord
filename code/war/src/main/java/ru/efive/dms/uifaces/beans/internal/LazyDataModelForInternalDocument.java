package ru.efive.dms.uifaces.beans.internal;

import com.github.javaplugs.jsf.SpringScopeView;
import org.primefaces.model.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import ru.efive.dms.uifaces.lazyDataModel.AbstractDocumentableLazyDataModel;
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
public class LazyDataModelForInternalDocument extends AbstractDocumentableLazyDataModel<InternalDocument> {
    @Autowired
    public LazyDataModelForInternalDocument(
            @Qualifier("internalDocumentDao") final InternalDocumentDao internalDocumentDao,
            @Qualifier("authData") AuthorizationData authData,
            @Qualifier("viewFactDao") ViewFactDao viewFactDao) {
        super(internalDocumentDao, authData, viewFactDao);
    }


    /**
     * При каждом запросе страницы (нового view) инициализировать список фильтров
     */
    @PostConstruct
    public void initInternalDocumentList() {
        if (!FacesContext.getCurrentInstance().isPostback()) {
            final Map<String, String> parameterMap = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
            if (!parameterMap.isEmpty()) {
                log.info("List initialize with {} params", parameterMap.size());
                filters.clear();
                for (Map.Entry<String, String> entry : parameterMap.entrySet()) {
                    log.info("{} = {}", entry.getKey(), entry.getValue());
                    filters.put(entry.getKey(), entry.getValue());
                }
            } else {
                filters.put("registrationNumber", "%");
            }
        }
    }
}
