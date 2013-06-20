package ru.efive.dms.uifaces.beans.task;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang.StringUtils;

import ru.efive.dao.alfresco.Revision;
import ru.efive.sql.dao.user.UserDAOHibernate;
import ru.efive.sql.entity.enums.DocumentStatus;
import ru.efive.sql.entity.enums.RoleType;
import ru.efive.sql.entity.user.Role;
import ru.efive.sql.entity.user.User;
import ru.efive.dms.dao.DocumentFormDAOImpl;
import ru.efive.dms.dao.IncomingDocumentDAOImpl;
import ru.efive.dms.dao.InternalDocumentDAOImpl;
import ru.efive.dms.dao.OutgoingDocumentDAOImpl;
import ru.efive.dms.dao.PaperCopyDocumentDAOImpl;
import ru.efive.dms.dao.RequestDocumentDAOImpl;
import ru.efive.dms.dao.TaskDAOImpl;
import ru.efive.dms.data.Attachment;
import ru.efive.dms.data.DocumentForm;
import ru.efive.dms.data.HistoryEntry;
import ru.efive.dms.data.IncomingDocument;
import ru.efive.dms.data.InternalDocument;
import ru.efive.dms.data.OutgoingDocument;
import ru.efive.dms.data.PaperCopyDocument;
import ru.efive.dms.data.RequestDocument;
import ru.efive.dms.data.Task;
import ru.efive.dms.uifaces.beans.FileManagementBean;
import ru.efive.dms.uifaces.beans.FileManagementBean.FileUploadDetails;
import ru.efive.dms.uifaces.beans.ProcessorModalBean;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.uifaces.beans.user.UserSelectModalBean;
import ru.efive.dms.util.ApplicationHelper;
import ru.efive.uifaces.bean.AbstractDocumentHolderBean;
import ru.efive.uifaces.bean.FromStringConverter;
import ru.efive.uifaces.bean.ModalWindowHolderBean;
import ru.efive.wf.core.ActionResult;

@Named("task")
@ConversationScoped
public class TaskHolder extends AbstractDocumentHolderBean<Task, Integer> implements Serializable {

