package ru.efive.uifaces.bean;

import java.io.Serializable;

public class Pagination implements Serializable {

    public static final int DEFAULT_PAGE_SIZE = 10;

    public static final int MIN_PAGE_SIZE = 10;

    public static final int MAX_PAGE_SIZE = 150;

    private static final long serialVersionUID = 1L;

    private final int offset;

    private final int totalCount;

    private final int pageSize;

    private final int pageCount;

    private final int pageOffset;

    public Pagination(int offset, int totalCount, int pageSize) {
        this.offset = Math.max(0, offset < totalCount? offset: totalCount - totalCount % pageSize);
        this.totalCount = totalCount;
        this.pageSize = pageSize;

        pageCount = (totalCount + pageSize - 1) / pageSize;
        pageOffset = this.offset / pageSize;
    }

    public int getOffset() {
        return offset;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public int getPageCount() {
        return pageCount;
    }

    public int getPageOffset() {
        return pageOffset;
    }

    public int getPageSize() {
        return pageSize;
    }
}