package ru.efive.dms.uifaces.beans.wf;

import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.uifaces.beans.roles.RoleListSelectModalBean;
import ru.efive.dms.uifaces.beans.user.UserListSelectModalBean;
import ru.efive.dms.uifaces.beans.user.UserSelectModalBean;
import ru.efive.uifaces.bean.AbstractDocumentHolderBean;
import ru.efive.uifaces.bean.FromStringConverter;
import ru.efive.wf.core.dao.EngineDAOImpl;
import ru.entity.model.enums.DocumentStatus;
import ru.entity.model.wf.HumanTask;
import ru.entity.model.wf.HumanTaskTree;
import ru.entity.model.wf.HumanTaskTreeNode;
import ru.entity.model.wf.RouteTemplate;
import ru.util.ApplicationHelper;

import javax.enterprise.context.ConversationScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Calendar;

import static ru.efive.dms.uifaces.beans.utils.MessageHolder.*;
import static ru.efive.dms.util.ApplicationDAONames.ENGINE_DAO;

@Named("routeTemplate")
@ConversationScoped
public class RouteTemplateBean extends AbstractDocumentHolderBean<RouteTemplate, Integer> {

    @Override
    protected boolean deleteDocument() {
        boolean result = false;
        try {
            result = sessionManagement.getDAO(EngineDAOImpl.class, ENGINE_DAO).delete(getDocumentId());
            if (!result) {
                FacesContext.getCurrentInstance().addMessage(null, MSG_CANT_DELETE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, MSG_ERROR_ON_DELETE);
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
            setDocument(sessionManagement.getDAO(EngineDAOImpl.class, ENGINE_DAO).get(RouteTemplate.class, id));
            if (getDocument() == null) {
                setState(STATE_NOT_FOUND);
            }
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, MSG_ERROR_ON_INITIALIZE);
            e.printStackTrace();
        }
    }

    @Override
    protected void initNewDocument() {
        RouteTemplate template = new RouteTemplate();
        template.setDeleted(false);
        template.setTaskTree(new HumanTaskTree());
        template.setAuthor(sessionManagement.getLoggedUser());
        template.setCreated(Calendar.getInstance(ApplicationHelper.getLocale()).getTime());
        setDocument(template);
    }

    @Override
    protected boolean saveDocument() {
        boolean result = false;
        try {
            RouteTemplate template = sessionManagement.getDAO(EngineDAOImpl.class, ENGINE_DAO).save(RouteTemplate.class, getDocument());
            if (template == null) {
                FacesContext.getCurrentInstance().addMessage(null, MSG_CANT_SAVE);
            } else {
                result = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, MSG_ERROR_ON_SAVE);
        }
        return result;
    }

    @Override
    protected boolean saveNewDocument() {
        boolean result = false;
        try {
            RouteTemplate template = sessionManagement.getDAO(EngineDAOImpl.class, ENGINE_DAO).save(RouteTemplate.class, getDocument());
            if (template == null) {
                FacesContext.getCurrentInstance().addMessage(null, MSG_CANT_SAVE);
            } else {
                result = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, MSG_ERROR_ON_SAVE_NEW);
        }
        return result;
    }

    public void addRootHumanTask() {
        HumanTask humanTask = new HumanTask();
        humanTask.setAuthor(sessionManagement.getLoggedUser());
        humanTask.setCreated(Calendar.getInstance(ApplicationHelper.getLocale()).getTime());
        humanTask.setStatusId(DocumentStatus.NEW.getId());

        HumanTaskTreeNode node = new HumanTaskTreeNode();
        humanTask.setParentNode(node);
        node.addTask(humanTask);
        getDocument().getTaskTree().addRootNode(node);

        getDocument().getTaskTree().loadHumanTaskTree();
    }

    public void addParallelHumanTask(HumanTaskTreeNode node) {
        HumanTask humanTask = new HumanTask();
        humanTask.setAuthor(sessionManagement.getLoggedUser());
        humanTask.setCreated(Calendar.getInstance(ApplicationHelper.getLocale()).getTime());
        humanTask.setStatusId(DocumentStatus.NEW.getId());
        humanTask.setParentNode(node);

        node.addTask(humanTask);

        getDocument().getTaskTree().loadHumanTaskTree();
    }

