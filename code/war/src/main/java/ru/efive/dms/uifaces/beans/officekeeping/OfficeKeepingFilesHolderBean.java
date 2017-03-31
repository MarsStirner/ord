package ru.efive.dms.uifaces.beans.officekeeping;

import com.github.javaplugs.jsf.SpringScopeView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentLazyDataModelBean;
import ru.efive.dms.uifaces.lazyDataModel.LazyDataModelForOfficeKeepingFile;
import ru.entity.model.document.OfficeKeepingFile;

import org.springframework.stereotype.Controller;

@Controller("officeKeepingFiles")
@SpringScopeView
public class OfficeKeepingFilesHolderBean extends AbstractDocumentLazyDataModelBean<OfficeKeepingFile> {

    @Autowired
    @Qualifier("officeKeepingFileLDM")
    private LazyDataModelForOfficeKeepingFile lazyDataModelForOfficeKeepingFile;

}