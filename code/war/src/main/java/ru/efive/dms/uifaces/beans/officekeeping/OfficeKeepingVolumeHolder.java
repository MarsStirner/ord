package ru.efive.dms.uifaces.beans.officekeeping;

import org.apache.commons.lang.StringUtils;
import ru.efive.dms.uifaces.beans.ProcessorModalBean;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentHolderBean;
import ru.efive.dms.uifaces.beans.user.UserSelectModalBean;
import ru.efive.wf.core.ActionResult;
import ru.entity.model.document.HistoryEntry;
import ru.entity.model.document.OfficeKeepingFile;
import ru.entity.model.document.OfficeKeepingVolume;
import ru.entity.model.enums.DocumentStatus;
import ru.entity.model.enums.RoleType;
import ru.hitsl.sql.dao.OfficeKeepingFileDAOImpl;
import ru.hitsl.sql.dao.OfficeKeepingVolumeDAOImpl;
import ru.util.ApplicationHelper;

import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

import static ru.efive.dms.uifaces.beans.utils.MessageHolder.*;
import static ru.hitsl.sql.dao.util.ApplicationDAONames.OFFICE_KEEPING_FILE_DAO;
import static ru.hitsl.sql.dao.util.ApplicationDAONames.OFFICE_KEEPING_VOLUME_DAO;

@Named("officeKeepingVolume")
@ConversationScoped
public class OfficeKeepingVolumeHolder extends AbstractDocumentHolderBean<OfficeKeepingVolume> implements Serializable {
    private static final long serialVersionUID = -7696075488442962088L;
    private boolean isRequisitesTabSelected = true;
    private boolean isDocumentsTabSelected = false;
    private boolean isLocationTabSelected = false;
    private boolean isHistoryTabSelected = false;

    @Inject
    @Named("sessionManagement")
    SessionManagementBean sessionManagement = new SessionManagementBean();

