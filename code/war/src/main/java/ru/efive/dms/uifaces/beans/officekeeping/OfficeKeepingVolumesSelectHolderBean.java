package ru.efive.dms.uifaces.beans.officekeeping;

import com.github.javaplugs.jsf.SpringScopeSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import ru.efive.uifaces.bean.AbstractDocumentListHolderBean;
import ru.efive.uifaces.bean.Pagination;
import ru.entity.model.document.OfficeKeepingVolume;
import ru.entity.model.enums.DocumentStatus;
import ru.hitsl.sql.dao.interfaces.OfficeKeepingVolumeDao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller("officeKeepingVolumesSelect")
@SpringScopeSession
public class OfficeKeepingVolumesSelectHolderBean extends AbstractDocumentListHolderBean<OfficeKeepingVolume> {

    static private Map<String, Object> filters = new HashMap<>();

    static {
        filters.put("statusId", DocumentStatus.OPEN.getId());
    }

    @Autowired
    @Qualifier("officeKeepingVolumeDao")
    private OfficeKeepingVolumeDao officeKeepingVolumeDao;

    private String filter;

    @Override
    protected Pagination initPagination() {
        return new Pagination(0, getTotalCount(), 100);
    }

    @Override
    protected int getTotalCount() {
        return officeKeepingVolumeDao.countItems(filter, filters, false);
    }

    @Override
    protected List<OfficeKeepingVolume> loadDocuments() {
        return officeKeepingVolumeDao.getItems(filter, filters, getSorting().getColumnId(), getSorting().isAsc(), getPagination().getOffset(), getPagination().getPageSize(), false);
    }

    @Override
    public List<OfficeKeepingVolume> getDocuments() {
        return super.getDocuments();
    }

    public List<OfficeKeepingVolume> getAllVolumesByParentFileId(String parentId) {
        List<OfficeKeepingVolume> result = new ArrayList<>();
        try {
            if (parentId != null && !parentId.equals("")) {
                result = officeKeepingVolumeDao.findAllVolumesByParentId(parentId);
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

    public void setOfficeKeepingVolume(OfficeKeepingVolume volume) {
        filters.put("officeKeepingVolume", volume);
    }

    public Map<String, Object> getFilters() {
        return filters;
    }

    public void setFilters(Map<String, Object> filters) {
        OfficeKeepingVolumesSelectHolderBean.filters = filters;
    }


}