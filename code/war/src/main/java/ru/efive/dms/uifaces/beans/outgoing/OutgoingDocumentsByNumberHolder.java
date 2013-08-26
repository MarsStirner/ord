package ru.efive.dms.uifaces.beans.outgoing;

import ru.efive.dms.dao.OutgoingDocumentDAOImpl;
import ru.efive.dms.data.OutgoingDocument;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.util.ApplicationHelper;
import ru.efive.sql.dao.user.UserDAOHibernate;
import ru.efive.sql.entity.user.User;
import ru.efive.uifaces.bean.AbstractDocumentListHolderBean;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

@Named("outDocumentsByNumber")
@SessionScoped
public class OutgoingDocumentsByNumberHolder extends AbstractDocumentListHolderBean<OutgoingDocument> {
    private static final String beanName = "outDocumentsByNumber";

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
                        } else if(colId.equalsIgnoreCase("registrationNumber")) {
                            try {
                                Integer i1 = Integer.parseInt(ApplicationHelper.getNotNull(o1.getRegistrationNumber()));
                                Integer i2 = Integer.parseInt(ApplicationHelper.getNotNull(o2.getRegistrationNumber()));
                                result = i1.compareTo(i2);
                            } catch(NumberFormatException e) {
                                result = ApplicationHelper.getNotNull(o1.getRegistrationNumber()).compareTo(ApplicationHelper.getNotNull(o2.getRegistrationNumber()));
                            }
                        } else if(colId.equalsIgnoreCase("signatureDate")) {
                            Calendar c1 = Calendar.getInstance(ApplicationHelper.getLocale());
                            c1.setTime(ApplicationHelper.getNotNull(o1.getSignatureDate()));
                            Calendar c2 = Calendar.getInstance(ApplicationHelper.getLocale());
                            c2.setTime(ApplicationHelper.getNotNull(o2.getSignatureDate()));
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
        return new Sorting("registrationDate", true);
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
        filters.put("registrationNumber", "%");
    }

    @Inject
    @Named("sessionManagement")
    private transient SessionManagementBean sessionManagement;

    private static final long serialVersionUID = 8535420074467871583L;
}