    @Override
    protected boolean deleteDocument() {
        boolean result = false;
        try {
            sessionManagement.getDAO(OfficeKeepingVolumeDAOImpl.class, OFFICE_KEEPING_VOLUME_DAO).delete(getDocument());
            result = true;
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, MSG_ERROR_ON_DELETE);
        }
        return result;
    }

    @Override
    protected void initDocument(Integer id) {
        setDocument(sessionManagement.getDAO(OfficeKeepingVolumeDAOImpl.class, OFFICE_KEEPING_VOLUME_DAO).get(id));

        if (getDocument() == null) {
            setDocumentNotFound();
        }
    }

    @Override
    protected void initNewDocument() {
        OfficeKeepingVolume document = new OfficeKeepingVolume();

        String parentId = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("parentId");
        if (StringUtils.isNotEmpty(parentId)) {
            final Integer parentIdentifier = Integer.valueOf(parentId);
            final OfficeKeepingFile file = sessionManagement.getDAO(OfficeKeepingFileDAOImpl.class, OFFICE_KEEPING_FILE_DAO)
                    .findDocumentById(parentIdentifier);
            if (file != null) {
                document.setParentFile(file);
                document.setShortDescription(file.getShortDescription());
                document.setComments(file.getComments());
                document.setKeepingPeriodReasons(file.getKeepingPeriodReasons());
                document.setFundNumber(file.getFundNumber());
                document.setBoxNumber(file.getBoxNumber());
                document.setShelfNumber(file.getShelfNumber());
                document.setStandNumber(file.getStandNumber());
                document.setLimitUnitsCount(250);
            }
        }
        document.setDocumentStatus(DocumentStatus.NEW);
        Date created = Calendar.getInstance(ApplicationHelper.getLocale()).getTime();

        HistoryEntry historyEntry = new HistoryEntry();
        historyEntry.setCreated(created);
        historyEntry.setStartDate(created);
        historyEntry.setOwner(sessionManagement.getLoggedUser());
        historyEntry.setDocType(document.getDocumentType().getName());
        historyEntry.setParentId(document.getId());
        historyEntry.setActionId(0);
        historyEntry.setFromStatusId(1);
        historyEntry.setEndDate(created);
        historyEntry.setProcessed(true);
        historyEntry.setCommentary("");
        Set<HistoryEntry> history = new HashSet<HistoryEntry>();
        history.add(historyEntry);
        document.setHistory(history);

        setDocument(document);
    }

    @Override
    protected boolean saveDocument() {
        boolean result = false;
        try {
            OfficeKeepingVolume record = sessionManagement.getDAO(OfficeKeepingVolumeDAOImpl.class, OFFICE_KEEPING_VOLUME_DAO).update((OfficeKeepingVolume) getDocument());
            if (record == null) {
                FacesContext.getCurrentInstance().addMessage(null, MSG_CANT_SAVE);
            } else {
                //setDocument(record);
                result = true;
            }
        } catch (Exception e) {
            result = false;
            FacesContext.getCurrentInstance().addMessage(null, MSG_ERROR_ON_SAVE);
            e.printStackTrace();
        }
        return result;
    }

    @Override
    protected boolean saveNewDocument() {
        try {
            OfficeKeepingVolume document = getDocument();
            OfficeKeepingFile file = document.getParentFile();
            if (file != null) {
                OfficeKeepingVolume record = sessionManagement.getDAO(OfficeKeepingVolumeDAOImpl.class, OFFICE_KEEPING_VOLUME_DAO).save(document);
                if (record == null) {
                    FacesContext.getCurrentInstance().addMessage(null, MSG_CANT_SAVE);
                } else {
                    return true;
                }
            } else {
                //TODO add to MSGHolder
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_ERROR,
                        "Невозможно сохранить документ. Необходимо выбрать корректный документ Номенклатуры дел.", ""));
            }
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, MSG_ERROR_ON_SAVE_NEW);
            e.printStackTrace();
        }
        return false;
    }


    public List<RoleType> getTypes() {
        List<RoleType> result = new ArrayList<RoleType>();
        for (RoleType type : RoleType.values()) {
            result.add(type);
        }
        return result;
    }

    public String getRequisitesTabHeader() {
        return "<span><span>Реквизиты</span></span>";
    }

    public boolean isRequisitesTabSelected() {
        return isRequisitesTabSelected;
    }

    public void setRequisitesTabSelected(boolean isRequisitesTabSelected) {
        this.isRequisitesTabSelected = isRequisitesTabSelected;
    }

    public String getLocationTabHeader() {
        return "<span><span>Местоположение</span></span>";
    }

    public boolean isLocationTabSelected() {
        return isLocationTabSelected;
    }

    public void setLocationTabSelected(boolean isLocationTabSelected) {
        this.isLocationTabSelected = isLocationTabSelected;
    }

    public String getDocumentsTabHeader() {
        return "<span><span>Документы</span></span>";
    }

    public boolean isDocumentsTabSelected() {
        return isDocumentsTabSelected;
    }

    public void setDocumentsTabSelected(boolean isDocumentsTabSelected) {
        this.isDocumentsTabSelected = isDocumentsTabSelected;
    }

    public String getHistoryTabHeader() {
        return "<span><span>История</span></span>";
    }

    public void setHistoryTabSelected(boolean isHistoryTabSelected) {
        this.isHistoryTabSelected = isHistoryTabSelected;
    }

    public boolean isHistoryTabSelected() {
        return isHistoryTabSelected;
    }

    public ProcessorModalBean getProcessorModal() {
        return processorModal;
    }

    public UserSelectModalBean getCollectorSelectModal() {
        return collectorSelectModal;
    }

    private ProcessorModalBean processorModal = new ProcessorModalBean() {

        @Override
        protected void doInit() {
            setProcessedData(getDocument());
            if (getDocumentId() == null || getDocumentId() == 0) saveNewDocument();
        }

        @Override
        protected void doPostProcess(ActionResult actionResult) {
            OfficeKeepingVolume document = (OfficeKeepingVolume) actionResult.getProcessedData();
            HistoryEntry historyEntry = getHistoryEntry();
            if (document.getDocumentStatus().equals(DocumentStatus.EXTRACT) && document.getCollector() != null) {
                historyEntry.setCommentary(document.getCollector().getDescriptionShort() + " : на руках до " + (new SimpleDateFormat("dd.MM.yyyy")).format(document.getReturnDate()));
            }
            if (getSelectedAction().isHistoryAction()) {
                Set<HistoryEntry> history = document.getHistory();
                if (history == null) {
                    history = new HashSet<HistoryEntry>();
                }
                history.add(historyEntry);
                document.setHistory(history);
            }
            setDocument(document);
            OfficeKeepingVolumeHolder.this.save();
        }

        @Override
        protected void doProcessException(ActionResult actionResult) {
            OfficeKeepingVolume document = (OfficeKeepingVolume) actionResult.getProcessedData();
            String in_result = document.getWFResultDescription();

            if (!in_result.equals("")) {
                setActionResult(in_result);
            }
        }
    };

    private UserSelectModalBean collectorSelectModal = new UserSelectModalBean() {
        @Override
        protected void doSave() {
            super.doSave();
            getDocument().setCollector(getUser());
        }

        @Override
        protected void doHide() {
            super.doHide();
            getUserList().setFilter("");
            getUserList().markNeedRefresh();
            setUser(null);
        }
    };

}