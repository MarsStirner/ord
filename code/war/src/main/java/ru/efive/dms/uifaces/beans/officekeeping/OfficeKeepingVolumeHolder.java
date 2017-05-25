package ru.efive.dms.uifaces.beans.officekeeping;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentHolderBean;
import ru.efive.dms.uifaces.beans.annotations.ViewScopedController;
import ru.efive.dms.util.message.MessageUtils;
import ru.entity.model.document.HistoryEntry;
import ru.entity.model.document.OfficeKeepingFile;
import ru.entity.model.document.OfficeKeepingVolume;
import ru.entity.model.enums.DocumentStatus;
import ru.hitsl.sql.dao.interfaces.OfficeKeepingFileDao;
import ru.hitsl.sql.dao.interfaces.OfficeKeepingVolumeDao;
import ru.hitsl.sql.dao.util.AuthorizationData;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@ViewScopedController("officeKeepingVolume")
public class OfficeKeepingVolumeHolder extends AbstractDocumentHolderBean<OfficeKeepingVolume, OfficeKeepingVolumeDao> {


    @Autowired
    @Qualifier("officeKeepingFileDao")
    private OfficeKeepingFileDao officeKeepingFileDao;

    @Autowired
    public OfficeKeepingVolumeHolder(@Qualifier("officeKeepingVolumeDao") OfficeKeepingVolumeDao dao, @Qualifier("authData") AuthorizationData authData) {
        super(dao, authData);
    }

    @Override
    protected OfficeKeepingVolume newModel(AuthorizationData authData) {
        OfficeKeepingVolume document = new OfficeKeepingVolume();

        String parentId = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("parentId");
        if (StringUtils.isNotEmpty(parentId)) {
            final Integer parentIdentifier = Integer.valueOf(parentId);
            final OfficeKeepingFile file = officeKeepingFileDao.get(parentIdentifier);
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
        document.setStatus(DocumentStatus.NEW);
        return document;
    }





    @Override
    public boolean beforeCreate(OfficeKeepingVolume document, AuthorizationData authData) {
        if (document.getParentFile() == null) {
            //TODO add to MSGHolder
            MessageUtils.addMessage(new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Невозможно сохранить документ. Необходимо выбрать корректный документ Номенклатуры дел.", ""));
            return false;
        }
        LocalDateTime created = LocalDateTime.now();

        HistoryEntry historyEntry = new HistoryEntry();
        historyEntry.setCreated(created);
        historyEntry.setStartDate(created);
        historyEntry.setOwner(authData.getAuthorized());
        historyEntry.setDocType(document.getType().getName());
        historyEntry.setParentId(document.getId());
        historyEntry.setActionId(0);
        historyEntry.setFromStatusId(1);
        historyEntry.setEndDate(created);
        historyEntry.setProcessed(true);
        historyEntry.setCommentary("");
        Set<HistoryEntry> history = new HashSet<>();
        history.add(historyEntry);
        document.setHistory(history);
        return true;
    }


}