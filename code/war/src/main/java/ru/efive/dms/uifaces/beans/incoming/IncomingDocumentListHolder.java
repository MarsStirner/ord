package ru.efive.dms.uifaces.beans.incoming;

import ru.efive.dms.dao.IncomingDocumentDAOImpl;
import ru.efive.dms.data.IncomingDocument;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.util.ApplicationHelper;
import ru.efive.sql.dao.user.UserDAOHibernate;
import ru.efive.sql.entity.user.User;
import ru.efive.uifaces.bean.AbstractDocumentListHolderBean;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;


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
        toIndex = (this.getHashDocuments().size() < fromIndex + toIndex) ? this.getHashDocuments().size() : fromIndex + toIndex;
        List<IncomingDocument> result = new ArrayList<IncomingDocument>(this.getHashDocuments().subList(fromIndex, toIndex));
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
                                int result = 0;

                        String colId = ApplicationHelper.getNotNull(getSorting().getColumnId());

                        if(colId.equalsIgnoreCase("registrationDate")) {
                            Date d1 = ApplicationHelper.getNotNull(o1.getRegistrationDate());
                            Calendar c1 = Calendar.getInstance(ApplicationHelper.getLocale());
                            c1.setTime(d1);
                            c1.set(Calendar.HOUR_OF_DAY, 0);
                            c1.set(Calendar.MINUTE, 0);
                            c1.set(Calendar.SECOND, 0);
                            Date d2 = ApplicationHelper.getNotNull(o2.getRegistrationDate());
                            Calendar c2 = Calendar.getInstance(ApplicationHelper.getLocale());
                            c2.setTime(d2);
                            c2.set(Calendar.HOUR_OF_DAY, 0);
                            c2.set(Calendar.MINUTE, 0);
                            c2.set(Calendar.SECOND, 0);
                            if(c1.equals(c2)) {
                                try {
                                    Integer i1 = Integer.parseInt(ApplicationHelper.getNotNull(o1.getRegistrationNumber()));
                                    Integer i2 = Integer.parseInt(ApplicationHelper.getNotNull(o2.getRegistrationNumber()));
                                    result = i1.compareTo(i2);
                                } catch(NumberFormatException e) {
                                    result = ApplicationHelper.getNotNull(o1.getRegistrationNumber()).compareTo(ApplicationHelper.getNotNull(o2.getRegistrationNumber()));
                                }
                            } else {
                                result = c1.compareTo(c2);
                            }
                        } else if(colId.equalsIgnoreCase("registrationNumber")) {
                            try {
                                Integer i1 = Integer.parseInt(ApplicationHelper.getNotNull(o1.getRegistrationNumber()));
                                Integer i2 = Integer.parseInt(ApplicationHelper.getNotNull(o2.getRegistrationNumber()));
                                result = i1.compareTo(i2);
                            } catch(NumberFormatException e) {
                                result = ApplicationHelper.getNotNull(o1.getRegistrationNumber()).compareTo(ApplicationHelper.getNotNull(o2.getRegistrationNumber()));
                            }
                        } else if(colId.equalsIgnoreCase("deliveryDate")) {
                            Calendar c1 = Calendar.getInstance(ApplicationHelper.getLocale());
                            c1.setTime(ApplicationHelper.getNotNull(o1.getDeliveryDate()));
                            Calendar c2 = Calendar.getInstance(ApplicationHelper.getLocale());
                            c2.setTime(ApplicationHelper.getNotNull(o2.getDeliveryDate()));
                            result = c2.compareTo(c1);
                        } else if(colId.equalsIgnoreCase("contragent")) {
                            result = ApplicationHelper.getNotNull(ApplicationHelper.getNotNull(o1.getContragent()).getShortName()).compareTo(ApplicationHelper.getNotNull(ApplicationHelper.getNotNull(o2.getContragent()).getShortName()));
                        } else if(colId.equalsIgnoreCase("form")) {
                            result = ApplicationHelper.getNotNull(ApplicationHelper.getNotNull(o1.getForm()).toString()).compareTo(ApplicationHelper.getNotNull(ApplicationHelper.getNotNull(o2.getForm()).toString()));
                        } else if(colId.equalsIgnoreCase("executionDate")) {
                            Calendar c1 = Calendar.getInstance(ApplicationHelper.getLocale());
                            c1.setTime(ApplicationHelper.getNotNull(o1.getExecutionDate()));
                            Calendar c2 = Calendar.getInstance(ApplicationHelper.getLocale());
                            c2.setTime(ApplicationHelper.getNotNull(o2.getExecutionDate()));
                            result = c1.compareTo(c2);
                        } else if(colId.equalsIgnoreCase("status_id")) {
                            result = ApplicationHelper.getNotNull(ApplicationHelper.getNotNull(o1.getDocumentStatus()).getName()).compareTo(ApplicationHelper.getNotNull(ApplicationHelper.getNotNull(o2.getDocumentStatus()).getName()));
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
        return new Sorting("registrationDate", true);
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