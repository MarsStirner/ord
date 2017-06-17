package ru.efive.dms.uifaces.beans.officekeeping;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentHolderBean;
import ru.efive.dms.uifaces.beans.annotations.ViewScopedController;
import ru.entity.model.workflow.HistoryEntry;
import ru.entity.model.document.OfficeKeepingFile;

import ru.hitsl.sql.dao.interfaces.OfficeKeepingFileDao;
import ru.hitsl.sql.dao.util.AuthorizationData;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@ViewScopedController("officeKeepingFile")
public class OfficeKeepingFileHolder extends AbstractDocumentHolderBean<OfficeKeepingFile, OfficeKeepingFileDao> {
    @Autowired
    public OfficeKeepingFileHolder(@Qualifier("officeKeepingFileDao") OfficeKeepingFileDao dao, @Qualifier("authData") AuthorizationData authData) {
        super(dao, authData);
    }

    @Override
    protected OfficeKeepingFile newModel(AuthorizationData authData) {
        OfficeKeepingFile document = new OfficeKeepingFile();
             return document;
    }

}