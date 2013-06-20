package ru.efive.dms.uifaces.beans;

import ru.efive.sql.dao.user.UserDAOHibernate;
import ru.efive.sql.entity.enums.DocumentStatus;
import ru.efive.sql.entity.enums.RoleType;
import ru.efive.sql.entity.user.Role;
import ru.efive.sql.entity.user.User;
import ru.efive.dms.dao.ScanCopyDocumentDAOImpl;
import ru.efive.dms.data.HistoryEntry;
import ru.efive.dms.data.ScanCopyDocument;
import ru.efive.dms.util.ApplicationHelper;
import ru.efive.uifaces.bean.AbstractDocumentHolderBean;
import ru.efive.uifaces.bean.FromStringConverter;

import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.*;

/**
 * @author Nastya Peshekhonova
 */
@Named("scan_copy_document")
@ConversationScoped
public class ScanDocumentHolder extends AbstractDocumentHolderBean<ScanCopyDocument, Integer> implements Serializable {
    @Inject
    @Named("sessionManagement")
    private transient SessionManagementBean sessionManagement;
    @Inject
    @Named("scan_documents")
    private transient ScanDocumentsHolder scanDocumentsHolder;

    private transient String stateComment;
    private boolean isRequisitesTabSelected = true;
    private boolean isHistoryTabSelected = false;

    public void setStateComment(String stateComment) {
        this.stateComment = stateComment;
    }

    public String getStateComment() {
        return stateComment;
    }

    public boolean isRequisitesTabSelected() {
        return isRequisitesTabSelected;
    }

    public void setRequisitesTabSelected(boolean isRequisitesTabSelected) {
        this.isRequisitesTabSelected = isRequisitesTabSelected;
    }

    public String getRequisitesTabHeader() {
        return "<span><span>Реквизиты</span></span>";
    }

    public void setHistoryTabSelected(boolean isHistoryTabSelected) {
        this.isHistoryTabSelected = isHistoryTabSelected;
    }

    public boolean isHistoryTabSelected() {
        return isHistoryTabSelected;
    }

    public String getHistoryTabHeader() {
        return "<span><span>История</span></span>";
    }


    @Override
    protected String doAfterCreate() {
        scanDocumentsHolder.markNeedRefresh();
        return super.doAfterCreate();
    }

    @Override
    protected String doAfterEdit() {
        scanDocumentsHolder.markNeedRefresh();
        return super.doAfterEdit();
    }

    @Override
    protected String doAfterDelete() {
        scanDocumentsHolder.markNeedRefresh();
        return super.doAfterDelete();
    }

    @Override
    protected String doAfterSave() {
        scanDocumentsHolder.markNeedRefresh();
        return super.doAfterSave();
    }


    @Override
    protected boolean deleteDocument() {
        boolean result = false;
        try {
            result = sessionManagement.getDAO(ScanCopyDocumentDAOImpl.class, ApplicationHelper.SCAN_DAO).delete(getDocumentId());
            if (!result) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_ERROR,
                        "Невозможно удалить документ. Попробуйте повторить позже.", ""));
            }
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Внутренняя ошибка при удалении.", ""));
        }
        return result;
    }

    @Override
    protected Integer getDocumentId() {
        return getDocument() == null ? null : getDocument().getId();
    }

    @Override
    protected void initNewDocument() {
        ScanCopyDocument scanCopyDocument = new ScanCopyDocument();
        scanCopyDocument.setDocumentStatus(DocumentStatus.NEW);
        Date created = Calendar.getInstance(ApplicationHelper.getLocale()).getTime();
        scanCopyDocument.setCreationDate(created);
        scanCopyDocument.setAuthor(sessionManagement.getLoggedUser());

        HistoryEntry historyEntry = new HistoryEntry();
        historyEntry.setCreated(created);
        historyEntry.setStartDate(created);
        historyEntry.setOwner(sessionManagement.getLoggedUser());
        historyEntry.setDocType(scanCopyDocument.getType());
        historyEntry.setParentId(scanCopyDocument.getId());
        historyEntry.setActionId(0);
        historyEntry.setFromStatusId(1);
        historyEntry.setEndDate(created);
        historyEntry.setProcessed(true);
        historyEntry.setCommentary("");
        Set<HistoryEntry> history = new HashSet<HistoryEntry>();
        history.add(historyEntry);
        scanCopyDocument.setHistory(history);

        setDocument(scanCopyDocument);
    }

    @Override
    protected void initDocument(Integer id) {
        try {
            setDocument(sessionManagement.getDAO(ScanCopyDocumentDAOImpl.class, ApplicationHelper.SCAN_DAO).get(id));
            if (getDocument() == null) {
                setState(STATE_NOT_FOUND);
            } else {
                User currentUser = sessionManagement.getLoggedUser();
                currentUser = sessionManagement.getDAO(UserDAOHibernate.class, ApplicationHelper.USER_DAO).findByLoginAndPassword(currentUser.getLogin(), currentUser.getPassword());
                int userId = currentUser.getId();
                List<Role> in_roles;
                if (userId > 0) {
                    boolean isAdminRole = false;
                    in_roles = currentUser.getRoleList();
                    if (in_roles != null) {
                        for (Role in_role : in_roles) {
                            if (in_role.getRoleType().equals(RoleType.ADMINISTRATOR)) {
                                isAdminRole = true;
                                break;
                            }
                        }
                    }

                    ScanCopyDocument document = getDocument();
                    if (!isAdminRole && !isUserHasAccess(document, currentUser)) {
                        return;
                    }
                }

            }
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR, "Внутренняя ошибка.", ""));
            e.printStackTrace();
        }
    }

    @Override
    protected boolean saveNewDocument() {
        return saveDocument();
    }

    @Override
    protected boolean saveDocument() {
        try {
            ScanCopyDocument scanCopyDocument = sessionManagement.getDAO(ScanCopyDocumentDAOImpl.class, ApplicationHelper.SCAN_DAO).save(getDocument());
            if (scanCopyDocument == null) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_ERROR, "Документ не может быть сохранен. Попробуйте повторить позже.", ""));
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR, "Внутренняя ошибка.", ""));
            return false;
        }
        return true;
    }

    @Override
    protected FromStringConverter<Integer> getIdConverter() {
        return FromStringConverter.INTEGER_CONVERTER;
    }

    /**
     * Check user authorities to document
     */
    private boolean isUserHasAccess(ScanCopyDocument document, User currentUser) {
        if (document.getAuthor() == null || !document.getAuthor().equals(currentUser.getId())) {
            setState(STATE_FORBIDDEN);
            setStateComment("В доступе отказано.");
            return false;
        }
        return true;
    }

    protected boolean isCurrentUserDocEditor() {
        User in_user = sessionManagement.getLoggedUser();
        in_user = sessionManagement.getDAO(UserDAOHibernate.class, ApplicationHelper.USER_DAO).findByLoginAndPassword(in_user.getLogin(), in_user.getPassword());
        ScanCopyDocument scanCopyDocument = getDocument();

        if (in_user.isAdministrator() || in_user.equals(scanCopyDocument.getAuthor())) {
            return true;
        }

        return false;
    }
}
