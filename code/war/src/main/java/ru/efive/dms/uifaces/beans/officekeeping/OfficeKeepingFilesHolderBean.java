package ru.efive.dms.uifaces.beans.officekeeping;

import ru.efive.dms.dao.OfficeKeepingFileDAOImpl;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.uifaces.bean.AbstractDocumentListHolderBean;
import ru.efive.uifaces.bean.Pagination;
import ru.entity.model.document.OfficeKeepingFile;
import ru.util.ApplicationHelper;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static ru.efive.dms.util.ApplicationDAONames.OFFICE_KEEPING_FILE_DAO;

@Named("officeKeepingFiles")
@SessionScoped
public class OfficeKeepingFilesHolderBean extends AbstractDocumentListHolderBean<OfficeKeepingFile> {

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
            result = new Long(sessionManagement.getDAO(OfficeKeepingFileDAOImpl.class, OFFICE_KEEPING_FILE_DAO).countDocument(false)).intValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    protected List<OfficeKeepingFile> loadDocuments() {
        List<OfficeKeepingFile> result = getAvailableOfficeKeepingFiles();
        Collections.sort(result, new Comparator<OfficeKeepingFile>() {
            public int compare(OfficeKeepingFile o1, OfficeKeepingFile o2) {
                int result = 0;
                String colId = getSorting().getColumnId();

                if (colId.equalsIgnoreCase("name")) {
                    result = ApplicationHelper.getNotNull(o1.getFileIndex()).compareTo(ApplicationHelper.getNotNull(o2.getFileIndex()));
                }

                if (getSorting().isAsc()) {
                    result *= -1;
                }
                return result;
            }
        });
        return result;
    }

    public List<OfficeKeepingFile> getAvailableOfficeKeepingFiles() {
        List<OfficeKeepingFile> result = new ArrayList<OfficeKeepingFile>();
        try {
            result = sessionManagement.getDAO(OfficeKeepingFileDAOImpl.class, OFFICE_KEEPING_FILE_DAO).findDocuments(false);
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