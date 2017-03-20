package ru.efive.dms.uifaces.beans.officekeeping;

import org.apache.commons.lang.StringUtils;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentLazyDataModelBean;
import ru.efive.dms.uifaces.lazyDataModel.LazyDataModelForOfficeKeepingVolume;
import ru.entity.model.document.OfficeKeepingVolume;
import ru.hitsl.sql.dao.OfficeKeepingVolumeDAOImpl;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

import static ru.hitsl.sql.dao.util.ApplicationDAONames.OFFICE_KEEPING_VOLUME_DAO;

@Named("officeKeepingVolumes")
@ViewScoped
public class OfficeKeepingVolumesHolderBean extends AbstractDocumentLazyDataModelBean<OfficeKeepingVolume> {

    @Inject
    @Named("sessionManagement")
    private transient SessionManagementBean sessionManagement;

    @PostConstruct
    public void init() {
        final OfficeKeepingVolumeDAOImpl dao = sessionManagement.getDAO(OfficeKeepingVolumeDAOImpl.class, OFFICE_KEEPING_VOLUME_DAO);
        setLazyModel(new LazyDataModelForOfficeKeepingVolume(dao));
    }

    public List<OfficeKeepingVolume> getAllVolumesByParentFileId(String parentId) {
        List<OfficeKeepingVolume> result = new ArrayList<>();
        try {
            if (StringUtils.isNotEmpty(parentId)) {
                result = sessionManagement.getDAO(OfficeKeepingVolumeDAOImpl.class, OFFICE_KEEPING_VOLUME_DAO).findAllVolumesByParentId(parentId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

}