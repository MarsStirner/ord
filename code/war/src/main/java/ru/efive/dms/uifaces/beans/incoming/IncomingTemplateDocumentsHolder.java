package ru.efive.dms.uifaces.beans.incoming;

import ru.efive.dms.dao.IncomingDocumentDAOImpl;
import ru.efive.dms.dao.NumeratorDAOImpl;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.uifaces.bean.AbstractDocumentListHolderBean;
import ru.efive.uifaces.bean.Pagination;
import ru.entity.model.document.IncomingDocument;
import ru.entity.model.document.Numerator;
import ru.util.ApplicationHelper;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

import static ru.efive.dms.util.ApplicationDAONames.INCOMING_DOCUMENT_FORM_DAO;
import static ru.efive.dms.util.ApplicationDAONames.NUMERATOR_DAO;


@Named("in_template_documents")
@SessionScoped
public class IncomingTemplateDocumentsHolder extends AbstractDocumentListHolderBean<IncomingDocument> {

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
        return this.getHashDocuments().subList(fromIndex, toIndex);
    }

    protected List<IncomingDocument> getHashDocuments() {
        List<IncomingDocument> result = new ArrayList<IncomingDocument>();
        if (needRefresh) {
            try {
                result = new ArrayList<IncomingDocument>(new HashSet<IncomingDocument>(sessionManagement.getDAO(IncomingDocumentDAOImpl.class,
                        INCOMING_DOCUMENT_FORM_DAO).findAllDocuments(filters, filter, false, true)));

                Collections.sort(result, new Comparator<IncomingDocument>() {
                    public int compare(IncomingDocument o1, IncomingDocument o2) {
                        int result = 0;
                        String colId = ApplicationHelper.getNotNull(getSorting().getColumnId());

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

    public List<IncomingDocument> getTemplateDocumentsByNumeratorId(String key) {
        System.out.println(">>" + key);
        List<IncomingDocument> result = new ArrayList<IncomingDocument>();
        if (!key.isEmpty()) {
            Numerator numerator = sessionManagement.getDAO(NumeratorDAOImpl.class, NUMERATOR_DAO).findDocumentById(key);
            if (numerator != null) {
                System.out.println("-->>" + numerator.getCreationDate());
                if (numerator.getDocumentTypeKey().equals("incoming")) {
                    IncomingDocument in_doc = sessionManagement.getDAO(IncomingDocumentDAOImpl.class, INCOMING_DOCUMENT_FORM_DAO).findDocumentByNumeratorId(key);
                    result.add(in_doc);
                }
            } else {
                System.out.println("<<--");
            }
        }
        return result;
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
    static private Map<String, Object> filters = new HashMap<String, Object>();

    static {
        filters.put("parentNumeratorId", "%");
    }

    @Inject
    @Named("sessionManagement")
    private transient SessionManagementBean sessionManagement;

    private static final long serialVersionUID = 8535420074467871583L;


}