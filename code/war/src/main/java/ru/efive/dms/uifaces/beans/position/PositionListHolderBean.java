package ru.efive.dms.uifaces.beans.position;

import com.github.javaplugs.jsf.SpringScopeView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.uifaces.bean.AbstractDocumentListHolderBean;
import ru.efive.uifaces.bean.Pagination;
import ru.entity.model.referenceBook.Position;
import ru.hitsl.sql.dao.interfaces.referencebook.PositionDao;

import org.springframework.stereotype.Controller;
import java.util.List;

/**
 * Author: Upatov Egor <br>
 * Date: 12.09.2014, 18:01 <br>
 * Company: Korus Consulting IT <br>
 * Description: Бин для получения списка должностей (ApplicationScope) <br>
 */
@Controller("positionList")
@SpringScopeView
public class PositionListHolderBean extends AbstractDocumentListHolderBean<Position> {

    @Autowired
    @Qualifier("positionDao")
    private static PositionDao positionDao;
    private String filter;

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
        return positionDao.countItems(filter, null, false);
    }

    @Override
    protected List<Position> loadDocuments() {
        return positionDao.getItems(filter, null,
                getSorting().getColumnId(), getSorting().isAsc(), getPagination().getOffset(), getPagination().getPageSize(), false);
    }
}
