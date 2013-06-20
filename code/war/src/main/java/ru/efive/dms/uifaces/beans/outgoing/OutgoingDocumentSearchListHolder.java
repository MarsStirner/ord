package ru.efive.dms.uifaces.beans.outgoing;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import ru.efive.crm.data.Contragent;
import ru.efive.sql.dao.user.UserDAOHibernate;
import ru.efive.sql.entity.user.User;
import ru.efive.dms.dao.OutgoingDocumentDAOImpl;
import ru.efive.dms.data.DeliveryType;
import ru.efive.dms.data.DocumentForm;
import ru.efive.dms.data.OfficeKeepingVolume;
import ru.efive.dms.data.OutgoingDocument;
import ru.efive.dms.uifaces.beans.ContragentListSelectModalBean;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.uifaces.beans.user.UserSelectModalBean;
import ru.efive.dms.util.ApplicationHelper;
import ru.efive.uifaces.bean.AbstractDocumentListHolderBean;

@Named("out_search_documents")
@SessionScoped
public class OutgoingDocumentSearchListHolder extends AbstractDocumentListHolderBean<OutgoingDocument> {

    private static final String beanName = "out_search_documents";

    public boolean isNeedRefresh() {
        return needRefresh;
    }

    public void setNeedRefresh(boolean needRefresh) {
        if (needRefresh == true) {
            this.markNeedRefresh();
        }
        this.needRefresh = needRefresh;
    }

