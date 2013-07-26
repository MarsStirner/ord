package ru.efive.dms.uifaces.beans.request;

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

import ru.efive.dms.dao.RequestDocumentDAOImpl;
import ru.efive.dms.data.RequestDocument;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.util.ApplicationHelper;
import ru.efive.sql.dao.user.UserDAOHibernate;
import ru.efive.sql.entity.user.User;
import ru.efive.uifaces.bean.AbstractDocumentListHolderBean;

@Named("appealRequestDocuments")
@SessionScoped
public class RequestDocumentsEqAppealHolder extends AbstractDocumentListHolderBean<RequestDocument> {

    private List<RequestDocument> hashDocuments;
    private boolean needRefresh = true;

    protected List<RequestDocument> getHashDocuments(int fromIndex, int toIndex) {        
        if (needRefresh) {
            try {
                User user = sessionManagement.getLoggedUser();
                user = sessionManagement.getDAO(UserDAOHibernate.class, ApplicationHelper.USER_DAO).findByLoginAndPassword(user.getLogin(), user.getPassword());
                this.hashDocuments = new ArrayList<RequestDocument>(new HashSet<RequestDocument>(sessionManagement.getDAO(RequestDocumentDAOImpl.class, ApplicationHelper.REQUEST_DOCUMENT_FORM_DAO).findAllDocumentsByUser(filters, filter, user, false, false)));

                Collections.sort(this.hashDocuments, new Comparator<RequestDocument>() {
                    public int compare(RequestDocument o1, RequestDocument o2) {
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
        List<RequestDocument> result = new ArrayList<RequestDocument>(this.hashDocuments.subList(fromIndex, toIndex));
        return result;
    }

    protected List<RequestDocument> getHashDocuments() {
        List<RequestDocument> result = new ArrayList<RequestDocument>();
        if (needRefresh) {
            try {
                User user = sessionManagement.getLoggedUser();
                user = sessionManagement.getDAO(UserDAOHibernate.class, ApplicationHelper.USER_DAO).findByLoginAndPassword(user.getLogin(), user.getPassword());
                result = new ArrayList<RequestDocument>(new HashSet<RequestDocument>(sessionManagement.getDAO(RequestDocumentDAOImpl.class, ApplicationHelper.REQUEST_DOCUMENT_FORM_DAO).findAllDocumentsByUser(filters, filter, user, false, false)));
                Collections.sort(result, new Comparator<RequestDocument>() {
                    public int compare(RequestDocument o1, RequestDocument o2) {
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
    protected List<RequestDocument> loadDocuments() {
        List<RequestDocument> result = new ArrayList<RequestDocument>();
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
    public List<RequestDocument> getDocuments() {
        return super.getDocuments();
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.needRefresh = true;
        this.filter = filter;
    }

    private String filter;

    @Inject
    @Named("sessionManagement")
    private transient SessionManagementBean sessionManagement;
    static private Map<String, Object> filters = new HashMap<String, Object>();

    static {
        filters.put("formCategory", "Обращения граждан");
        filters.put("formValue", "Жалоба");
    }

    private static final long serialVersionUID = 8535420074467871583L;
}