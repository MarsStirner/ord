package ru.efive.dms.uifaces.beans.internal;

import ru.efive.dms.dao.InternalDocumentDAOImpl;
import ru.efive.dms.data.InternalDocument;
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

/**
 * @author Nastya Peshekhonova
 */

@Named("internalDocumentsOnAgreement")
@SessionScoped
public class InternalDocumentsOnAgreementHolder extends AbstractDocumentListHolderBean<InternalDocument> {

    private static final String beanName = "internalDocumentsOnAgreement";
    private List<InternalDocument> hashDocuments;
    private boolean needRefresh = true;
    static private Map<String, Object> filters = new HashMap<String, Object>() {{
        put("statusId", DocumentStatus.AGREEMENT_3.getId());
    }};
    private String filter;

    @Inject
    @Named("sessionManagement")
    private transient SessionManagementBean sessionManagement;

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.needRefresh = true;
        this.filter = filter;
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
    protected Pagination initPagination() {
        return new Pagination(0, getTotalCount(), 100);
    }

    @Override
    protected Sorting initSorting() {
        return new Sorting("registrationDate", false);
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
        return super.getDocuments();
    }

    protected List<InternalDocument> getHashDocuments(int fromIndex, int toIndex) {
        this.hashDocuments = getHashDocuments();
        toIndex = (hashDocuments.size() < fromIndex + toIndex) ? this.hashDocuments.size() : fromIndex + toIndex;
        return new ArrayList<InternalDocument>(hashDocuments.subList(fromIndex, toIndex));
    }

    protected List<InternalDocument> getHashDocuments() {
        List<InternalDocument> result = hashDocuments;
        if (needRefresh) {
            sessionManagement.registrateBeanName(beanName);
            try {
                User user = sessionManagement.getDAO(UserDAOHibernate.class, ApplicationHelper.USER_DAO).
                        findByLoginAndPassword(sessionManagement.getLoggedUser().getLogin(), sessionManagement.getLoggedUser().getPassword());

                result = new ArrayList<InternalDocument>(new HashSet<InternalDocument>(sessionManagement.getDAO(InternalDocumentDAOImpl.class,
                        ApplicationHelper.INTERNAL_DOCUMENT_FORM_DAO).findAllDocumentsByUser(filters, filter, user, false, false)));

                Collections.sort(result, new Comparator<InternalDocument>() {
                    public int compare(InternalDocument o1, InternalDocument o2) {
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
                            Calendar c1 = Calendar.getInstance(ApplicationHelper.getLocale());
                            c1.setTime(ApplicationHelper.getNotNull(o1.getRegistrationDate()));
                            Calendar c2 = Calendar.getInstance(ApplicationHelper.getLocale());
                            c2.setTime(ApplicationHelper.getNotNull(o2.getRegistrationDate()));
                            result = c2.compareTo(c1);
                        } else if(colId.equalsIgnoreCase("form")) {
                            result = ApplicationHelper.getNotNull(ApplicationHelper.getNotNull(o1.getForm()).toString()).compareTo(ApplicationHelper.getNotNull(ApplicationHelper.getNotNull(o2.getForm()).toString()));
                        } else if(colId.equalsIgnoreCase("status_id")) {
                            result = ApplicationHelper.getNotNull(ApplicationHelper.getNotNull(o1.getDocumentStatus()).getName()).compareTo(ApplicationHelper.getNotNull(ApplicationHelper.getNotNull(o2.getDocumentStatus()).getName()));
                        }


                        if(getSorting().isAsc()) {
                            result *= -1;
                        }
                        return result;
                    }
                });

                hashDocuments = result;
                needRefresh = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}
