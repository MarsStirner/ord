package ru.efive.uifaces.demo.bean;

import java.util.ArrayList;
import javax.enterprise.context.ConversationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import ru.efive.uifaces.bean.workflow.AbstractDocumentWorkflow;
import ru.efive.uifaces.bean.workflow.AbstractWorkflow.Action;
import ru.efive.uifaces.demo.dao.ContractDaoEmulation;
import ru.efive.uifaces.demo.document.Contract;
import ru.efive.uifaces.demo.document.Organization;
import ru.efive.uifaces.demo.document.Person;

/**
 *
 * @author Denis Kotegov
 */
@Named("contractWorkflow")
@ConversationScoped
public class ContractWorkflowBean extends AbstractDocumentWorkflow<Contract, Integer> {

    private static final long serialVersionUID = 1L;

    @Inject @Named("contractList")
    private ContractListHolderBean contractListHolderBean;

    @Override
    protected void deleteDocument(Contract document) {
        ContractDaoEmulation.delete(document.getId());
    }

    @Override
    protected Integer getDocumentId(Contract document) {
        return document == null? null: document.getId();
    }

    @Override
    protected Contract initDocument(Integer documentId) {
        return ContractDaoEmulation.get(documentId);
    }

    @Override
    protected Contract initNewDocument() {
        Contract result = new Contract();
        
        result.setAuthor(new Person());
        result.setCurator(new Person());
        result.setContractors(new ArrayList<Organization>());

        return result;
    }

    @Override
    protected void saveDocument(Contract document) {
        ContractDaoEmulation.save(document);
    }

    @Override
    protected Contract saveNewDocument(Contract document) {
        return ContractDaoEmulation.add(document);
    }

    @Override
    protected String getViewKeyView() {
        return "/component/contractForTable.xhtml";
    }

    @Override
    protected String getViewKeyFinal() {
        return "/component/table.xhtml";
    }

    @Override
    public Contract getDocument() {
        return super.getDocument();
    }

    @Override
    protected Action buildActionApply() {
        return createActionWithTableRefresh(super.buildActionApply());
    }

    @Override
    protected Action buildActionSave() {
        return createActionWithTableRefresh(super.buildActionSave());
    }

    @Override
    protected Action buildActionSaveAndView() {
        return createActionWithTableRefresh(super.buildActionSaveAndView());
    }

    protected Action createActionWithTableRefresh(Action action) {
        return new ActionWrapper(action) {

            @Override
            public void onSuccess() {
                super.onSuccess();
                contractListHolderBean.markNeedRefresh();
            }

        };
    }

}
