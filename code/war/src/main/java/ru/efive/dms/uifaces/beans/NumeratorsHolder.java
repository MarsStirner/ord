package ru.efive.dms.uifaces.beans;

import ru.efive.dms.dao.NumeratorDAOImpl;
import ru.efive.dms.data.Numerator;
import ru.efive.dms.util.ApplicationHelper;
import ru.efive.uifaces.bean.AbstractDocumentListHolderBean;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

@Named("numerators")
@SessionScoped
public class NumeratorsHolder extends AbstractDocumentListHolderBean<Numerator> {

    public boolean isNeedRefresh() {
        return needRefresh;
    }

    public void setNeedRefresh(boolean needRefresh) {
        if (needRefresh) {
            this.markNeedRefresh();
        }
        this.needRefresh = needRefresh;
    }

    private List<Numerator> hashDocuments;
    private boolean needRefresh = true;

    public List<Numerator> getHashDocuments(int fromIndex, int toIndex) {
        toIndex = (this.getHashDocuments().size() < fromIndex + toIndex) ? this.getHashDocuments().size() : fromIndex + toIndex;
        return this.getHashDocuments().subList(fromIndex, toIndex);
    }

    public List<Numerator> getHashDocuments() {
        List<Numerator> result = new ArrayList<Numerator>();
        if (needRefresh) {
            try {
                result = new ArrayList<Numerator>(new HashSet<Numerator>(sessionManagement.getDAO(NumeratorDAOImpl.class, ApplicationHelper.NUMERATOR_DAO).findAllDocuments(filters, filter, false, true)));

                Collections.sort(result, new Comparator<Numerator>() {
                    @Override
                    public int compare(Numerator o1, Numerator o2) {
                        int result = 0;
                        String colId = getSorting().getColumnId();

                        if(colId.equalsIgnoreCase("numeratorIndex")) {
                            result = new Integer(o1.getNumeratorIndex()).compareTo(o2.getNumeratorIndex());
                        } else if(colId.equalsIgnoreCase("numberFormat")) {
                            result = ApplicationHelper.getNotNull(o1.getNumberFormat()).compareTo(ApplicationHelper.getNotNull(o1.getNumberFormat()));
                        } else if(colId.equalsIgnoreCase("creationDate")) {
                            Calendar c1 = Calendar.getInstance(ApplicationHelper.getLocale());
                            c1.setTime(ApplicationHelper.getNotNull(o1.getCreationDate()));
                            Calendar c2 = Calendar.getInstance(ApplicationHelper.getLocale());
                            c2.setTime(ApplicationHelper.getNotNull(o2.getCreationDate()));
                            result = c1.compareTo(c2);
                        }

                        if(getSorting().isAsc()) {
                            result *= -1;
                        }

                        return result;
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
        return new Sorting("numeratorIndex", false);
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
            this.needRefresh = true;
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