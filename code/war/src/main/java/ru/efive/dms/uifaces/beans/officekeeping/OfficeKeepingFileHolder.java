package ru.efive.dms.uifaces.beans.officekeeping;

import com.github.javaplugs.jsf.SpringScopeView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.ProcessorModalBean;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentHolderBean;
import ru.efive.wf.core.ActionResult;
import ru.entity.model.document.HistoryEntry;
import ru.entity.model.document.OfficeKeepingFile;
import ru.entity.model.enums.DocumentStatus;
import ru.entity.model.enums.RoleType;
import ru.hitsl.sql.dao.interfaces.OfficeKeepingFileDao;
import ru.hitsl.sql.dao.util.AuthorizationData;

import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.*;

import static ru.efive.dms.uifaces.beans.utils.MessageHolder.*;

@Controller("officeKeepingFile")
@SpringScopeView
public class OfficeKeepingFileHolder extends AbstractDocumentHolderBean<OfficeKeepingFile> {

    private static final long serialVersionUID = 5947443099767481905L;
    @Autowired
    @Qualifier("officeKeepingFileDao")
    private OfficeKeepingFileDao officeKeepingFileDao;

    @Autowired
    @Qualifier("authData")
    private AuthorizationData authData;

    private ProcessorModalBean processorModal = new ProcessorModalBean() {

        @Override
        protected void doInit() {
            setProcessedData(getDocument());
            if (getDocumentId() == null || getDocumentId() == 0) saveNewDocument();
        }

        @Override
        protected void doPostProcess(ActionResult actionResult) {
            OfficeKeepingFile document = (OfficeKeepingFile) actionResult.getProcessedData();
            if (getSelectedAction().isHistoryAction()) {
                Set<HistoryEntry> history = document.getHistory();
                if (history == null) {
                    history = new HashSet<>();
                }
                history.add(getHistoryEntry());
                document.setHistory(history);
            }
            setDocument(document);
            OfficeKeepingFileHolder.this.save();
        }

        @Override
        protected void doProcessException(ActionResult actionResult) {
            OfficeKeepingFile document = (OfficeKeepingFile) actionResult.getProcessedData();
            String in_result = document.getWFResultDescription();

            if (!in_result.equals("")) {
                setActionResult(in_result);
            }
        }
    };

    @Override
    protected boolean deleteDocument() {
        boolean result = false;
        try {
            officeKeepingFileDao.delete(getDocument());
            result = true;
        } catch (Exception e) {
            addMessage(null, MSG_CANT_DELETE);
        }
        return result;
    }

    @Override
    protected void initDocument(Integer id) {
        setDocument(officeKeepingFileDao.get(id));
        if (getDocument() == null) {
            setDocumentNotFound();
        }
    }

    @Override
    protected void initNewDocument() {
        OfficeKeepingFile document = new OfficeKeepingFile();
        document.setDocumentStatus(DocumentStatus.NEW);
        LocalDateTime created = LocalDateTime.now();
        //document.setCreationDate(created);
        //document.setAuthor(authData.getAuthorized());

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

        setDocument(document);
    }

    @Override
    protected boolean saveDocument() {
        boolean result = false;
        try {
            OfficeKeepingFile record = officeKeepingFileDao.update(getDocument());
            if (record == null) {
                addMessage(null, MSG_CANT_SAVE);
            } else {
                setDocument(record);
                result = true;
            }
        } catch (Exception e) {
            result = false;
            addMessage(null, MSG_ERROR_ON_SAVE);
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public boolean saveNewDocument() {
        boolean result = false;
        try {
            OfficeKeepingFile record = officeKeepingFileDao.save(getDocument());
            if (record == null) {
                addMessage(null, MSG_CANT_SAVE);
            } else {
                setDocument(record);
                result = true;
            }
        } catch (Exception e) {
            result = false;
            addMessage(null, MSG_ERROR_ON_SAVE_NEW);
            e.printStackTrace();
        }
        return result;
    }

    public List<RoleType> getTypes() {
        List<RoleType> result = new ArrayList<>();
        Collections.addAll(result, RoleType.values());
        return result;
    }

    public ProcessorModalBean getProcessorModal() {
        return processorModal;
    }
}