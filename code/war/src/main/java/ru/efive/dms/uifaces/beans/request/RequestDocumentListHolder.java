package ru.efive.dms.uifaces.beans.request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.efive.dms.dao.RequestDocumentDAOImpl;
import ru.efive.dms.dao.ViewFactDaoImpl;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentLazyDataModelBean;
import ru.efive.dms.uifaces.lazyDataModel.documents.LazyDataModelForRequestDocument;
import ru.efive.dms.util.DocumentSearchMapKeys;
import ru.entity.model.document.RequestDocument;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

import static ru.efive.dms.util.ApplicationDAONames.REQUEST_DOCUMENT_FORM_DAO;
import static ru.efive.dms.util.ApplicationDAONames.VIEW_FACT_DAO;

@ManagedBean(name = "request_documents")
@ViewScoped
public class RequestDocumentListHolder extends AbstractDocumentLazyDataModelBean<RequestDocument> {

    private static final Logger logger = LoggerFactory.getLogger("REQUEST_DOCUMENT");
    private Map<String, Object> filters = new HashMap<String, Object>();
    @Inject
    @Named("sessionManagement")
    private transient SessionManagementBean sessionManagement;
    private RequestDocumentDAOImpl dao;
    private ViewFactDaoImpl viewFactDao;

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
                    final String value = entry.getValue();
                    logger.info("{} = {}", entry.getKey(), value);
                    if (value.startsWith("{") && value.endsWith("}")) {
                        // Список
                        final List<String> strings = Arrays.asList(value.substring(1, value.length() - 1).split("\\s*,\\s*"));
                        if (!strings.isEmpty()) {
                            //Для некоторых парметров надо приводить типы
                            if (DocumentSearchMapKeys.STATUS_LIST_KEY.equals(entry.getKey())) {
                                final Set<Integer> ints = new HashSet<Integer>(strings.size());
                                for (String string : strings) {
                                    ints.add(Integer.valueOf(string));
                                }
                                filters.put(entry.getKey(), ints);
                            } else {
                                filters.put(entry.getKey(), new ArrayList<String>(strings));
                            }
                        }
                    } else {
                        //Одиночное значение
                        filters.put(entry.getKey(), value);
                    }
                }
            }

        }
        dao = sessionManagement.getDAO(RequestDocumentDAOImpl.class, REQUEST_DOCUMENT_FORM_DAO);
        viewFactDao = sessionManagement.getDAO(ViewFactDaoImpl.class, VIEW_FACT_DAO);
        final LazyDataModelForRequestDocument lazyDataModel = new LazyDataModelForRequestDocument(
                dao, viewFactDao, sessionManagement.getAuthData()
        );
        lazyDataModel.setFilters(filters);
        setLazyModel(lazyDataModel);
    }
}