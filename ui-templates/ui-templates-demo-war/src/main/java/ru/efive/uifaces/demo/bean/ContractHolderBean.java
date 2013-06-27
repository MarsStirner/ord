package ru.efive.uifaces.demo.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import ru.efive.uifaces.bean.AbstractDocumentHolderBean;
import ru.efive.uifaces.bean.AbstractDocumentListHolderBean.Pagination;
import ru.efive.uifaces.bean.FromStringConverter;
import ru.efive.uifaces.bean.ModalWindowHolderBean;
import ru.efive.uifaces.demo.dao.ContractDaoEmulation;
import ru.efive.uifaces.demo.document.Contract;
import ru.efive.uifaces.demo.document.Organization;
import ru.efive.uifaces.demo.document.Person;

/**
 *
 * @author Denis Kotegov
 */
@Named("contractHolder")
@ConversationScoped
public class ContractHolderBean extends AbstractDocumentHolderBean<Contract, Integer> implements Serializable {

    public class RegNumberModalHolder extends ModalWindowHolderBean {

        private static final long serialVersionUID = 1L;

        private String registrationNumber;

        public String getRegistrationNumber() {
            return registrationNumber;
        }

        public void setRegistrationNumber(String registrationNumber) {
            this.registrationNumber = registrationNumber;
        }

        @Override
        protected void doSave() {
            getDocument().setRegistrationNumber(registrationNumber);
        }

        @Override
        protected void doShow() {
            super.doShow();
            this.registrationNumber = getDocument().getRegistrationNumber();
        }

    }

    public class SumSelectorModalHolder extends ModalWindowHolderBean {

        private static final long serialVersionUID = 1L;

        private ContractListHolderBean contractList;

        private Set<Contract> selectedContracts;

        @Override
        protected void doHide() {
            contractList = null;
            selectedContracts = null;
        }

        @Override
        protected void doSave() {
            super.doSave();

            double sum = 0;
            for (Contract contract : selectedContracts) {
                sum += contract.getSum();
            }

            getDocument().setSum(sum);
        }

        @Override
        protected void doShow() {
            contractList = new ContractListHolderBean() {
                private static final long serialVersionUID = 1L;
                @Override
                protected Pagination initPagination() {
                    return new Pagination(0, getTotalCount(), 20);
                }
            };
            selectedContracts = new HashSet<Contract>();
        }

        public ContractListHolderBean getContractList() {
            return contractList;
        }

        public void setContractList(ContractListHolderBean contractList) {
            this.contractList = contractList;
        }

        public void select(Contract contract) {
            selectedContracts.add(contract);
        }

        public void unselect(Contract contrcat) {
            selectedContracts.remove(contrcat);
        }

        public boolean selected(Contract contract) {
            return selectedContracts.contains(contract);
        }

    }

    public static final String CONTRACT_VIEW = "/component/contract.xhtml";

    private static final long serialVersionUID = 1L;

    @Inject @Named("contractList")
    private transient ContractListHolderBean contractListHolder;

    private boolean viewCuratorPerson = false;

    private Organization newContractorToAdd = new Organization();

    private RegNumberModalHolder registrationNumberModal = new RegNumberModalHolder();

    private SumSelectorModalHolder sumSelectorModalHolder = new SumSelectorModalHolder();

    @Override
    protected boolean deleteDocument() {
        boolean result = ContractDaoEmulation.delete(getDocumentId());
        if (!result) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Can not delete contract.",
                    "Contract alraeady deleted."));
        }
        return result;
    }

    @Override
    protected Integer getDocumentId() {
        return getDocument() == null? null: getDocument().getId();
    }

    @Override
    protected void initDocument(Integer documentId) {
        setDocument(ContractDaoEmulation.get(documentId));
        if (getDocument() == null) {
            setState(STATE_NOT_FOUND);
        }
    }

    @Override
    protected void initNewDocument() {
        Contract result = new Contract();
        result.setAuthor(new Person());
        result.setCurator(new Person());
        result.setContractors(new ArrayList<Organization>());

        setDocument(result);
    }

    @Override
    protected boolean saveDocument() {
        boolean result = ContractDaoEmulation.save(getDocument());
        if (!result) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Can not save contract.",
                    "Contract alraeady deleted."));
        }
        return result;
    }

    @Override
    protected boolean saveNewDocument() {
        setDocument(ContractDaoEmulation.add(getDocument()));
        return true;
    }

    @Override
    public Contract getDocument() {
        return super.getDocument();
    }

    @Override
    protected FromStringConverter<Integer> getIdConverter() {
        return FromStringConverter.INTEGER_CONVERTER;
    }

    public boolean isViewCuratorPerson() {
        return viewCuratorPerson;
    }

    public void changePersonToAuthor() {
        this.viewCuratorPerson = false;
    }

    public void changePersonToCurator() {
        this.viewCuratorPerson = true;
    }

    public void deleteContror(int index) {
        if (index < getDocument().getContractors().size()) {
            getDocument().getContractors().remove(index);
        }
    }

    public Organization getNewContractorToAdd() {
        return newContractorToAdd;
    }

    public void addNewContractor() {
        getDocument().getContractors().add(newContractorToAdd);
        newContractorToAdd = new Organization();
    }

    public RegNumberModalHolder getRegistrationNumberModal() {
        return registrationNumberModal;
    }

    public SumSelectorModalHolder getSumSelectorModalHolder() {
        return sumSelectorModalHolder;
    }


    @Override
    protected String doAfterView() {
        return CONTRACT_VIEW;
    }

    @Override
    protected String doAfterCreate() {
        contractListHolder.markNeedRefresh();
        return super.doAfterCreate();
    }

    @Override
    protected String doAfterDelete() {
        contractListHolder.markNeedRefresh();
        return super.doAfterDelete();
    }

    @Override
    protected String doAfterSave() {
        contractListHolder.markNeedRefresh();
        return super.doAfterSave();
    }

}
