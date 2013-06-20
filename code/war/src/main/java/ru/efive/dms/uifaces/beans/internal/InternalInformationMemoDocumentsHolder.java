package ru.efive.dms.uifaces.beans.internal;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import ru.efive.sql.dao.user.UserDAOHibernate;
import ru.efive.sql.entity.user.User;
import ru.efive.dms.dao.InternalDocumentDAOImpl;
import ru.efive.dms.data.InternalDocument;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.util.ApplicationHelper;
import ru.efive.uifaces.bean.AbstractDocumentListHolderBean;

@Named("internal_informationMemos")
@SessionScoped
public class InternalInformationMemoDocumentsHolder extends AbstractDocumentListHolderBean<InternalDocument> {
    private static final String beanName = "internal_informationMemos";

    public boolean isNeedRefresh() {
        return needRefresh;
    }

    public void setNeedRefresh(boolean needRefresh) {
        if (needRefresh == true) {
            this.markNeedRefresh();
        }
        this.needRefresh = needRefresh;
    }

    protected List<InternalDocument> getHashDocuments(int fromIndex, int toIndex) {
        List<InternalDocument> result = new ArrayList<InternalDocument>();
        if (needRefresh) {
            sessionManagement.registrateBeanName(beanName);
            try {
                User user = sessionManagement.getLoggedUser();
                user = sessionManagement.getDAO(UserDAOHibernate.class, ApplicationHelper.USER_DAO).findByLoginAndPassword(user.getLogin(), user.getPassword());
                this.hashDocuments = new ArrayList<InternalDocument>(new HashSet<InternalDocument>(sessionManagement.getDAO(InternalDocumentDAOImpl.class, ApplicationHelper.INTERNAL_DOCUMENT_FORM_DAO).findAllDocumentsByUser(filters, filter, user, false, false)));

                Collections.sort(this.hashDocuments, new Comparator<InternalDocument>() {
                    public int compare(InternalDocument o1, InternalDocument o2) {
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

    protected List<InternalDocument> getHashDocuments() {
        List<InternalDocument> result = new ArrayList<InternalDocument>();
        if (needRefresh) {
            sessionManagement.registrateBeanName(beanName);
            try {
                User user = sessionManagement.getLoggedUser();
                user = sessionManagement.getDAO(UserDAOHibernate.class, ApplicationHelper.USER_DAO).findByLoginAndPassword(user.getLogin(), user.getPassword());
                result = new ArrayList<InternalDocument>(new HashSet<InternalDocument>(sessionManagement.getDAO(InternalDocumentDAOImpl.class, ApplicationHelper.INTERNAL_DOCUMENT_FORM_DAO).findAllDocumentsByUser(filters, filter, user, false, false)));
                Collections.sort(result, new Comparator<InternalDocument>() {
                    public int compare(InternalDocument o1, InternalDocument o2) {
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
        return new Sorting("creationDate", false);
    }

    /*@Override
     protected int getTotalCount() {
         return new Long(sessionManagement.getDAO(InternalDocumentDAOImpl.class, ApplicationHelper.INTERNAL_DOCUMENT_FORM_DAO).countAllDocumentsByUser(filter,
                 sessionManagement.getLoggedUser(), false, false)).intValue();
     }

     @Override
     protected List<InternalDocument> loadDocuments() {
         return sessionManagement.getDAO(InternalDocumentDAOImpl.class, ApplicationHelper.INTERNAL_DOCUMENT_FORM_DAO).findAllDocumentsByUser(filter, sessionManagement.getLoggedUser(),
                 false, false, getPagination().getOffset(), getPagination().getPageSize(), getSorting().getColumnId(), getSorting().isAsc());
     }*/

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
    protected List<InternalDocument> loadDocuments() {
        List<InternalDocument> result = new ArrayList<InternalDocument>();
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
    public List<InternalDocument> getDocuments() {
        String key = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("key");
        if (key != null && !key.isEmpty()) {
            if (!filters.containsKey(key)) {
                this.needRefresh = true;
                markNeedRefresh();
                this.filters.clear();
                String value = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("value");
                this.filters.put(key, value);
            }
        }
        return super.getDocuments();
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.needRefresh = true;
        this.filter = filter;
    }

    private List<InternalDocument> hashDocuments;
    private boolean needRefresh = true;

    private String filter;
    static private Map<String, Object> filters = new HashMap<String, Object>();

    static {
        filters.put("formValue", "Информационное письмо");
        filters.put("formCategory", "Внутренние документы");
        filters.put("registrationNumber", "%");
    }

    @Inject
    @Named("sessionManagement")
    private transient SessionManagementBean sessionManagement;

    private static final long serialVersionUID = 8535420074467871583L;
}