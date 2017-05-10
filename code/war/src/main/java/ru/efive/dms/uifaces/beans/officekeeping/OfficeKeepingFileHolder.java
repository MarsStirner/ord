package ru.efive.dms.uifaces.beans.officekeeping;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentHolderBean;
import ru.efive.dms.uifaces.beans.annotations.ViewScopedController;
import ru.entity.model.document.HistoryEntry;
import ru.entity.model.document.OfficeKeepingFile;
import ru.entity.model.enums.DocumentStatus;
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
        document.setDocumentStatus(DocumentStatus.NEW);

        LocalDateTime created = LocalDateTime.now();
        HistoryEntry historyEntry = new HistoryEntry();
        historyEntry.setCreated(created);
        historyEntry.setStartDate(created);
        historyEntry.setOwner(authData.getAuthorized());
        historyEntry.setDocType(document.getDocumentType().getName());
        historyEntry.setParentId(document.getId());
        historyEntry.setActionId(0);
        historyEntry.setFromStatusId(1);
        historyEntry.setEndDate(created);
        historyEntry.setProcessed(true);
        historyEntry.setCommentary("");
        Set<HistoryEntry> history = new HashSet<>();
        history.add(historyEntry);
        document.setHistory(history);
        return document;
    }
}