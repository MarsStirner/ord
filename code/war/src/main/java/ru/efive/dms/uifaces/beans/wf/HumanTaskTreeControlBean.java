package ru.efive.dms.uifaces.beans.wf;

import java.util.Calendar;

import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.uifaces.beans.user.UserSelectModalBean;
import ru.util.ApplicationHelper;
import ru.efive.dms.util.WorkflowHelper;
import ru.entity.model.enums.DocumentStatus;
import ru.efive.uifaces.bean.ModalWindowHolderBean;
import ru.external.AgreementIssue;
import ru.entity.model.wf.HumanTask;
import ru.entity.model.wf.HumanTaskTree;
import ru.entity.model.wf.HumanTaskTreeNode;
import ru.entity.model.wf.RouteTemplate;

@Named("humanTaskTreeControl")
@SessionScoped
public class HumanTaskTreeControlBean implements java.io.Serializable {

    public void addRootHumanTask(HumanTaskTree agreementTree) {
        HumanTask humanTask = new HumanTask();
        humanTask.setAuthor(sessionManagement.getLoggedUser());
        humanTask.setCreated(Calendar.getInstance(ApplicationHelper.getLocale()).getTime());
        humanTask.setStatusId(DocumentStatus.NEW.getId());

        HumanTaskTreeNode node = new HumanTaskTreeNode();
        humanTask.setParentNode(node);
        node.addTask(humanTask);
        agreementTree.addRootNode(node);

        agreementTree.loadHumanTaskTree();
    }

    public void addParallelHumanTask(HumanTaskTree agreementTree, HumanTaskTreeNode node) {
        HumanTask humanTask = new HumanTask();
        humanTask.setAuthor(sessionManagement.getLoggedUser());
        humanTask.setCreated(Calendar.getInstance(ApplicationHelper.getLocale()).getTime());
        humanTask.setStatusId(DocumentStatus.NEW.getId());
        humanTask.setParentNode(node);

        node.addTask(humanTask);

        agreementTree.loadHumanTaskTree();
    }

    public void addFollowingHumanTask(HumanTaskTree agreementTree, HumanTaskTreeNode node) {
        HumanTask humanTask = new HumanTask();
        humanTask.setAuthor(sessionManagement.getLoggedUser());
        humanTask.setCreated(Calendar.getInstance(ApplicationHelper.getLocale()).getTime());
        humanTask.setStatusId(DocumentStatus.NEW.getId());
        humanTask.setParentNode(node);

        HumanTaskTreeNode childNode = new HumanTaskTreeNode();
        childNode.setParentNode(node);
        node.addChildNode(childNode);
        childNode.addTask(humanTask);

        agreementTree.loadHumanTaskTree();
    }

    public void composeHumanTaskExecutorSelectModal(HumanTask humanTask) {
        humanTaskExecutorSelectModal.setHumanTask(humanTask);
        humanTaskExecutorSelectModal.show();
    }

    public HumanTaskExecutorSelectModal getHumanTaskExecutorSelectModal() {
        return humanTaskExecutorSelectModal;
    }

    public class HumanTaskExecutorSelectModal extends UserSelectModalBean {

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

        private static final long serialVersionUID = -3982278140984190051L;
    }

    public void composeRouteTemplateSelectModal(AgreementIssue agreementIssueDoc) {
        routeTemplateSelectModal.setAgreementIssueDoc(agreementIssueDoc);
        routeTemplateSelectModal.show();
    }

    public RouteTemplateSelectModal getRouteTemplateSelectModal() {
        return routeTemplateSelectModal;
    }

    public class RouteTemplateSelectModal extends ModalWindowHolderBean {

        public RouteTemplateListBean getRouteTemplateList() {
            if (routeTemplateList == null) {
                FacesContext context = FacesContext.getCurrentInstance();
                routeTemplateList = (RouteTemplateListBean) context.getApplication().evaluateExpressionGet(context, "#{routeTemplates}", RouteTemplateListBean.class);
            }
            return routeTemplateList;
        }

        public RouteTemplate getRouteTemplate() {
            return routeTemplate;
        }

        public void setRouteTemplate(RouteTemplate routeTemplate) {
            this.routeTemplate = routeTemplate;
        }

        public void select(RouteTemplate routeTemplate) {
            this.routeTemplate = routeTemplate;
        }

        public boolean selected(RouteTemplate routeTemplate) {
            return this.routeTemplate != null && this.routeTemplate.equals(routeTemplate);
        }

        public void setAgreementIssueDoc(AgreementIssue agreementIssueDoc) {
            this.agreementIssueDoc = agreementIssueDoc;
        }

        public AgreementIssue getAgreementIssueDoc() {
            return agreementIssueDoc;
        }

        @Override
        protected void doSave() {
            super.doSave();
            if (agreementIssueDoc != null) {
                WorkflowHelper.formAgreementTree(agreementIssueDoc, routeTemplate.getTaskTree());
            }
        }

        @Override
        protected void doHide() {
            super.doHide();
            setRouteTemplate(null);
            setAgreementIssueDoc(null);
        }


        private RouteTemplateListBean routeTemplateList;
        private RouteTemplate routeTemplate;
        private AgreementIssue agreementIssueDoc;

        private static final long serialVersionUID = -147276267202594109L;
    }


    private HumanTaskExecutorSelectModal humanTaskExecutorSelectModal = new HumanTaskExecutorSelectModal();
    private RouteTemplateSelectModal routeTemplateSelectModal = new RouteTemplateSelectModal();

    @Inject
    @Named("sessionManagement")
    private transient SessionManagementBean sessionManagement;


    private static final long serialVersionUID = -4051544470740884383L;
}