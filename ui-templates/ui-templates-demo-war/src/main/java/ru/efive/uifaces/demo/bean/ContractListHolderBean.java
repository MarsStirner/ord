package ru.efive.uifaces.demo.bean;

import java.util.List;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import ru.efive.uifaces.bean.AbstractDocumentListHolderBean;
import ru.efive.uifaces.bean.AbstractDocumentListHolderBean.Pagination;
import ru.efive.uifaces.demo.dao.ContractDaoEmulation;
import ru.efive.uifaces.demo.document.Contract;

/**
 *
 * @author Denis Kotegov
 */
@Named("contractList")
@SessionScoped
public class ContractListHolderBean extends AbstractDocumentListHolderBean<Contract> {

    private static final long serialVersionUID = 1L;

    private boolean groupByAuthor;

    private boolean groupByCurator;

    @Override
    protected Pagination initPagination() {
        return new Pagination(0, getTotalCount(), 30);
    }

    @Override
    protected int getTotalCount() {
        return ContractDaoEmulation.getAllCount(null);
    }

    @Override
    protected List<Contract> loadDocuments() {
        if (groupByAuthor) {
            return ContractDaoEmulation.getGroupedList(
                    null,
                    getPagination().getOffset(),
                    getPagination().getPageSize(),
                    groupByCurator);
        } else {
            return ContractDaoEmulation.getList(
                    null,
                    getPagination().getOffset(),
                    getPagination().getPageSize(),
                    getSorting().getColumnId(),
                    getSorting().isAsc());
        }
    }

    @Override
    public List<Contract> getDocuments() {
        return super.getDocuments();
    }

    public void groupByAuthor() {
        groupByAuthor = true;
        groupByCurator = false;
        refresh();
    }

    public void groupByAuthorAndCurator() {
        groupByAuthor = true;
        groupByCurator = true;
        refresh();
    }

    public void ungroup() {
        groupByAuthor = false;
        groupByCurator = false;
        refresh();
    }

    public boolean isGroupingEnabled() {
        return groupByAuthor;
    }

    public boolean isGroupingByCuratorEnabled() {
        return groupByAuthor && groupByCurator;
    }

}
