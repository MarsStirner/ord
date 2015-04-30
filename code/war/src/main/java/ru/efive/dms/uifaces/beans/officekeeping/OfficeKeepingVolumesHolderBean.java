package ru.efive.dms.uifaces.beans.officekeeping;

import ru.efive.dms.dao.OfficeKeepingVolumeDAOImpl;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentLazyDataModelBean;
import ru.efive.dms.uifaces.lazyDataModel.LazyDataModelForOfficeKeepingVolume;
import ru.entity.model.document.OfficeKeepingVolume;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import static ru.efive.dms.util.ApplicationDAONames.OFFICE_KEEPING_VOLUME_DAO;

@ManagedBean(name="officeKeepingVolumes")
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
}