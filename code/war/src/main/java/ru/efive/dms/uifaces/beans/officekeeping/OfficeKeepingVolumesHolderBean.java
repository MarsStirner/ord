package ru.efive.dms.uifaces.beans.officekeeping;

import com.github.javaplugs.jsf.SpringScopeView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentLazyDataModelBean;
import ru.efive.dms.uifaces.lazyDataModel.LazyDataModelForOfficeKeepingVolume;
import ru.entity.model.document.OfficeKeepingVolume;

import org.springframework.stereotype.Controller;

@Controller("officeKeepingVolumes")
@SpringScopeView
public class OfficeKeepingVolumesHolderBean extends AbstractDocumentLazyDataModelBean<OfficeKeepingVolume> {
    @Autowired
    public void setLazyModel(  @Qualifier("officeKeepingVolumeLDM")LazyDataModelForOfficeKeepingVolume officeKeepingVolumes) {
        super.setLazyModel(officeKeepingVolumes);
    }

}