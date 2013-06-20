package ru.efive.dms.uifaces.beans.incoming;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import ru.efive.sql.dao.user.UserDAOHibernate;
import ru.efive.sql.entity.user.User;
import ru.efive.dms.dao.IncomingDocumentDAOImpl;
import ru.efive.dms.data.IncomingDocument;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.util.ApplicationHelper;
import ru.efive.uifaces.bean.AbstractDocumentListHolderBean;


@Named("in_documents")
@SessionScoped
public class IncomingDocumentListHolder extends AbstractDocumentListHolderBean<IncomingDocument> {
    private static final String beanName = "in_documents";

    public boolean isNeedRefresh() {
        return needRefresh;
    }

    public void setNeedRefresh(boolean needRefresh) {
        if (needRefresh == true) {
            this.markNeedRefresh();
        }
        this.needRefresh = needRefresh;
    }

    private List<IncomingDocument> hashDocuments;
    private boolean needRefresh = true;

    protected List<IncomingDocument> getHashDocuments(int fromIndex, int toIndex) {
        List<IncomingDocument> result = new ArrayList<IncomingDocument>();
        if (needRefresh) {
            sessionManagement.registrateBeanName(beanName);
            try {
                User user = sessionManagement.getLoggedUser();

                user = sessionManagement.getDAO(UserDAOHibernate.class, ApplicationHelper.USER_DAO).findByLoginAndPassword(user.getLogin(), user.getPassword());
                this.hashDocuments = new ArrayList<IncomingDocument>(new HashSet<IncomingDocument>(sessionManagement.
                        getDAO(IncomingDocumentDAOImpl.class, ApplicationHelper.INCOMING_DOCUMENT_FORM_DAO).findAllDocumentsByUser(filter, user, false, false)));

                Collections.sort(this.hashDocuments, new Comparator<IncomingDocument>() {
                    public int compare(IncomingDocument o1, IncomingDocument o2) {
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

    protected List<IncomingDocument> getHashDocuments() {
        List<IncomingDocument> result = new ArrayList<IncomingDocument>();
        if (needRefresh) {
            sessionManagement.registrateBeanName(beanName);
            try {
                User user = sessionManagement.getLoggedUser();
                user = sessionManagement.getDAO(UserDAOHibernate.class, ApplicationHelper.USER_DAO).findByLoginAndPassword(user.getLogin(), user.getPassword());
                result = new ArrayList<IncomingDocument>(new HashSet<IncomingDocument>(sessionManagement.getDAO(IncomingDocumentDAOImpl.class,
                        ApplicationHelper.INCOMING_DOCUMENT_FORM_DAO).findAllDocumentsByUser(filter, user, false, false)));
                Collections.sort(result, new Comparator<IncomingDocument>() {
                    public int compare(IncomingDocument o1, IncomingDocument o2) {
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
    protected List<IncomingDocument> loadDocuments() {
        List<IncomingDocument> result = new ArrayList<IncomingDocument>();
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
    public List<IncomingDocument> getDocuments() {
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

    @Inject
    @Named("sessionManagement")
    private transient SessionManagementBean sessionManagement;

    private static final long serialVersionUID = 8535420074467871583L;


}