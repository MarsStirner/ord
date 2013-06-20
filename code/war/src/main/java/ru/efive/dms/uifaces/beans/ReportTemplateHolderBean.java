package ru.efive.dms.uifaces.beans;

import java.io.Serializable;
import java.util.Date;

import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import ru.efive.dms.dao.ReportDAOImpl;
import ru.efive.dms.data.Region;
import ru.efive.dms.data.ReportTemplate;
import ru.efive.dms.uifaces.beans.user.UserSelectModalBean;
import ru.efive.dms.util.ApplicationHelper;
import ru.efive.uifaces.bean.AbstractDocumentHolderBean;
import ru.efive.uifaces.bean.FromStringConverter;
import ru.efive.uifaces.bean.ModalWindowHolderBean;

@Named("reportTemplate")
@ConversationScoped
public class ReportTemplateHolderBean extends AbstractDocumentHolderBean<ReportTemplate, Integer> implements Serializable {

    @Override
    protected boolean deleteDocument() {
        boolean result = false;
        try {
            sessionManagement.getDAO(ReportDAOImpl.class, ApplicationHelper.REPORT_DAO).delete(getDocument());
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
        ReportTemplate reportTemplate = sessionManagement.getDAO(ReportDAOImpl.class, ApplicationHelper.REPORT_DAO).get(id);
        reportTemplate.setStartDate(new Date());
        reportTemplate.setEndDate(new Date());
        setDocument(reportTemplate);
        if (getDocument() == null) {
            setState(STATE_NOT_FOUND);
        } else {
            setState(STATE_EDIT);
        }
    }

    @Override
    protected void initNewDocument() {
        ReportTemplate template = new ReportTemplate();
        template.setDeleted(false);
        setDocument(template);
    }

    @Override
    protected boolean saveDocument() {
        boolean result = false;
        try {
            reportsManagement.sqlPrintReportByRequestParams(getDocument());
            result = true;
        } catch (Exception e) {
            result = false;
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ошибка при формировании отчета.", ""));
            e.printStackTrace();
        }
        return result;
    }

    @Override
    protected boolean saveNewDocument() {
        boolean result = false;
        try {
            ReportTemplate template = sessionManagement.getDAO(ReportDAOImpl.class, ApplicationHelper.REPORT_DAO).save(getDocument());
            if (template == null) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_ERROR,
                        "Невозможно сохранить документ. Попробуйте повторить позже.", ""));
            } else {
                setDocument(template);
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

    /* ------------------------------------------------------------------------------------------------*/
    /* --------------------------- Выбор пользователя -------------------------------------------------*/
    /* ------------------------------------------------------------------------------------------------*/
    public void setUserSelectModal(UserSelectModalBean userSelectModal) {
        this.userSelectModal = userSelectModal;
    }

    public UserSelectModalBean getUserSelectModal() {
        return userSelectModal;
    }

    private UserSelectModalBean userSelectModal = new UserSelectModalBean() {
        @Override
        protected void doSave() {
            super.doSave();
            getDocument().setUser(getUser());
        }

        @Override
        protected void doHide() {
            super.doHide();
            getUserList().setFilter("");
            getUserList().markNeedRefresh();
            setUser(null);
        }
    };
    /* ------------------------------------------------------------------------------------------------*/

    /* ------------------------------------------------------------------------------------------------*/
    /* --------------------------- Выбор региона ------------------------------------------------------*/
    /* ------------------------------------------------------------------------------------------------*/
    public class RegionSelectModal extends ModalWindowHolderBean {

        private static final long serialVersionUID = -7963682960194163165L;

        public Region getValue() {
            return value;
        }

        public void setValue(Region value) {
            this.value = value;
        }

        public boolean selected(Region value) {
            return this.value != null ? this.value.getValue().equals(value.getValue()) : false;
        }

        public void select(Region value) {
            this.value = value;
        }

        public void unselect() {
            this.value = null;
        }

        @Override
        protected void doSave() {
            super.doSave();
            getDocument().setRegion(getValue());
        }

        @Override
        protected void doShow() {
            super.doShow();
        }

        @Override
        protected void doHide() {
            super.doHide();
            value = null;
        }

        private Region value;

    }

    public RegionSelectModal getRegionSelectModal() {
        return regionSelectModal;
    }

    private RegionSelectModal regionSelectModal = new RegionSelectModal();
    /* ------------------------------------------------------------------------------------------------*/

    @Inject
    @Named("sessionManagement")
    SessionManagementBean sessionManagement = new SessionManagementBean();

    @Inject
    @Named("reports")
    ReportsManagmentBean reportsManagement = new ReportsManagmentBean();

    private static final long serialVersionUID = 5947443099767481905L;
}