    public void addFollowingHumanTask(HumanTaskTreeNode node) {
        HumanTask humanTask = new HumanTask();
        humanTask.setAuthor(sessionManagement.getLoggedUser());
        humanTask.setCreated(Calendar.getInstance(ApplicationHelper.getLocale()).getTime());
        humanTask.setStatusId(DocumentStatus.NEW.getId());
        humanTask.setParentNode(node);

        HumanTaskTreeNode childNode = new HumanTaskTreeNode();
        childNode.setParentNode(node);
        node.addChildNode(childNode);
        childNode.addTask(humanTask);

        getDocument().getTaskTree().loadHumanTaskTree();
    }


    public void composeExecutorSelectModal(HumanTask humanTask) {
        executorSelectModal.setHumanTask(humanTask);
        executorSelectModal.show();
    }

    public ExecutorSelectModal getExecutorSelectModal() {
        return executorSelectModal;
    }

    public UserListSelectModalBean getReadersPickList() {
        return readersPickList;
    }

    public UserListSelectModalBean getEditorsPickList() {
        return editorsPickList;
    }

    public RoleListSelectModalBean getReaderRolesPickList() {
        return readerRolesPickList;
    }

    public RoleListSelectModalBean getEditorRolesPickList() {
        return editorRolesPickList;
    }


    public String getSettingsTabHeader() {
        return "<span><span>Настройка согласования</span></span>";
    }

    public boolean isSettingsTabSelected() {
        return settingsTabSelected;
    }

    public void setSettingsTabSelected(boolean settingsTabSelected) {
        this.settingsTabSelected = settingsTabSelected;
    }

    public String getAccessTabHeader() {
        return "<span><span>Доступ</span></span>";
    }

    public boolean isAccessTabSelected() {
        return accessTabSelected;
    }

    public void setAccessTabSelected(boolean accessTabSelected) {
        this.accessTabSelected = accessTabSelected;
    }

    private boolean settingsTabSelected = true;
    private boolean accessTabSelected = false;


    public class ExecutorSelectModal extends UserSelectModalBean {

        public void setHumanTask(HumanTask humanTask) {
            this.humanTask = humanTask;
        }

        public HumanTask getHumanTask() {
            return humanTask;
        }

        @Override
        protected void doSave() {
            super.doSave();
            if (humanTask != null) humanTask.setExecutor(getUser());
        }

        @Override
        protected void doHide() {
            super.doHide();
            getUserList().setFilter("");
            getUserList().markNeedRefresh();
            setUser(null);
            humanTask = null;
        }

        private HumanTask humanTask;
    }

    private ExecutorSelectModal executorSelectModal = new ExecutorSelectModal();


    private UserListSelectModalBean readersPickList = new UserListSelectModalBean() {

        @Override
        protected void doSave() {
            super.doSave();
            getDocument().getReaders().addAll(getUsers());
        }

        @Override
        protected void doHide() {
            super.doHide();
            getUserList().setFilter("");
            getUserList().markNeedRefresh();
            setUsers(null);
        }
    };

    private UserListSelectModalBean editorsPickList = new UserListSelectModalBean() {

        @Override
        protected void doSave() {
            super.doSave();
            getDocument().getEditors().addAll(getUsers());
        }

        @Override
        protected void doHide() {
            super.doHide();
            getUserList().setFilter("");
            getUserList().markNeedRefresh();
            setUsers(null);
        }
    };

    private RoleListSelectModalBean readerRolesPickList = new RoleListSelectModalBean() {

        @Override
        protected void doSave() {
            super.doSave();
            getDocument().getReaderRoles().addAll(getRoles());
        }

        @Override
        protected void doHide() {
            super.doHide();
            getRoleList().setFilter("");
            setRoles(null);
        }
    };

    private RoleListSelectModalBean editorRolesPickList = new RoleListSelectModalBean() {

        @Override
        protected void doSave() {
            super.doSave();
            getDocument().getEditorRoles().addAll(getRoles());
        }

        @Override
        protected void doHide() {
            super.doHide();
            getRoleList().setFilter("");
            setRoles(null);
        }
    };


    @Inject
    @Named("sessionManagement")
    private transient SessionManagementBean sessionManagement;

    private static final long serialVersionUID = 6145083231714062442L;
}