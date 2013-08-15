package ru.efive.dms.uifaces.beans.officekeeping;

import ru.efive.dms.dao.OfficeKeepingVolumeDAOImpl;
import ru.efive.dms.data.OfficeKeepingVolume;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.util.ApplicationHelper;
import ru.efive.uifaces.bean.AbstractDocumentListHolderBean;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

@Named("officeKeepingVolumes")
@SessionScoped
public class OfficeKeepingVolumesHolderBean extends AbstractDocumentListHolderBean<OfficeKeepingVolume> {

    @Override
    protected Pagination initPagination() {
        return new Pagination(0, getTotalCount(), 100);
    }

    @Override
    protected int getTotalCount() {
        int in_result;
        in_result = new Long(sessionManagement.getDAO(OfficeKeepingVolumeDAOImpl.class, ApplicationHelper.OFFICE_KEEPING_VOLUME_DAO).countAllDocuments(filters, filter, false, false)).intValue();
        return in_result;
    }

    @Override
    protected List<OfficeKeepingVolume> loadDocuments() {
        List<OfficeKeepingVolume> result = new ArrayList<OfficeKeepingVolume>(new HashSet<OfficeKeepingVolume>(sessionManagement.getDAO(OfficeKeepingVolumeDAOImpl.class, ApplicationHelper.OFFICE_KEEPING_VOLUME_DAO).findAllDocuments(filters, filter, false, false)));

        Collections.sort(result, new Comparator<OfficeKeepingVolume>() {
            public int compare(OfficeKeepingVolume o1, OfficeKeepingVolume o2) {
                int result = 0;
                String colId = getSorting().getColumnId();

                if (colId.equalsIgnoreCase("volumeIndex")) {
                    result = ApplicationHelper.getNotNull(o1.getVolumeIndex()).compareTo(ApplicationHelper.getNotNull(o2.getVolumeIndex()));
                }

                if (getSorting().isAsc()) {
                    result *= -1;
                }
                return result;
            }
        });

/*
		Collections.sort(result, new Comparator<Task>() {
			@Override
			public int compare(Task o1, Task o2) {
				Calendar c1 = Calendar.getInstance();
				Calendar c2 = Calendar.getInstance();
				c1.setTime(o1.getCreationDate());					
				c2.setTime(o2.getCreationDate());				
				int in_result = (-1)*c1.compareTo(c2);
				return in_result;
			}
		});
*/
        return result;
    }

    @Override
    public List<OfficeKeepingVolume> getDocuments() {
        return super.getDocuments();
    }

    public List<OfficeKeepingVolume> getAllVolumesByParentFileId(String parentId) {
        List<OfficeKeepingVolume> result = new ArrayList<OfficeKeepingVolume>();
        try {
            if (parentId != null && !parentId.equals("")) {
                result = sessionManagement.getDAO(OfficeKeepingVolumeDAOImpl.class, ApplicationHelper.OFFICE_KEEPING_VOLUME_DAO).findAllVolumesByParentId(parentId);
            }
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

    public void setFilters(Map<String, Object> filters) {
        this.filters = filters;
    }

    public Map<String, Object> getFilters() {
        return filters;
    }

    private String filter;
    static private Map<String, Object> filters = new HashMap<String, Object>();

    static {
        filters.put("volumeIndex", "%");
    }

    @Inject
    @Named("sessionManagement")
    SessionManagementBean sessionManagement = new SessionManagementBean();

    private static final long serialVersionUID = 1426067769816981240L;
}