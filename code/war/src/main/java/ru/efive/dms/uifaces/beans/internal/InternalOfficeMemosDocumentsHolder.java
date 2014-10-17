package ru.efive.dms.uifaces.beans.internal;

import ru.efive.dms.dao.InternalDocumentDAOImpl;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.uifaces.bean.AbstractDocumentListHolderBean;
import ru.efive.uifaces.bean.Pagination;
import ru.entity.model.document.InternalDocument;
import ru.entity.model.user.User;
import ru.util.ApplicationHelper;

import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

import static ru.efive.dms.util.ApplicationDAONames.INTERNAL_DOCUMENT_FORM_DAO;

@Named("internal_officeMemos")
@SessionScoped
public class InternalOfficeMemosDocumentsHolder extends AbstractDocumentListHolderBean<InternalDocument> {
    private static final String beanName = "internal_officeMemos";

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
        toIndex = (this.getHashDocuments().size() < fromIndex + toIndex) ? this.getHashDocuments().size() : fromIndex + toIndex;
        List<InternalDocument> result = new ArrayList<InternalDocument>(this.getHashDocuments().subList(fromIndex, toIndex));
        return result;
    }

    protected List<InternalDocument> getHashDocuments() {
        List<InternalDocument> result = new ArrayList<InternalDocument>();
        if (needRefresh) {
            try {
                User user = sessionManagement.getLoggedUser();
                //user = sessionManagement.getDAO(UserDAOHibernate.class,USER_DAO).findByLoginAndPassword(user.getLogin(), user.getPassword());
                result = new ArrayList<InternalDocument>(new HashSet<InternalDocument>(sessionManagement.getDAO(InternalDocumentDAOImpl.class, INTERNAL_DOCUMENT_FORM_DAO).findAllDocumentsByUser(filters, filter, user, false, false)));

                Collections.sort(result, new Comparator<InternalDocument>() {
                    public int compare(InternalDocument o1, InternalDocument o2) {
                        int result = 0;
                        String colId = getSorting().getColumnId();

                        if (colId.equalsIgnoreCase("registrationDate")) {
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
                            if (c1.equals(c2)) {
                                try {
                                    Integer i1 = Integer.parseInt(ApplicationHelper.getNotNull(o1.getRegistrationNumber()));
                                    Integer i2 = Integer.parseInt(ApplicationHelper.getNotNull(o2.getRegistrationNumber()));
                                    result = i1.compareTo(i2);
                                } catch (NumberFormatException e) {
                                    result = ApplicationHelper.getNotNull(o1.getRegistrationNumber()).compareTo(ApplicationHelper.getNotNull(o2.getRegistrationNumber()));
                                }
                            } else {
                                result = c1.compareTo(c2);
                            }
                        } else if (colId.equalsIgnoreCase("registrationNumber")) {
                            try {
                                Integer i1 = Integer.parseInt(ApplicationHelper.getNotNull(o1.getRegistrationNumber()));
                                Integer i2 = Integer.parseInt(ApplicationHelper.getNotNull(o2.getRegistrationNumber()));
                                result = i1.compareTo(i2);
                            } catch (NumberFormatException e) {
                                result = ApplicationHelper.getNotNull(o1.getRegistrationNumber()).compareTo(ApplicationHelper.getNotNull(o2.getRegistrationNumber()));
                            }
                        } else if (colId.equalsIgnoreCase("signatureDate")) {
                            Calendar c1 = Calendar.getInstance(ApplicationHelper.getLocale());
                            c1.setTime(ApplicationHelper.getNotNull(o1.getSignatureDate()));
                            Calendar c2 = Calendar.getInstance(ApplicationHelper.getLocale());
                            c2.setTime(ApplicationHelper.getNotNull(o2.getSignatureDate()));
                            result = c2.compareTo(c1);
                        } else if (colId.equalsIgnoreCase("responsible")) {
                            result = ApplicationHelper.getNotNull(ApplicationHelper.getNotNull(o1.getResponsible()).getDescriptionShort()).compareTo(ApplicationHelper.getNotNull(ApplicationHelper.getNotNull(o2.getResponsible()).getDescriptionShort()));
                        } else if (colId.equalsIgnoreCase("signer")) {
                            result = ApplicationHelper.getNotNull(ApplicationHelper.getNotNull(o1.getSigner()).getDescriptionShort()).compareTo(ApplicationHelper.getNotNull(ApplicationHelper.getNotNull(o2.getSigner()).getDescriptionShort()));
                        } else if (colId.equalsIgnoreCase("initiator")) {
                            result = ApplicationHelper.getNotNull(ApplicationHelper.getNotNull(o1.getInitiator()).getDescriptionShort()).compareTo(ApplicationHelper.getNotNull(ApplicationHelper.getNotNull(o2.getInitiator()).getDescriptionShort()));
                        } else if (colId.equalsIgnoreCase("form")) {
                            result = ApplicationHelper.getNotNull(ApplicationHelper.getNotNull(o1.getForm()).toString()).compareTo(ApplicationHelper.getNotNull(ApplicationHelper.getNotNull(o2.getForm()).toString()));
                        } else if (colId.equalsIgnoreCase("status_id")) {
                            result = ApplicationHelper.getNotNull(ApplicationHelper.getNotNull(o1.getDocumentStatus()).getName()).compareTo(ApplicationHelper.getNotNull(ApplicationHelper.getNotNull(o2.getDocumentStatus()).getName()));
                        }

                        if (getSorting().isAsc()) {
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
         return new Long(sessionManagement.getDAO(InternalDocumentDAOImpl.class,INTERNAL_DOCUMENT_FORM_DAO).countAllDocumentsByUser(filter,
                 sessionManagement.getLoggedUser(), false, false)).intValue();
     }

     @Override
     protected List<InternalDocument> loadDocuments() {
         return sessionManagement.getDAO(InternalDocumentDAOImpl.class,INTERNAL_DOCUMENT_FORM_DAO).findAllDocumentsByUser(filter, sessionManagement.getLoggedUser(),
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
        filters.put("formValue", "Служебная записка");
        filters.put("formCategory", "Внутренние документы");
        filters.put("registrationNumber", "%");
    }

    @Inject
    @Named("sessionManagement")
    private transient SessionManagementBean sessionManagement;

    private static final long serialVersionUID = 8535420074467871583L;
}