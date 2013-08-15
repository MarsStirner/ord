package ru.efive.dms.uifaces.beans.request;

import ru.efive.dms.dao.RequestDocumentDAOImpl;
import ru.efive.dms.data.RequestDocument;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.util.ApplicationHelper;
import ru.efive.sql.dao.user.UserDAOHibernate;
import ru.efive.sql.entity.user.User;
import ru.efive.uifaces.bean.AbstractDocumentListHolderBean;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

@Named("request_documents")
@SessionScoped
public class RequestDocumentListHolder extends AbstractDocumentListHolderBean<RequestDocument> {

    private List<RequestDocument> hashDocuments;
    private boolean needRefresh = true;

    protected List<RequestDocument> getHashDocuments(int fromIndex, int toIndex) {
        toIndex = (this.hashDocuments.size() < fromIndex + toIndex) ? getHashDocuments().size() : fromIndex + toIndex;
        List<RequestDocument> result = new ArrayList<RequestDocument>(getHashDocuments().subList(fromIndex, toIndex));
        return result;
    }

    protected List<RequestDocument> getHashDocuments() {
        List<RequestDocument> result = new ArrayList<RequestDocument>();
        if (needRefresh) {
            try {
                User user = sessionManagement.getLoggedUser();
                user = sessionManagement.getDAO(UserDAOHibernate.class, ApplicationHelper.USER_DAO).findByLoginAndPassword(user.getLogin(), user.getPassword());
                result = new ArrayList<RequestDocument>(new HashSet<RequestDocument>(sessionManagement.getDAO(RequestDocumentDAOImpl.class, ApplicationHelper.REQUEST_DOCUMENT_FORM_DAO).findAllDocumentsByUser(filter, user, false, false)));

                Collections.sort(result, new Comparator<RequestDocument>() {
                    public int compare(RequestDocument o1, RequestDocument o2) {
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
                        } else if(colId.equalsIgnoreCase("deliveryDate")) {
                            Calendar c1 = Calendar.getInstance(ApplicationHelper.getLocale());
                            c1.setTime(ApplicationHelper.getNotNull(o1.getDeliveryDate()));
                            Calendar c2 = Calendar.getInstance(ApplicationHelper.getLocale());
                            c2.setTime(ApplicationHelper.getNotNull(o2.getDeliveryDate()));
                            result = c2.compareTo(c1);
                        } else if(colId.equalsIgnoreCase("contragent")) {
                            result = ApplicationHelper.getNotNull(o1.getSenderDescriptionShort()).compareTo(ApplicationHelper.getNotNull(o2.getSenderDescriptionShort()));
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
        return new Sorting("deliveryDate", false);
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

    private static final long serialVersionUID = 8535420074467871583L;
}