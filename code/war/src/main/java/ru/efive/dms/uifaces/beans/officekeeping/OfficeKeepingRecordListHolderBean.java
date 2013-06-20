package ru.efive.dms.uifaces.beans.officekeeping;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import ru.efive.dms.dao.OfficeKeepingRecordDAOImpl;
import ru.efive.dms.data.OfficeKeepingRecord;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.util.ApplicationHelper;
import ru.efive.uifaces.bean.AbstractDocumentListHolderBean;

@Named("officeKeepingRecordList")
@SessionScoped
public class OfficeKeepingRecordListHolderBean extends AbstractDocumentListHolderBean<OfficeKeepingRecord> {

    @Override
    public Pagination initPagination() {
        return new Pagination(0, getTotalCount(), 25);
    }

    @Override
    protected Sorting initSorting() {
        return new Sorting("name", true);
    }

    @Override
    protected int getTotalCount() {
        int result = 0;
        try {
            result = new Long(sessionManagement.getDAO(OfficeKeepingRecordDAOImpl.class, ApplicationHelper.OFFICE_KEEPING_RECORD_DAO).countDocument(false)).intValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    protected List<OfficeKeepingRecord> loadDocuments() {
        List<OfficeKeepingRecord> result = new ArrayList<OfficeKeepingRecord>();
        try {
            result = sessionManagement.getDAO(OfficeKeepingRecordDAOImpl.class, ApplicationHelper.OFFICE_KEEPING_RECORD_DAO).findDocuments(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public List<OfficeKeepingRecord> getAvailableOfficeKeepingRecords() {
        List<OfficeKeepingRecord> result = new ArrayList<OfficeKeepingRecord>();
        try {
            result = sessionManagement.getDAO(OfficeKeepingRecordDAOImpl.class, ApplicationHelper.OFFICE_KEEPING_RECORD_DAO).findDocuments(false);
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