    protected List<OutgoingDocument> getHashDocuments(int fromIndex, int toIndex) {
        List<OutgoingDocument> result = new ArrayList<OutgoingDocument>();
        if (needRefresh) {
            sessionManagement.registrateBeanName(beanName);
            try {
                User user = sessionManagement.getLoggedUser();
                user = sessionManagement.getDAO(UserDAOHibernate.class, ApplicationHelper.USER_DAO).findByLoginAndPassword(user.getLogin(), user.getPassword());
                this.hashDocuments = new ArrayList<OutgoingDocument>(new HashSet<OutgoingDocument>(sessionManagement.getDAO(OutgoingDocumentDAOImpl.class, ApplicationHelper.OUTGOING_DOCUMENT_FORM_DAO).findAllDocumentsByUser(filters, filter, user, false, false)));

                Collections.sort(this.hashDocuments, new Comparator<OutgoingDocument>() {
                    public int compare(OutgoingDocument o1, OutgoingDocument o2) {
                        Calendar c1 = Calendar.getInstance(ApplicationHelper.getLocale());
                        c1.setTime(o1.getCreationDate());
                        Calendar c2 = Calendar.getInstance(ApplicationHelper.getLocale());
                        c2.setTime(o2.getCreationDate());
                        return c2.compareTo(c1);
                    }
                });
                needRefresh = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        toIndex = (this.hashDocuments.size() < fromIndex + toIndex) ? this.hashDocuments.size() : fromIndex + toIndex;
        result = this.hashDocuments.subList(fromIndex, toIndex);
        return result;
    }

    protected List<OutgoingDocument> getHashDocuments() {
        List<OutgoingDocument> result = new ArrayList<OutgoingDocument>();
        if (needRefresh) {
            sessionManagement.registrateBeanName(beanName);
            try {
                User user = sessionManagement.getLoggedUser();
                user = sessionManagement.getDAO(UserDAOHibernate.class, ApplicationHelper.USER_DAO).findByLoginAndPassword(user.getLogin(), user.getPassword());
                result = new ArrayList<OutgoingDocument>(new HashSet<OutgoingDocument>(sessionManagement.getDAO(OutgoingDocumentDAOImpl.class, ApplicationHelper.OUTGOING_DOCUMENT_FORM_DAO).findAllDocumentsByUser(filters, filter, user, false, false)));
                Collections.sort(result, new Comparator<OutgoingDocument>() {
                    public int compare(OutgoingDocument o1, OutgoingDocument o2) {
                        Calendar c1 = Calendar.getInstance(ApplicationHelper.getLocale());
                        c1.setTime(o1.getCreationDate());
                        Calendar c2 = Calendar.getInstance(ApplicationHelper.getLocale());
                        c2.setTime(o2.getCreationDate());
                        return c2.compareTo(c1);
                    }
                });

                this.hashDocuments = result;
                needRefresh = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            result = this.hashDocuments;
        }
        return result;
    }

    protected Pagination initPagination() {
        return new Pagination(0, getTotalCount(), 100);
    }

    @Override
    protected Sorting initSorting() {
        return new Sorting("creationDate,id", false);
    }

    @Override
    protected int getTotalCount() {
        int result = 0;
        try {
            result = new Long(this.getHashDocuments().size()).intValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    protected List<OutgoingDocument> loadDocuments() {
        List<OutgoingDocument> result = new ArrayList<OutgoingDocument>();
        try {
            result = this.getHashDocuments(getPagination().getOffset(), getPagination().getPageSize());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public void refresh() {
        this.needRefresh = true;
        super.refresh();
    }

    public void search() {
        try {
            markNeedRefresh();
            FacesContext.getCurrentInstance().getExternalContext().redirect("out_documents_by_conjunction_search.xhtml");
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR, "Невозможно осуществить поиск", ""));
            e.printStackTrace();
        }
    }

    public void clear() {
        try {
            this.setRegistrationNumber(null);
            this.setStartRegistrationDate(null);
            this.setEndRegistrationDate(null);
            this.setStartSignatureDate(null);
            this.setEndSignatureDate(null);
            this.setAuthor(null);
            this.setStartSendingDate(null);
            this.setEndSendingDate(null);
            this.setRecipientContragents(null);
            this.setShortDescription("");
            this.setStartCreationDate(null);
            this.setEndCreationDate(null);
            this.setForm(null);
            this.setDeliveryType(null);
            this.setSigner(null);
            this.setExecutor(null);
            this.setCopiesCount(0);
            this.setSheetsCount(0);
            this.setAppendixiesCount(0);
            this.setFundNumber(0);
            this.setStandNumber(0);
            this.setShelfNumber(0);
            this.setBoxNumber(0);
            this.setStatusId("");
            this.setOfficeKeepingVolume(null);

            markNeedRefresh();
            //FacesContext.getCurrentInstance().getExternalContext().redirect("in_documents_by_conjunction_search.xhtml");
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR, "Невозможно осуществить поиск", ""));
            e.printStackTrace();
        }
    }

    public String getFilter() {
        return filter;
    }


    public void setSignerSelectModal(UserSelectModalBean signerSelectModal) {
        this.signerSelectModal = signerSelectModal;
    }

    public void setExecutorSelectModal(UserSelectModalBean executorSelectModal) {
        this.executorSelectModal = executorSelectModal;
    }

    public void setRecipientContragentsSelectModal(ContragentListSelectModalBean recipientContragentsSelectModal) {
        this.recipientContragentsSelectModal = recipientContragentsSelectModal;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    private UserSelectModalBean signerSelectModal = new UserSelectModalBean() {
        @Override
        protected void doSave() {
            super.doSave();
            filters.put("signer", getUser());
        }

        @Override
        protected void doHide() {
            super.doHide();
            getUserList().setFilter("");
            getUserList().markNeedRefresh();
            setUser(null);
        }
    };

    private UserSelectModalBean executorSelectModal = new UserSelectModalBean() {
        @Override
        protected void doSave() {
            super.doSave();
            filters.put("executor", getUser());
        }

        @Override
        protected void doHide() {
            super.doHide();
            getUserList().setFilter("");
            getUserList().markNeedRefresh();
            setUser(null);
        }
    };

    private UserSelectModalBean authorSelectModal = new UserSelectModalBean() {
        @Override
        protected void doSave() {
            super.doSave();
            filters.put("author", getUser());
        }

        @Override
        protected void doHide() {
            super.doHide();
            getUserList().setFilter("");
            getUserList().markNeedRefresh();
            setUser(null);
        }
    };

    private ContragentListSelectModalBean recipientContragentsSelectModal = new ContragentListSelectModalBean() {
        @Override
        protected void doSave() {
            super.doSave();
            filters.put("recipientContragents", getContragents());
        }

        @Override
        protected void doHide() {
            super.doHide();
            getContragentList().setFilter("");
            getContragentList().markNeedRefresh();
            setContragents(null);
        }
    };

    //--------------------------------------------------------------------------------------------------------//
    public UserSelectModalBean getAuthorSelectModal() {
        return authorSelectModal;
    }

    public UserSelectModalBean getExecutorSelectModal() {
        return executorSelectModal;
    }

    public UserSelectModalBean getSignerSelectModal() {
        return signerSelectModal;
    }

    public ContragentListSelectModalBean getRecipientContragentsSelectModal() {
        return recipientContragentsSelectModal;
    }

    public void setRegistrationNumber(String registrationNumber) {
        filters.put("registrationNumber", registrationNumber);
    }

    public void setStartRegistrationDate(Date registrationDate) {
        filters.put("startRegistrationDate", registrationDate);
    }

    public void setEndRegistrationDate(Date registrationDate) {
        filters.put("endRegistrationDate", registrationDate);
    }

    public void setStartSendingDate(Date sendingDate) {
        filters.put("startSendingDate", sendingDate);
    }

    public void setEndSendingDate(Date sendingDate) {
        filters.put("endSendingDate", sendingDate);
    }

    public void setAuthor(User author) {
        filters.put("author", author);
    }

    public void setStartSignatureDate(Date signatureDate) {
        filters.put("startSignatureDate", signatureDate);
    }

    public void setEndSignatureDate(Date signatureDate) {
        filters.put("endSignatureDate", signatureDate);
    }

    public void setSigner(User signer) {
        filters.put("signer", signer);
    }

    public void setExecutor(User executor) {
        filters.put("executor", executor);
    }

    public void setRecipientContragents(List<Contragent> recipientContragents) {
        filters.put("recipientContragents", recipientContragents);
    }

    public void setShortDescription(String shortDescription) {
        filters.put("shortDescription", shortDescription);
    }

    public void setStartCreationDate(Date creationDate) {
        filters.put("startCreationDate", creationDate);
    }

    public void setEndCreationDate(Date creationDate) {
        filters.put("endCreationDate", creationDate);
    }

    public Date getStartCreationDate() {
        return (Date) filters.get("startCreationDate");
    }

    public Date getEndCreationDate() {
        return (Date) filters.get("endCreationDate");
    }

    public void setForm(DocumentForm form) {
        //System.out.println("setForm"+form.getValue());
        //if(form.getValue().equals("любой")){form.setValue("");}
        filters.put("form", form);
    }

    public void setDeliveryType(DeliveryType deliveryType) {
        //if(deliveryType.getValue().equals("любой")){deliveryType.setValue("");}
        filters.put("deliveryType", deliveryType);
    }

    public void setCopiesCount(int copiesCount) {
        filters.put("copiesCount", copiesCount);
    }

    public void setSheetsCount(int sheetsCount) {
        filters.put("sheetsCount", sheetsCount);
    }

    public void setAppendixiesCount(int appendixiesCount) {
        filters.put("appendixiesCount", appendixiesCount);
    }

    public void setFundNumber(int fundNumber) {
        filters.put("fundNumber", fundNumber);
    }

    public void setStandNumber(int standNumber) {
        filters.put("standNumber", standNumber);
    }

    public void setShelfNumber(int shelfNumber) {
        filters.put("shelfNumber", shelfNumber);
    }

    public void setBoxNumber(int boxNumber) {
        filters.put("boxNumber", boxNumber);
    }

    public void setStatusId(String statusId) {
        filters.put("statusId", statusId);
    }

    public String getRegistrationNumber() {
        return (String) filters.get("registrationNumber");
    }

    public Date getStartRegistrationDate() {
        return (Date) filters.get("startRegistrationDate");
    }

    public Date getEndRegistrationDate() {
        return (Date) filters.get("endRegistrationDate");
    }

    public Date getStartSendingDate() {
        return (Date) filters.get("startSendingDate");
    }

    public Date getEndSendingDate() {
        return (Date) filters.get("endSendingDate");
    }

    public User getAuthor() {
        return (User) filters.get("author");
    }

    public Date getStartSignatureDate() {
        return (Date) filters.get("startSignatureDate");
    }

    public Date getEndSignatureDate() {
        return (Date) filters.get("endSignatureDate");
    }

    public User getSigner() {
        //if((User) filters.get("controller") !=null){System.out.println("######"+((User) filters.get("controller")).getLastName());}
        return (User) filters.get("signer");
    }

    public User getExecutor() {
        return (User) filters.get("executor");
    }

    public List<Contragent> getRecipientContragents() {
        return (List<Contragent>) filters.get("recipientContragents");
    }

    public String getShortDescription() {
        return (String) filters.get("shortDescription");
    }

    public DocumentForm getForm() {
        return (DocumentForm) filters.get("form");
    }

    public DeliveryType getDeliveryType() {
        return (DeliveryType) filters.get("deliveryType");
    }

    public int getCopiesCount() {
        return (Integer) filters.get("copiesCount");
    }

    public int getSheetsCount() {
        return (Integer) filters.get("sheetsCount");
    }

    public int getAppendixiesCount() {
        return (Integer) filters.get("appendixiesCount");
    }

    public int getFundNumber() {
        return (Integer) filters.get("fundNumber");
    }

    public int getStandNumber() {
        return (Integer) filters.get("standNumber");
    }

    public int getShelfNumber() {
        return (Integer) filters.get("shelfNumber");
    }

    public int getBoxNumber() {
        return (Integer) filters.get("boxNumber");
    }

    public String getStatusId() {
        return (String) filters.get("statusId");
    }

    public void setOfficeKeepingVolume(OfficeKeepingVolume volume) {
        filters.put("officeKeepingVolume", volume);
    }

    private String filter;
    /**
     * Набор пар ключ-значение для расширенного поиска
     */
    private Map<String, Object> filters = new HashMap<String, Object>();

    private List<OutgoingDocument> hashDocuments;
    private boolean needRefresh = true;

    @Inject
    @Named("sessionManagement")
    private transient SessionManagementBean sessionManagement;

    private static final long serialVersionUID = 8535420074467871583L;
}