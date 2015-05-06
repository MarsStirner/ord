package ru.efive.dms.uifaces.beans.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.efive.dms.dao.InternalDocumentDAOImpl;
import ru.efive.dms.dao.ViewFactDaoImpl;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentLazyDataModelBean;
import ru.efive.dms.uifaces.lazyDataModel.documents.LazyDataModelForInternalDocument;
import ru.entity.model.document.InternalDocument;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static ru.efive.dms.util.ApplicationDAONames.INTERNAL_DOCUMENT_FORM_DAO;
import static ru.efive.dms.util.ApplicationDAONames.VIEW_FACT_DAO;

@Named("internal_documents")
@ViewScoped
public class InternalDocumentsListHolder extends AbstractDocumentLazyDataModelBean<InternalDocument> implements Serializable{

    private static final Logger logger = LoggerFactory.getLogger("INTERNAL_DOCUMENT");
    private InternalDocumentDAOImpl dao;
    private ViewFactDaoImpl viewFactDao;

    @Inject
    @Named("sessionManagement")
    private transient SessionManagementBean sessionManagement;

    private Map<String, Object> filters = new HashMap<String, Object>();

    @PostConstruct
    /**
     * При каждом запросе страницы (нового view) инициализировать список фильтров
     */
    public void initInternalDocumentList(){
        if(!FacesContext.getCurrentInstance().isPostback()) {
            final Map<String, String> parameterMap = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
            if (!parameterMap.isEmpty()) {
                logger.info("List initialize with {} params", parameterMap.size());
                filters.clear();
                for (Map.Entry<String, String> entry : parameterMap.entrySet()) {
                    logger.info("{} = {}", entry.getKey(), entry.getValue());
                    filters.put(entry.getKey(), entry.getValue());
                }
            } else  {
                filters.put("registrationNumber", "%");
            }
        }
        dao = sessionManagement.getDAO(InternalDocumentDAOImpl.class, INTERNAL_DOCUMENT_FORM_DAO);
        viewFactDao = sessionManagement.getDAO(ViewFactDaoImpl.class, VIEW_FACT_DAO);
        final LazyDataModelForInternalDocument lazyDataModel = new LazyDataModelForInternalDocument(
                dao,
                viewFactDao,
                sessionManagement.getAuthData()
        );
        lazyDataModel.setFilters(filters);
        setLazyModel(lazyDataModel);
    }

}