package ru.efive.dms.uifaces.beans.officekeeping;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import ru.efive.sql.entity.enums.RoleType;
import ru.efive.dms.dao.OfficeKeepingRecordDAOImpl;
import ru.efive.dms.data.OfficeKeepingRecord;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.util.ApplicationHelper;
import ru.efive.uifaces.bean.AbstractDocumentHolderBean;
import ru.efive.uifaces.bean.FromStringConverter;

@Named("officeKeepingRecord")
@ConversationScoped
public class OfficeKeepingRecordHolderBean extends AbstractDocumentHolderBean<OfficeKeepingRecord, Integer> implements Serializable {

    @Override
    protected boolean deleteDocument() {
        boolean result = false;
        try {
            sessionManagement.getDAO(OfficeKeepingRecordDAOImpl.class, ApplicationHelper.OFFICE_KEEPING_RECORD_DAO).delete(getDocument());
            result = true;
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Невозможно удалить документ. Попробуйте повторить позже.", ""));
        }
        return result;
    }

    @Override
    protected Integer getDocumentId() {
        return getDocument().getId();
    }

    @Override
    protected FromStringConverter<Integer> getIdConverter() {
        return FromStringConverter.INTEGER_CONVERTER;
    }

    @Override
    protected void initDocument(Integer id) {
        setDocument(sessionManagement.getDAO(OfficeKeepingRecordDAOImpl.class, ApplicationHelper.OFFICE_KEEPING_RECORD_DAO).get(id));
        if (getDocument() == null) {
            setState(STATE_NOT_FOUND);
        }
    }

    @Override
    protected void initNewDocument() {
        setDocument(new OfficeKeepingRecord());
    }

    @Override
    protected boolean saveDocument() {
        boolean result = false;
        try {
            OfficeKeepingRecord record = sessionManagement.getDAO(OfficeKeepingRecordDAOImpl.class, ApplicationHelper.OFFICE_KEEPING_RECORD_DAO).update(getDocument());
            if (record == null) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_ERROR,
                        "Невозможно сохранить документ. Попробуйте повторить позже.", ""));
            } else {
                setDocument(record);
                result = true;
            }
        } catch (Exception e) {
            result = false;
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Ошибка при сохранении документа.", ""));
            e.printStackTrace();
        }
        return result;
    }

    @Override
    protected boolean saveNewDocument() {
        boolean result = false;
        try {
            OfficeKeepingRecord record = sessionManagement.getDAO(OfficeKeepingRecordDAOImpl.class, ApplicationHelper.OFFICE_KEEPING_RECORD_DAO).save(getDocument());
            if (record == null) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_ERROR,
                        "Невозможно сохранить документ. Попробуйте повторить позже.", ""));
            } else {
                setDocument(record);
                result = true;
            }
        } catch (Exception e) {
            result = false;
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Ошибка при сохранении документа.", ""));
            e.printStackTrace();
        }
        return result;
    }

    @Override
    protected String doAfterCreate() {
        officeKeepingRecordList.markNeedRefresh();
        return super.doAfterCreate();
    }

    @Override
    protected String doAfterDelete() {
        officeKeepingRecordList.markNeedRefresh();
        return super.doAfterDelete();
    }

    @Override
    protected String doAfterSave() {
        officeKeepingRecordList.markNeedRefresh();
        return super.doAfterSave();
    }

    public List<RoleType> getTypes() {
        List<RoleType> result = new ArrayList<RoleType>();
        for (RoleType type : RoleType.values()) {
            result.add(type);
        }
        return result;
    }


    @Inject
    @Named("sessionManagement")
    SessionManagementBean sessionManagement = new SessionManagementBean();
    @Inject
    @Named("officeKeepingRecordList")
    OfficeKeepingRecordListHolderBean officeKeepingRecordList = new OfficeKeepingRecordListHolderBean();

    private static final long serialVersionUID = 5947443099767481905L;
}