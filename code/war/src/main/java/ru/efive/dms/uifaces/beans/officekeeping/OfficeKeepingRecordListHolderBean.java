package ru.efive.dms.uifaces.beans.officekeeping;

import ru.efive.dms.dao.OfficeKeepingRecordDAOImpl;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.uifaces.bean.AbstractDocumentListHolderBean;
import ru.entity.model.document.OfficeKeepingRecord;
import ru.util.ApplicationHelper;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static ru.efive.dms.util.ApplicationDAONames.OFFICE_KEEPING_RECORD_DAO;

@Named("officeKeepingRecordList")
@SessionScoped
public class OfficeKeepingRecordListHolderBean extends AbstractDocumentListHolderBean<OfficeKeepingRecord> {

    @Override
    public Pagination initPagination() {
        return new Pagination(0, getTotalCount(), 25);
    }

    @Override
    protected Sorting initSorting() {
        return new Sorting("name", false);
    }

    @Override
    protected int getTotalCount() {
        int result = 0;
        try {
            result = new Long(sessionManagement.getDAO(OfficeKeepingRecordDAOImpl.class, OFFICE_KEEPING_RECORD_DAO).countDocument(false)).intValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    protected List<OfficeKeepingRecord> loadDocuments() {
        List<OfficeKeepingRecord> result = getAvailableOfficeKeepingRecords();
        Collections.sort(result, new Comparator<OfficeKeepingRecord>() {
            public int compare(OfficeKeepingRecord o1, OfficeKeepingRecord o2) {
                int result = 0;
                String colId = getSorting().getColumnId();
                if (colId.equalsIgnoreCase("name")) {
                    result = ApplicationHelper.getNotNull(o1.getRecordIndex()).compareTo(ApplicationHelper.getNotNull(o2.getRecordIndex()));
                }
                if (getSorting().isAsc()) {
                    result *= -1;
                }
                return result;
            }
        });
        return result;
    }

    public List<OfficeKeepingRecord> getAvailableOfficeKeepingRecords() {
        List<OfficeKeepingRecord> result = new ArrayList<OfficeKeepingRecord>();
        try {
            result = sessionManagement.getDAO(OfficeKeepingRecordDAOImpl.class, OFFICE_KEEPING_RECORD_DAO).findDocuments(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    @Inject
    @Named("sessionManagement")
    SessionManagementBean sessionManagement = new SessionManagementBean();

    private String filter;

    private static final long serialVersionUID = 1023130636261147049L;
}