package ru.efive.uifaces.beans;

import java.util.List;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import ru.efive.uifaces.bean.AbstractDocumentListHolderBean;
import ru.efive.uifaces.demo.dao.ContractDaoEmulation;
import ru.efive.uifaces.demo.document.Contract;

/**
 *
 *
 * @author Pavel Porubov
 */
@ManagedBean(name = "dynaTableData")
@SessionScoped
public class DynaTableBean {

    private int pageSize = 20;
    private AbstractDocumentListHolderBean<Contract> contractList;

    public DynaTableBean() {
        contractList = new AbstractDocumentListHolderBean<Contract>() {

            @Override
            protected int getTotalCount() {
                return ContractDaoEmulation.getAllCount(null);
            }

            @Override
            protected List<Contract> loadDocuments() {
                return ContractDaoEmulation.getList(
                        null,
                        getPagination().getOffset(),
                        getPagination().getPageSize(),
                        getSorting().getColumnId(),
                        getSorting().isAsc());
            }
        };
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public AbstractDocumentListHolderBean<Contract> getContractList() {
        return contractList;
    }
}
