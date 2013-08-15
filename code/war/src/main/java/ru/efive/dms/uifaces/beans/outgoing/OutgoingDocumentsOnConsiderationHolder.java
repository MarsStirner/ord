package ru.efive.dms.uifaces.beans.outgoing;

import ru.efive.dms.dao.OutgoingDocumentDAOImpl;
import ru.efive.dms.data.OutgoingDocument;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.util.ApplicationHelper;
import ru.efive.sql.dao.user.UserDAOHibernate;
import ru.efive.sql.entity.enums.DocumentStatus;
import ru.efive.sql.entity.user.User;
import ru.efive.uifaces.bean.AbstractDocumentListHolderBean;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

@Named("outDocumentsOnConsideration")
@SessionScoped
public class OutgoingDocumentsOnConsiderationHolder extends AbstractDocumentListHolderBean<OutgoingDocument> {
    private static final String beanName = "outDocumentsOnCosideration";

    private List<OutgoingDocument> hashDocuments;
    private boolean needRefresh = true;

    public boolean isNeedRefresh() {
        return needRefresh;
    }

    public void setNeedRefresh(boolean needRefresh) {
        if (needRefresh == true) {
            this.markNeedRefresh();
        }
        this.needRefresh = needRefresh;
    }

    protected List<OutgoingDocument> getHashDocuments(int fromIndex, int toIndex) {
        toIndex = (this.getHashDocuments().size() < fromIndex + toIndex) ? this.getHashDocuments().size() : fromIndex + toIndex;
        List<OutgoingDocument> result = new ArrayList<OutgoingDocument>(this.getHashDocuments().subList(fromIndex, toIndex));
        return result;
    }

    protected List<OutgoingDocument> getHashDocuments() {
        List<OutgoingDocument> result = new ArrayList<OutgoingDocument>();
        if (needRefresh) {
            sessionManagement.registrateBeanName(beanName);
            try {
                User user = sessionManagement.getLoggedUser();
                user = sessionManagement.getDAO(UserDAOHibernate.class, ApplicationHelper.USER_DAO).findByLoginAndPassword(user.getLogin(), user.getPassword());
                result = new ArrayList<OutgoingDocument>(new HashSet<OutgoingDocument>(sessionManagement.getDAO(OutgoingDocumentDAOImpl.class, ApplicationHelper.OUTGOING_DOCUMENT_FORM_DAO).findAllDocumentsByUser(filters, filter, user, false, false)));

                Collections.sort(result, new Comparator<OutgoingDocument>() {
                    public int compare(OutgoingDocument o1, OutgoingDocument o2) {
                        int result = 0;
                        String colId = getSorting().getColumnId();

                        if(colId.equalsIgnoreCase("registrationNumber")) {
                            try {
                                Integer i1 = Integer.parseInt(ApplicationHelper.getNotNull(o1.getRegistrationNumber()));
                                Integer i2 = Integer.parseInt(ApplicationHelper.getNotNull(o2.getRegistrationNumber()));
                                result = i1.compareTo(i2);
                            } catch(NumberFormatException e) {
                                result = ApplicationHelper.getNotNull(o1.getRegistrationNumber()).compareTo(ApplicationHelper.getNotNull(o2.getRegistrationNumber()));
                            }
                        } else if(colId.equalsIgnoreCase("registrationDate")) {
                            Date d1 = o1.isRegistered() ? ApplicationHelper.getNotNull(o1.getRegistrationDate()) : ApplicationHelper.getNotNull(o1.getCreationDate());
                            Date d2 = o2.isRegistered() ? ApplicationHelper.getNotNull(o2.getRegistrationDate()) : ApplicationHelper.getNotNull(o2.getCreationDate());
                            Calendar c1 = Calendar.getInstance(ApplicationHelper.getLocale());
                            c1.setTime(d1);
                            Calendar c2 = Calendar.getInstance(ApplicationHelper.getLocale());
                            c2.setTime(d2);
                            result = c2.compareTo(c1);
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
        return new Sorting("registrationDate", false);
    }

    /*@Override
        protected int getTotalCount() {
            int result = 0;
            try {
                User user=sessionManagement.getLoggedUser();
                user = sessionManagement.getDAO(UserDAOHibernate.class, ApplicationHelper.USER_DAO).findByLoginAndPassword(user.getLogin(),user.getPassword());
                return new Long(sessionManagement.getDAO(OutgoingDocumentDAOImpl.class, ApplicationHelper.OUTGOING_DOCUMENT_FORM_DAO).countAllDocumentsByUser(filter,
                        user, false, false)).intValue();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected List<OutgoingDocument> loadDocuments() {
            List<OutgoingDocument> result = new ArrayList<OutgoingDocument>();
            try {
                User user=sessionManagement.getLoggedUser();
                user = sessionManagement.getDAO(UserDAOHibernate.class, ApplicationHelper.USER_DAO).findByLoginAndPassword(user.getLogin(),user.getPassword());
                result = sessionManagement.getDAO(OutgoingDocumentDAOImpl.class, ApplicationHelper.OUTGOING_DOCUMENT_FORM_DAO).findAllDocumentsByUser(filter, user,
                        false, false, getPagination().getOffset(), getPagination().getPageSize(), getSorting().getColumnId(), getSorting().isAsc());
                result = new ArrayList<OutgoingDocument>(new HashSet<OutgoingDocument>(result));
                Collections.sort(result, new Comparator<OutgoingDocument>() {
                    public int compare(OutgoingDocument o1, OutgoingDocument o2) {
                        Calendar c1 = Calendar.getInstance(ApplicationHelper.getLocale());
                        c1.setTime(o1.getRegistrationDate());
                        Calendar c2 = Calendar.getInstance(ApplicationHelper.getLocale());
                        c2.setTime(o2.getRegistrationDate());
                        return c2.compareTo(c1);
                    }
                });
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }
    */

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
    protected List<OutgoingDocument> loadDocuments() {
        List<OutgoingDocument> result = new ArrayList<OutgoingDocument>();
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
    public List<OutgoingDocument> getDocuments() {
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
    static private Map<String, Object> filters = new HashMap<String, Object>();

    static {
        filters.put("statusId", DocumentStatus.ON_CONSIDERATION.getId());
    }

    @Inject
    @Named("sessionManagement")
    private transient SessionManagementBean sessionManagement;

    private static final long serialVersionUID = 8535420074467871583L;
}