    @Override
    public String delete() {
        String in_result = super.delete();
        if (in_result != null && in_result.equals("delete")) {
            try {
                FacesContext.getCurrentInstance().getExternalContext().redirect("../delete_document.xhtml");
            } catch (Exception e) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_ERROR, "Невозможно удалить документ", ""));
                e.printStackTrace();
            }
            return in_result;
        } else {
            return in_result;
        }
    }

    @Override
    protected boolean deleteDocument() {

        boolean result = false;
        try {
            result = sessionManagement.getDAO(TaskDAOImpl.class, ApplicationHelper.TASK_DAO).delete(getDocumentId());
            if (!result) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_ERROR,
                        "Невозможно удалить документ. Попробуйте повторить позже.", ""));
            }
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Внутренняя ошибка.", ""));
        }
        return result;
    }

    @Override
    protected Integer getDocumentId() {
        return getDocument() == null ? null : getDocument().getId();
    }

    @Override
    protected FromStringConverter<Integer> getIdConverter() {
        return FromStringConverter.INTEGER_CONVERTER;
    }

    @Override
    protected void initDocument(Integer id) {
        try {
            setDocument(sessionManagement.getDAO(TaskDAOImpl.class, ApplicationHelper.TASK_DAO).get(id));
            if (getDocument() == null) {
                setState(STATE_NOT_FOUND);
            } else {
                User currentUser = sessionManagement.getLoggedUser();
                currentUser = sessionManagement.getDAO(UserDAOHibernate.class, ApplicationHelper.USER_DAO).findByLoginAndPassword(currentUser.getLogin(), currentUser.getPassword());
                int userId = currentUser.getId();
                List<Role> in_roles = new ArrayList<Role>();
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

                    Task document = getDocument();
                    if (!isAdminRole && !isUserHasAccess(document, in_roles, currentUser)) {
                        return;
                    }
                }

            }
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Внутренняя ошибка.", ""));
            e.printStackTrace();
        }
    }

    /**
     * Check user authorities to document
     */
    private boolean isUserHasAccess(Task document, List<Role> in_roles, User currentUser) {
        Set<Integer> allReadersId = new HashSet<Integer>();
        List<Role> in_docUsersRoles = new ArrayList<Role>();
        if (document.getAuthor() != null) {
            allReadersId.add(document.getAuthor().getId());
            if (document.getAuthor().getRoleList().size() > 0) {
                in_docUsersRoles.addAll(document.getAuthor().getRoleList());
            }
        }

        if (document.getInitiator() != null) {
            allReadersId.add(document.getInitiator().getId());
            if (document.getInitiator().getRoleList().size() > 0) {
                in_docUsersRoles.addAll(document.getInitiator().getRoleList());
            }
        }

        // Исполнитель должен иметь права доступа к документу, через сколько бы поручений оно не проходило
        List<Task> relationTasks = sessionManagement.getDAO(TaskDAOImpl.class, ApplicationHelper.TASK_DAO).
                findAllRegistratedDocumentsByRootFormat(document.getRootDocumentId(), true);
        if (relationTasks != null) {
            for (Task relationTask : relationTasks) {
                User parentExecutor = (relationTask.getExecutor() != null) ? relationTask.getExecutor() : null;
                if (parentExecutor != null) {
                    allReadersId.add(parentExecutor.getId());
                }
            }
        }

        if (document.getExecutor() != null) {
            allReadersId.add(document.getExecutor().getId());
            if (document.getExecutor().getRoleList().size() > 0) {
                in_docUsersRoles.addAll(document.getExecutor().getRoleList());
            }
        }
        if (document.getController() != null) {
            allReadersId.add(document.getController().getId());
            if (document.getController().getRoleList().size() > 0) {
                in_docUsersRoles.addAll(document.getController().getRoleList());
            }
        }

        Set<Integer> in_docUsersRolesId = new HashSet<Integer>();
        for (Role role : in_docUsersRoles) {
            in_docUsersRolesId.add(role.getId());
        }

        Set<Integer> userRolesId = new HashSet<Integer>();
        for (Role role : in_roles) {
            userRolesId.add(role.getId());
        }

        boolean isUserReaderByRole = false;
        if (userRolesId.size() > 0 && in_docUsersRolesId.size() > 0) {
            userRolesId.retainAll(in_docUsersRolesId);
            isUserReaderByRole = (userRolesId.size() > 0);
        }

        if (!(allReadersId.contains(currentUser.getId()) || isUserReaderByRole)) {
            setState(STATE_FORBIDDEN);
            setStateComment("В доступе отказано.");
            return false;
        }
        return true;
    }

    @Override
    protected void initNewDocument() {
        Task doc = new Task();
        Date created = Calendar.getInstance(ApplicationHelper.getLocale()).getTime();
        doc.setCreationDate(created);
        doc.setAuthor(sessionManagement.getLoggedUser());
        doc.setRegistered(false);
        doc.setDocumentStatus(DocumentStatus.NEW);
        String parentId = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("parentId");
        if (parentId == null || parentId.equals("")) {
        } else {
            doc.setParentId(parentId);
            String rootDocumentId = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("rootDocumentId");
            doc.setRootDocumentId(rootDocumentId);
        }


        List<DocumentForm> forms = null;
        DocumentForm form = null;

        String formId = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("formId");
        if (formId == null || formId.equals("")) {
            forms = sessionManagement.getDictionaryDAO(DocumentFormDAOImpl.class, ApplicationHelper.DOCUMENT_FORM_DAO).findByCategoryAndDescription("Поручения", "task");
        } else {
            forms = sessionManagement.getDictionaryDAO(DocumentFormDAOImpl.class, ApplicationHelper.DOCUMENT_FORM_DAO).findByCategoryAndDescription("Поручения", formId);
        }
        if (forms.size() > 0) {
            form = forms.get(0);
            if (form.getDescription().equals("exercise")) {
                String exerciseTypeId = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("exerciseTypeId");
                if ((exerciseTypeId != null) && (!exerciseTypeId.isEmpty())) {
                    List<DocumentForm> exerciseTypes = null;
                    DocumentForm exerciseType = null;
                    exerciseTypes = sessionManagement.getDictionaryDAO(DocumentFormDAOImpl.class, ApplicationHelper.DOCUMENT_FORM_DAO).findByCategoryAndValue("Задачи", exerciseTypeId);
                    if (exerciseTypes.size() > 0) {
                        exerciseType = exerciseTypes.get(0);
                        if (exerciseType != null) {
                            doc.setExerciseType(exerciseType);
                        }
                    }
                }
            }
        } else {
            forms = sessionManagement.getDictionaryDAO(DocumentFormDAOImpl.class, ApplicationHelper.DOCUMENT_FORM_DAO).findByCategory("Поручения");
            if (forms.size() > 0) {
                form = forms.get(0);
            }
        }
        if (form != null) {
            doc.setForm(form);
        }

        HistoryEntry historyEntry = new HistoryEntry();
        historyEntry.setCreated(created);
        historyEntry.setStartDate(created);
        historyEntry.setOwner(sessionManagement.getLoggedUser());
        historyEntry.setDocType(doc.getDocumentType().getName());
        historyEntry.setParentId(doc.getId());
        historyEntry.setActionId(0);
        historyEntry.setFromStatusId(1);
        historyEntry.setEndDate(created);
        historyEntry.setProcessed(true);
        historyEntry.setCommentary("");
        Set<HistoryEntry> history = new HashSet<HistoryEntry>();
        history.add(historyEntry);
        doc.setHistory(history);

        setDocument(doc);
        initDefaultInitiator();
    }


    @Override
    protected boolean saveDocument() {
        boolean result = false;
        try {
            Task task = sessionManagement.getDAO(TaskDAOImpl.class, ApplicationHelper.TASK_DAO).save(getDocument());
            if (task == null) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_ERROR,
                        "Документ не может быть сохранен. Попробуйте повторить позже.", ""));
            } else {
                result = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Внутренняя ошибка.", ""));
        }
        return result;
    }

    @Override
    protected boolean saveNewDocument() {
        boolean result = false;
        try {
            //Task document=(Task)getDocument();
            /*if (document != null) {
                   if(document.getTaskNumber()==null || document.getTaskNumber().isEmpty()){
                       StringBuffer in_number=new StringBuffer(""+(sessionManagement.getDAO(TaskDAOImpl.class, ApplicationHelper.TASK_DAO).countDocument(true)+1));
                       document.setTaskNumber(in_number.toString());
                   }
               }*/
            Task task = sessionManagement.getDAO(TaskDAOImpl.class, ApplicationHelper.TASK_DAO).save(getDocument());
            if (task == null) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_ERROR,
                        "Документ не может быть сохранен. Попробуйте повторить позже.", ""));
            } else {
                System.out.println("uploading newly created files");
                for (int i = 0; i < files.size(); i++) {
                    Attachment tmpAttachment = attachments.get(i);
                    if (tmpAttachment != null) {
                        tmpAttachment.setParentId(new String(("task_" + getDocumentId()).getBytes(), "utf-8"));
                        fileManagement.createFile(tmpAttachment, files.get(i));
                    }
                }
                result = true;

            }
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Внутренняя ошибка.", ""));
        }
        return result;
    }

    protected String getLinkDescriptionById(String key) {
        if (!key.isEmpty()) {
            int pos = key.indexOf('_');
            if (pos != -1) {
                String id = key.substring(pos + 1, key.length());
                //String in_type=key.substring(0,pos);
                StringBuffer in_description = new StringBuffer("");
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

                if (key.indexOf("incoming") != -1) {
                    IncomingDocument in_doc = sessionManagement.getDAO(IncomingDocumentDAOImpl.class, ApplicationHelper.INCOMING_DOCUMENT_FORM_DAO).findDocumentById(id);
                    in_description = new StringBuffer(in_doc.getRegistrationNumber() == null || in_doc.getRegistrationNumber().equals("") ?
                            "Черновик входщяего документа от " + sdf.format(in_doc.getCreationDate()) :
                            "Входящий документ № " + in_doc.getRegistrationNumber() + " от " + sdf.format(in_doc.getRegistrationDate())
                    );

                } else if (key.indexOf("outgoing") != -1) {
                    OutgoingDocument out_doc = sessionManagement.getDAO(OutgoingDocumentDAOImpl.class, ApplicationHelper.OUTGOING_DOCUMENT_FORM_DAO).findDocumentById(id);
                    in_description = new StringBuffer(out_doc.getRegistrationNumber() == null || out_doc.getRegistrationNumber().equals("") ?
                            "Черновик исходящего документа от " + sdf.format(out_doc.getCreationDate()) :
                            "Исходящий документ № " + out_doc.getRegistrationNumber() + " от " + sdf.format(out_doc.getRegistrationDate())
                    );

                } else if (key.indexOf("internal") != -1) {
                    InternalDocument internal_doc = sessionManagement.getDAO(InternalDocumentDAOImpl.class, ApplicationHelper.INTERNAL_DOCUMENT_FORM_DAO).findDocumentById(id);
                    in_description = new StringBuffer(internal_doc.getRegistrationNumber() == null || internal_doc.getRegistrationNumber().equals("") ?
                            "Черновик внутреннего документа от " + sdf.format(internal_doc.getCreationDate()) :
                            "Внутренний документ № " + internal_doc.getRegistrationNumber() + " от " + sdf.format(internal_doc.getRegistrationDate())
                    );

                } else if (key.indexOf("request") != -1) {
                    RequestDocument request_doc = sessionManagement.getDAO(RequestDocumentDAOImpl.class, ApplicationHelper.REQUEST_DOCUMENT_FORM_DAO).findDocumentById(id);
                    in_description = new StringBuffer(request_doc.getRegistrationNumber() == null || request_doc.getRegistrationNumber().equals("") ?
                            "Черновик обращения граждан от " + sdf.format(request_doc.getCreationDate()) :
                            "Обращение граждан № " + request_doc.getRegistrationNumber() + " от " + sdf.format(request_doc.getRegistrationDate())
                    );

                } else if (key.indexOf("task") != -1) {
                    Task task_doc = sessionManagement.getDAO(TaskDAOImpl.class, ApplicationHelper.TASK_DAO).findDocumentById(id);
                    in_description = new StringBuffer(task_doc.getTaskNumber() == null || task_doc.getTaskNumber().equals("") ?
                            "Черновик поручения от " + sdf.format(task_doc.getCreationDate()) :
                            "Поручение № " + task_doc.getTaskNumber() + " от " + sdf.format(task_doc.getCreationDate())
                    );
                }
                return in_description.toString();
            }
        }
        return "";
    }

    protected void initDefaultInitiator() {
        Task task = getDocument();
        User initiator = null;
        String key = task.getParentId();
        if (key != null && !key.isEmpty()) {
            int pos = key.indexOf('_');
            if (pos != -1) {
                String id = key.substring(pos + 1, key.length());

                List<PaperCopyDocument> paperCopies = sessionManagement.getDAO(PaperCopyDocumentDAOImpl.class, ApplicationHelper.PAPER_COPY_DOCUMENT_FORM_DAO).findAllDocumentsByParentId(key);
                if (key.indexOf("incoming") != -1) {
                    IncomingDocument in_doc = sessionManagement.getDAO(IncomingDocumentDAOImpl.class, ApplicationHelper.INCOMING_DOCUMENT_FORM_DAO).findDocumentById(id);
                    initiator = in_doc.getController();
                } else if (key.indexOf("outgoing") != -1) {
                    OutgoingDocument out_doc = sessionManagement.getDAO(OutgoingDocumentDAOImpl.class, ApplicationHelper.OUTGOING_DOCUMENT_FORM_DAO).findDocumentById(id);
                    initiator = out_doc.getSigner();
                } else if (key.indexOf("internal") != -1) {
                    InternalDocument internal_doc = sessionManagement.getDAO(InternalDocumentDAOImpl.class, ApplicationHelper.INTERNAL_DOCUMENT_FORM_DAO).findDocumentById(id);
                    initiator = internal_doc.getController();
                } else if (key.indexOf("request") != -1) {
                    RequestDocument request_doc = sessionManagement.getDAO(RequestDocumentDAOImpl.class, ApplicationHelper.REQUEST_DOCUMENT_FORM_DAO).findDocumentById(id);
                    initiator = request_doc.getController();
                } else if (key.indexOf("task") != -1) {
                    Task parent_task = sessionManagement.getDAO(TaskDAOImpl.class, ApplicationHelper.TASK_DAO).findDocumentById(id);
                    initiator = parent_task.getExecutor();
                }

                if (initiator != null) {
                    task.setInitiator(initiator);
                }
            }
        }
    }

    @Override
    protected String doAfterCreate() {
        taskList.markNeedRefresh();
        return super.doAfterCreate();
    }

    @Override
    protected String doAfterEdit() {
        taskList.markNeedRefresh();
        return super.doAfterEdit();
    }

    @Override
    protected String doAfterDelete() {
        taskList.markNeedRefresh();
        return super.doAfterDelete();
    }

    @Override
    protected String doAfterSave() {
        taskList.markNeedRefresh();
        return super.doAfterSave();
    }

    // FILES

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public void uploadAttachments(FileUploadDetails details) {
        try {
            if (details.getAttachment() != null) {
                Attachment attachment = details.getAttachment();
                //attachment.setFileName(new String(attachment.getFileName().getBytes(), "utf-8"));
                attachment.setFileName(attachment.getFileName());
                if (getDocumentId() == null || getDocumentId() == 0) {
                    attachments.add(attachment);
                    files.add(details.getByteArray());
                } else {
                    attachment.setParentId(new String(("task_" + getDocumentId()).getBytes(), "utf-8"));
                    System.out.println("result of the upload operation - " + fileManagement.createFile(attachment, details.getByteArray()));
                    updateAttachments();
                }
            }
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Внутренняя ошибка при вложении файла.", ""));
            e.printStackTrace();
        }
    }

    public void versionAttachment(FileUploadDetails details, Attachment attachment, boolean majorVersion) {
        try {
            if (details.getByteArray() != null) {
                if (getDocumentId() == null || getDocumentId() == 0) {
                    if (attachments.contains(attachment)) {
                        int pos = attachments.indexOf(attachment);
                        if (pos > -1) {
                            files.remove(pos);
                            files.add(pos, details.getByteArray());
                        }
                    } else {
                        attachments.add(attachment);
                        files.add(details.getByteArray());
                    }
                } else {
                    System.out.println("result of the upload operation - " + fileManagement.createVersion(attachment, details.getByteArray(), majorVersion, details.getAttachment().getFileName()));
                    updateAttachments();
                }
            }
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Внутренняя ошибка при вложении файла.", ""));
            e.printStackTrace();
        }
    }

    public void updateAttachments() {
        if (getDocumentId() != null && getDocumentId() != 0) {
            attachments = fileManagement.getFilesByParentId("task_" + getDocumentId());
            if (attachments == null) attachments = new ArrayList<Attachment>();
        }
    }

    public void deleteAttachment(Attachment attachment) {
        Task document = getDocument();
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
        historyEntry.setCommentary("Файл " + attachment.getFileName() + " был удален");
        Set<HistoryEntry> history = document.getHistory();
        history.add(historyEntry);
        document.setHistory(history);

        setDocument(document);

        if (attachment != null) {
            if (fileManagement.deleteFile(attachment)) {
                updateAttachments();
            }
        }

        document = sessionManagement.getDAO(TaskDAOImpl.class, ApplicationHelper.TASK_DAO).save(document);
    }

    private List<Attachment> attachments = new ArrayList<Attachment>();
    private List<byte[]> files = new ArrayList<byte[]>();

    // END OF FILES

    public class VersionAppenderModal extends ModalWindowHolderBean {

        public Attachment getAttachment() {
            return attachment;
        }

        public void setAttachment(Attachment attachment) {
            this.attachment = attachment;
        }

        public void setMajorVersion(boolean majorVersion) {
            this.majorVersion = majorVersion;
        }

        public boolean isMajorVersion() {
            return majorVersion;
        }

        @Override
        protected void doShow() {
            super.doShow();
            setMajorVersion(false);
        }

        public void saveAttachment() {
            if (attachment != null) {
                versionAttachment(fileManagement.getDetails(), attachment, majorVersion);
            }
            versionAppenderModal.save();
        }

        @Override
        protected void doHide() {
            super.doHide();
            attachment = null;
        }

        private Attachment attachment;
        private boolean majorVersion;
    }

    public class VersionHistoryModal extends ModalWindowHolderBean {

        public Attachment getAttachment() {
            return attachment;
        }

        public void setAttachment(Attachment attachment) {
            this.attachment = attachment;
        }

        public List<Revision> getVersionList() {
            return versionList == null ? new ArrayList<Revision>() : versionList;
        }

        @Override
        protected void doShow() {
            super.doShow();
            versionList = fileManagement.getVersionHistory(attachment);
        }

        @Override
        protected void doHide() {
            super.doHide();
            versionList = null;
            attachment = null;
        }


        private Attachment attachment;
        private List<Revision> versionList;
    }

    public VersionAppenderModal getVersionAppenderModal() {
        return versionAppenderModal;
    }

    public void initializeVersionAppender(Attachment attachment) {
        versionAppenderModal.setAttachment(attachment);
        versionAppenderModal.setModalVisible(true);
    }

    public VersionHistoryModal getVersionHistoryModal() {
        return versionHistoryModal;
    }

    public void initializeVersionHistory(Attachment attachment) {
        versionHistoryModal.setAttachment(attachment);
        versionHistoryModal.show();
    }

    // MODAL HOLDERS

    // END OF MODAL HOLDERS


    public String getRequisitesTabHeader() {
        return "<span><span>Реквизиты</span></span>";
    }

    public boolean isRequisitesTabSelected() {
        return isRequisitesTabSelected;
    }

    public void setRequisitesTabSelected(boolean isRequisitesTabSelected) {
        this.isRequisitesTabSelected = isRequisitesTabSelected;
    }

    public String getRouteTabHeader() {
        return "<span><span>Движение документа</span></span>";
    }

    public boolean isRouteTabSelected() {
        return isRouteTabSelected;
    }

    public void setRouteTabSelected(boolean isRouteTabSelected) {
        this.isRouteTabSelected = isRouteTabSelected;
    }

    public String getRelationTabHeader() {
        return "<span><span>Связи</span></span>";
    }

    public boolean isRelationTabSelected() {
        return isRelationTabSelected;
    }

    public void setRelationTabSelected(boolean isRelationTabSelected) {
        this.isRelationTabSelected = isRelationTabSelected;
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

    public UserSelectModalBean getResponsibleSelectModal() {
        return responsibleSelectModal;
    }

    public UserSelectModalBean getInitiatorSelectModal() {
        return initiatorSelectModal;
    }

    public UserSelectModalBean getControllerSelectModal() {
        return controllerSelectModal;
    }

    private UserSelectModalBean responsibleSelectModal = new UserSelectModalBean() {
        @Override
        protected void doSave() {
            super.doSave();
            getDocument().setExecutor(getUser());
        }

        @Override
        protected void doHide() {
            super.doHide();
            getUserList().setFilter("");
            getUserList().markNeedRefresh();
            setUser(null);
        }
    };

    private UserSelectModalBean initiatorSelectModal = new UserSelectModalBean() {
        @Override
        protected void doShow() {
            this.setUser(getDocument().getInitiator());
            super.doShow();
        }

        @Override
        protected void doSave() {
            super.doSave();
            getDocument().setInitiator(getUser());
        }

        @Override
        protected void doHide() {
            super.doHide();
            getUserList().setFilter("");
            getUserList().markNeedRefresh();
            this.setUser(null);
        }
    };

    private UserSelectModalBean controllerSelectModal = new UserSelectModalBean() {
        @Override
        protected void doShow() {
            this.setUser(getDocument().getController());
            super.doShow();
        }

        @Override
        protected void doSave() {
            super.doSave();
            getDocument().setController(getUser());
        }

        @Override
        protected void doHide() {
            super.doHide();
            getUserList().setFilter("");
            getUserList().markNeedRefresh();
            this.setUser(null);
        }
    };

    public ProcessorModalBean getProcessorModal() {
        return processorModal;
    }

    private ProcessorModalBean processorModal = new ProcessorModalBean() {
        @Override
        protected void doInit() {
            setProcessedData(getDocument());
            if (getDocumentId() == null || getDocumentId() == 0) saveNewDocument();
        }

        @Override
        protected void doSave() {
            Task task = getDocument();
            Set<HistoryEntry> history = task.getHistory();
            super.doSave();

            task.setHistory(history);
            setDocument(task);
            TaskHolder.this.save();
        }

        @Override
        protected void doPostProcess(ActionResult actionResult) {
            Task task = (Task) actionResult.getProcessedData();
            if (getSelectedAction().isHistoryAction()) {
                Set<HistoryEntry> history = task.getHistory();
                if (history == null) {
                    history = new HashSet<HistoryEntry>();
                }
                history.add(getHistoryEntry());
                task.setHistory(history);
            }
            setDocument(task);
            TaskHolder.this.save();
        }

        @Override
        protected void doProcessException(ActionResult actionResult) {
            Task document = (Task) actionResult.getProcessedData();
            String in_result = document.getWFResultDescription();
            if (in_result != null) {
                if (StringUtils.isNotEmpty(in_result)) {
                    setActionResult(in_result);
                }
            } else {
                setActionResult("");
            }
        }
    };

    public String getRootDocumentIdByTaskDocument(Task task) {
        if (task != null) {
            String key = task.getParentId();
            if (key != null && !key.isEmpty()) {
                int pos = key.indexOf('_');
                if (pos != -1) {
                    String id = key.substring(pos + 1, key.length());
                    StringBuffer in_description = new StringBuffer("");

                    if (key.indexOf("incoming") != -1) {
                        return key;
                    } else if (key.indexOf("outgoing") != -1) {
                        return key;
                    } else if (key.indexOf("internal") != -1) {
                        return key;
                    } else if (key.indexOf("request") != -1) {
                        return key;
                    } else if (key.indexOf("task") != -1) {
                        Task task_doc = sessionManagement.getDAO(TaskDAOImpl.class, ApplicationHelper.TASK_DAO).findDocumentById(id);
                        if (task.getParentId() == null || task.getParentId().isEmpty()) {
                            return "task_" + task.getId();
                        } else {
                            return getRootDocumentIdByTaskDocument(task_doc);
                        }
                    } else {
                        return key;
                    }
                }
            } else {
                return key;
            }
            return key;
        } else {
            return "";
        }
    }

    public void setStateComment(String stateComment) {
        this.stateComment = stateComment;
    }

    public String getStateComment() {
        return stateComment;
    }

    private boolean isRequisitesTabSelected = true;
    private boolean isRouteTabSelected = false;
    private boolean isRelationTabSelected = false;
    private boolean isHistoryTabSelected = false;

    private VersionAppenderModal versionAppenderModal = new VersionAppenderModal();
    private VersionHistoryModal versionHistoryModal = new VersionHistoryModal();

    private transient String stateComment;

    @Inject
    @Named("sessionManagement")
    private transient SessionManagementBean sessionManagement;
    @Inject
    @Named("tasks")
    private transient TaskListHolder taskList;
    @Inject
    @Named("fileManagement")
    private transient FileManagementBean fileManagement;

    private static final long serialVersionUID = 4716264614655470705L;
}