package ru.efive.dms.uifaces.beans.position;

import ru.efive.dms.uifaces.beans.IndexManagementBean;
import ru.efive.dms.util.ApplicationHelper;
import ru.efive.sql.dao.user.PositionDAO;
import ru.efive.sql.entity.user.Position;
import ru.efive.uifaces.bean.AbstractDocumentListHolderBean;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import java.util.List;

/**
 * Author: Upatov Egor <br>
 * Date: 12.09.2014, 18:01 <br>
 * Company: Korus Consulting IT <br>
 * Description: Бин для получения списка должностей (ApplicationScope) <br>
 */
@Singleton
@Named("positionList")
@ApplicationScoped
public class PositionListHolderBean extends AbstractDocumentListHolderBean<Position>{

    private String filter;
    private static PositionDAO dao;
    @Inject
    @Named("indexManagement")
    private IndexManagementBean indexManagement;

    @PostConstruct
    public void init() {
        dao = indexManagement.getContext().getBean(ApplicationHelper.POSITION_DAO, PositionDAO.class);
    }


    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    @Override
    public Pagination initPagination() {
        return new Pagination(0, getTotalCount(), 25);
    }

    @Override
    public Sorting initSorting() {
        return new Sorting("value", true);
    }


    @Override
    protected int getTotalCount() {
        return (int) dao.countDocument(filter, false);
    }

    @Override
    protected List<Position> loadDocuments() {
        return dao.findDocuments(filter, false, getPagination().getOffset(), getPagination().getPageSize(), getSorting().getColumnId(), getSorting().isAsc());
    }
}
