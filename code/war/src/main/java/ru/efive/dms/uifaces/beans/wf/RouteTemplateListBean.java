package ru.efive.dms.uifaces.beans.wf;

import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.uifaces.bean.AbstractDocumentListHolderBean;
import ru.efive.uifaces.bean.Pagination;
import ru.efive.wf.core.dao.EngineDAOImpl;
import ru.entity.model.referenceBook.Nomenclature;
import ru.entity.model.wf.RouteTemplate;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

import static ru.hitsl.sql.dao.util.ApplicationDAONames.ENGINE_DAO;

@Named("routeTemplates")
@SessionScoped
public class RouteTemplateListBean extends AbstractDocumentListHolderBean<RouteTemplate> {

    @Override
    public Pagination initPagination() {
        return new Pagination(0, getTotalCount(), 25);
    }

    @Override
    protected Sorting initSorting() {
        return new Sorting("name", true);
    }

    @Override
    protected int getTotalCount() {
        int result = 0;
        try {
            result = new Long(sessionManagement.getDAO(EngineDAOImpl.class, ENGINE_DAO).countRouteTemplates(sessionManagement.getLoggedUser(), false)).intValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    protected List<RouteTemplate> loadDocuments() {
        List<RouteTemplate> result = new ArrayList<RouteTemplate>();
        try {
            result = sessionManagement.getDAO(EngineDAOImpl.class, ENGINE_DAO).findRouteTemplates(sessionManagement.getLoggedUser(), false,
                    getPagination().getOffset(), getPagination().getPageSize(), getSorting().getColumnId(), getSorting().isAsc());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public RouteTemplate getTemplateByNomenclature(Nomenclature nomenclature) {
        RouteTemplate result = null;
        try {
            result = sessionManagement.getDAO(EngineDAOImpl.class, ENGINE_DAO).findRouteTemplates(sessionManagement.getLoggedUser(), false,
                    -1, -1, "name", true).get(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    @Inject
    @Named("sessionManagement")
    private transient SessionManagementBean sessionManagement;

    private static final long serialVersionUID = -6594220699938509381L;
}