package ru.efive.uifaces.bean;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author Denis Kotegov
 */
public abstract class AbstractDocumentListHolderBean<D extends Serializable> implements Serializable {

    public static class Sorting implements Serializable {

        private static final long serialVersionUID = 1L;

        private final String columnId;

        private final boolean asc;

        public Sorting(String columnId, boolean asc) {
            this.columnId = columnId;
            this.asc = asc;
        }

        public boolean isAsc() {
            return asc;
        }

        public String getColumnId() {
            return columnId;
        }

    }

    // Changeable state -----------------------------------------------------------------------------------------------

    private List<D> documents;

    private Pagination pagination;

    private Sorting sorting;

    private boolean needRefresh = false;

    private String pageToGo;

    private int pageSizeToSelect;

    private boolean initialized = false;

    // Can be overrided -----------------------------------------------------------------------------------------------

    protected abstract int getTotalCount();

    protected abstract List<D> loadDocuments();

    protected Pagination initPagination() {
        return new Pagination(0, getTotalCount(), Pagination.DEFAULT_PAGE_SIZE);
    }

    protected Sorting initSorting() {
        return new Sorting(null, false);
    }

    public List<D> getDocuments() {
        return doGetDocuments();
    }

    // Should not be overrided ----------------------------------------------------------------------------------------

    private void adjustUserInputs() {
        pageToGo = String.valueOf(pagination.getPageOffset() + 1);
        pageSizeToSelect = pagination.getPageSize();
    }

    private List<D> doGetDocuments() {
        if (!initialized) {
            initialized = true;
            needRefresh = false;
            reset();
        } else if (needRefresh) {
            needRefresh = false;
            refresh();
        }

        return documents;
    }

    public void refresh() {
        pagination = new Pagination(pagination.getOffset(), getTotalCount(), pagination.getPageSize());
        documents = loadDocuments();
        adjustUserInputs();
    }

    public void reset() {
        sorting = initSorting();
        pagination = initPagination();
        documents = loadDocuments();
        adjustUserInputs();
    }

    public Pagination getPagination() {
        return pagination;
    }

    public void markNeedRefresh() {
        needRefresh = true;
    }

    public Sorting getSorting() {
        return sorting;
    }

    public void sort(String columnId) {
        if (columnId != null && columnId.equals(sorting.getColumnId()) || columnId == null) {
            sorting = new Sorting(columnId, !sorting.isAsc());
        } else {
            sorting = new Sorting(columnId, true);
        }

        pagination = new Pagination(0, getTotalCount(), pagination.getPageSize());
        documents = loadDocuments();
        adjustUserInputs();
    }

    public void changePageSize(int pageSize) {
        if (!initialized) {
            initialized = true;
            needRefresh = false;
        }
        if (sorting == null) {
            sorting = initSorting();
        }
        pagination = new Pagination(0, getTotalCount(),
                Math.min(Math.max(Pagination.MIN_PAGE_SIZE, pageSize), Pagination.MAX_PAGE_SIZE));
        documents = loadDocuments();
        adjustUserInputs();
    }

    // 0-based
    public void changePageOffset(int pageOffset) {
        pagination = new Pagination(pageOffset * pagination.getPageSize(), getTotalCount(), pagination.getPageSize());
        documents = loadDocuments();
        adjustUserInputs();
    }

    public void changeOffset(int offset) {
        pagination = new Pagination(offset, getTotalCount(), pagination.getPageSize());
        documents = loadDocuments();
        adjustUserInputs();
    }

    public void goToPage() {
        try {
            int page = Integer.parseInt(pageToGo);
            changePageOffset(page - 1);
        } catch (NumberFormatException ex) {
            // Nothing required
            System.out.println("User enter non-number as page to go parameter (" + ex.getMessage() + ").");
            adjustUserInputs();
        }
    }

    public void selectPageSize() {
        changePageSize(pageSizeToSelect);
    }

    public int getPageSizeToSelect() {
        return pageSizeToSelect;
    }

    public void setPageSizeToSelect(int pageSizeToSelect) {
        this.pageSizeToSelect = pageSizeToSelect;
    }

    // 1-based
    public String getPageToGo() {
        return pageToGo;
    }

    // 1-based
    public void setPageToGo(String pageToGo) {
        this.pageToGo = pageToGo;
    }

}
