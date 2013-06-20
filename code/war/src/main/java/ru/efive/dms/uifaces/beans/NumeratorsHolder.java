package ru.efive.dms.uifaces.beans;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import ru.efive.dms.dao.NumeratorDAOImpl;
import ru.efive.dms.data.Numerator;
import ru.efive.dms.util.ApplicationHelper;
import ru.efive.uifaces.bean.AbstractDocumentListHolderBean;

@Named("numerators")
@SessionScoped
public class NumeratorsHolder extends AbstractDocumentListHolderBean<Numerator> {

    public boolean isNeedRefresh() {
        return needRefresh;
    }

    public void setNeedRefresh(boolean needRefresh) {
        if (needRefresh == true) {
            this.markNeedRefresh();
        }
        this.needRefresh = needRefresh;
    }

    private List<Numerator> hashDocuments;
    private boolean needRefresh = true;

    protected List<Numerator> getHashDocuments(int fromIndex, int toIndex) {
        List<Numerator> result = new ArrayList<Numerator>();
        if (needRefresh) {
            try {
                this.hashDocuments = new ArrayList<Numerator>(new HashSet<Numerator>(sessionManagement.getDAO(NumeratorDAOImpl.class, ApplicationHelper.NUMERATOR_DAO).findAllDocuments(filters, filter, false, true)));

                Collections.sort(this.hashDocuments, new Comparator<Numerator>() {
                    public int compare(Numerator o1, Numerator o2) {
                        Calendar c1 = Calendar.getInstance(ApplicationHelper.getLocale());
                        c1.setTime(o1.getCreationDate());
                        Calendar c2 = Calendar.getInstance(ApplicationHelper.getLocale());
                        c2.setTime(o2.getCreationDate());
                        return c2.compareTo(c1);
                    }
                });
                needRefresh = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        toIndex = (this.hashDocuments.size() < fromIndex + toIndex) ? this.hashDocuments.size() : fromIndex + toIndex;
        result = this.hashDocuments.subList(fromIndex, toIndex);
        return result;
    }

    protected List<Numerator> getHashDocuments() {
        List<Numerator> result = new ArrayList<Numerator>();
        if (needRefresh) {
            try {
                result = new ArrayList<Numerator>(new HashSet<Numerator>(sessionManagement.getDAO(NumeratorDAOImpl.class, ApplicationHelper.NUMERATOR_DAO).findAllDocuments(filters, filter, false, true)));
                Collections.sort(result, new Comparator<Numerator>() {
                    public int compare(Numerator o1, Numerator o2) {
                        Calendar c1 = Calendar.getInstance(ApplicationHelper.getLocale());
                        c1.setTime(o1.getCreationDate());
                        Calendar c2 = Calendar.getInstance(ApplicationHelper.getLocale());
                        c2.setTime(o2.getCreationDate());
                        return c2.compareTo(c1);
                    }
                });

                this.hashDocuments = result;
                needRefresh = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            result = this.hashDocuments;
            //needRefresh=false;
        }
        return result;
    }

    @Override
    protected Pagination initPagination() {
        return new Pagination(0, getTotalCount(), 100);
    }

    @Override
    protected Sorting initSorting() {
        return new Sorting("creationDate,id", false);
    }

    @Override
    protected int getTotalCount() {
        int result = 0;
        try {
            result = new Long(this.getHashDocuments().size()).intValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    protected List<Numerator> loadDocuments() {
        List<Numerator> result = new ArrayList<Numerator>();
        try {
            result = this.getHashDocuments(getPagination().getOffset(), getPagination().getPageSize());


        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public void refresh() {
        this.needRefresh = true;
        super.refresh();
    }

    @Override
    public List<Numerator> getDocuments() {
        return super.getDocuments();
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.needRefresh = true;
        this.filter = filter;
    }

    public String getFilterByIndex() {
        return filter;
    }

    private String filter;
    private Map<String, Object> filters = new HashMap<String, Object>();

    @Inject
    @Named("sessionManagement")
    private transient SessionManagementBean sessionManagement;

    private static final long serialVersionUID = 999664946593243996L;
}