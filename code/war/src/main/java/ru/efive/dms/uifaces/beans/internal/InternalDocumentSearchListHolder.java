package ru.efive.dms.uifaces.beans.internal;

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

import ru.efive.dms.dao.InternalDocumentDAOImpl;
import ru.efive.dms.data.DocumentForm;
import ru.efive.dms.data.InternalDocument;
import ru.efive.dms.data.OfficeKeepingVolume;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.uifaces.beans.user.UserListSelectModalBean;
import ru.efive.dms.uifaces.beans.user.UserSelectModalBean;
import ru.efive.dms.util.ApplicationHelper;
import ru.efive.sql.dao.user.UserDAOHibernate;
import ru.efive.sql.entity.user.User;
import ru.efive.uifaces.bean.AbstractDocumentListHolderBean;

@Named("internal_search_documents")
@SessionScoped
public class InternalDocumentSearchListHolder extends AbstractDocumentListHolderBean<InternalDocument> {

    private static final String beanName = "internal_search_documents";

    public boolean isNeedRefresh() {
        return needRefresh;
    }

    public void setNeedRefresh(boolean needRefresh) {
        if (needRefresh == true) {
            this.markNeedRefresh();
        }
        this.needRefresh = needRefresh;
    }

    protected List<InternalDocument> getHashDocuments(int fromIndex, int toIndex) {
        toIndex = (this.getHashDocuments().size() < fromIndex + toIndex) ? this.getHashDocuments().size() : fromIndex + toIndex;
        return new ArrayList<InternalDocument>(this.getHashDocuments().subList(fromIndex, toIndex));
    }

    protected List<InternalDocument> getHashDocuments() {
        List<InternalDocument> result = new ArrayList<InternalDocument>();
        if (needRefresh) {
            sessionManagement.registrateBeanName(beanName);
            try {
                User user = sessionManagement.getLoggedUser();
                user = sessionManagement.getDAO(UserDAOHibernate.class, ApplicationHelper.USER_DAO).findByLoginAndPassword(user.getLogin(), user.getPassword());
                result = new ArrayList<InternalDocument>(new HashSet<InternalDocument>(sessionManagement.getDAO(InternalDocumentDAOImpl.class, ApplicationHelper.INTERNAL_DOCUMENT_FORM_DAO).findAllDocumentsByUser(filters, filter, user, false, false)));

                Collections.sort(result, new Comparator<InternalDocument>() {
                    public int compare(InternalDocument o1, InternalDocument o2) {
                        int result = 0;
                        String colId = ApplicationHelper.getNotNull(getSorting().getColumnId());

                        if(colId.equalsIgnoreCase("registrationDate")) {
                            Date d1 = ApplicationHelper.getNotNull(o1.getRegistrationDate());
                            Calendar c1 = Calendar.getInstance(ApplicationHelper.getLocale());
                            c1.setTime(d1);
                            c1.set(Calendar.HOUR_OF_DAY, 0);
                            c1.set(Calendar.MINUTE, 0);
                            c1.set(Calendar.SECOND, 0);
                            Date d2 = ApplicationHelper.getNotNull(o2.getRegistrationDate());
                            Calendar c2 = Calendar.getInstance(ApplicationHelper.getLocale());
                            c2.setTime(d2);
                            c2.set(Calendar.HOUR_OF_DAY, 0);
                            c2.set(Calendar.MINUTE, 0);
                            c2.set(Calendar.SECOND, 0);
                            if(c1.equals(c2)) {
                                try {
                                    Integer i1 = Integer.parseInt(ApplicationHelper.getNotNull(o1.getRegistrationNumber()));
                                    Integer i2 = Integer.parseInt(ApplicationHelper.getNotNull(o2.getRegistrationNumber()));
                                    result = i1.compareTo(i2);
                                } catch(NumberFormatException e) {
                                    result = ApplicationHelper.getNotNull(o1.getRegistrationNumber()).compareTo(ApplicationHelper.getNotNull(o2.getRegistrationNumber()));
                                }
                            } else {
                                result = c1.compareTo(c2);
                            }
                        }

                        if(getSorting().isAsc()) {
                            result *= -1;
                        }
                        return result;
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
        return new Sorting("registrationDate", true);
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
    protected List<InternalDocument> loadDocuments() {
        List<InternalDocument> result = new ArrayList<InternalDocument>();
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
            FacesContext.getCurrentInstance().getExternalContext().redirect("internal_documents_by_conjunction_search.xhtml");
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
            this.setStartExecutionDate(null);
            this.setEndExecutionDate(null);
            this.setInitiator(null);
            this.setStartExecutionDate(null);
            this.setEndExecutionDate(null);
            this.setSigner(null);
            this.setResponsible(null);
            this.setRecipientUsers(null);
            this.setShortDescription("");
            this.setStartCreationDate(null);
            this.setEndCreationDate(null);
            this.setForm(null);
            this.setRecipientUsers(null);
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

    public void setSignerSelectModal(UserSelectModalBean sigerSelectModal) {
        this.signerSelectModal = signerSelectModal;
    }

    public void setResponsibleSelectModal(UserSelectModalBean responsibleSelectModal) {
        this.responsibleSelectModal = responsibleSelectModal;
    }

    public void setRecipientUsersSelectModal(
            UserListSelectModalBean recipientUsersSelectModal) {
        this.recipientUsersSelectModal = recipientUsersSelectModal;
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

    private UserSelectModalBean responsibleSelectModal = new UserSelectModalBean() {
        @Override
        protected void doSave() {
            super.doSave();
            filters.put("responsible", getUser());
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
        protected void doSave() {
            super.doSave();
            filters.put("initiator", getUser());
        }

        @Override
        protected void doHide() {
            super.doHide();
            getUserList().setFilter("");
            getUserList().markNeedRefresh();
            setUser(null);
        }
    };

    private UserListSelectModalBean recipientUsersSelectModal = new UserListSelectModalBean() {

        @Override
        protected void doSave() {
            super.doSave();
            filters.put("recipientUsers", getUsers());
        }

        @Override
        protected void doHide() {
            super.doHide();
            getUserList().setFilter("");
            getUserList().markNeedRefresh();
            setUsers(null);
        }
    };

    //--------------------------------------------------------------------------------------------------------//
    public UserSelectModalBean getInitiatorSelectModal() {
        return initiatorSelectModal;
    }

    public UserSelectModalBean getResponsibleSelectModal() {
        return responsibleSelectModal;
    }

    public UserSelectModalBean getSignerSelectModal() {
        return signerSelectModal;
    }

    public UserListSelectModalBean getRecipientUsersSelectModal() {
        return recipientUsersSelectModal;
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

    public void setStartSignatureDate(Date signatureDate) {
        filters.put("startSignatureDate", signatureDate);
    }

    public void setEndSignatureDate(Date signatureDate) {
        filters.put("endSignatureDate", signatureDate);
    }

    public void setInitiator(User initiator) {
        filters.put("initiator", initiator);
    }

    public void setStartExecutionDate(Date executionDate) {
        filters.put("startExecutionDate", executionDate);
    }

    public void setEndExecutionDate(Date executionDate) {
        filters.put("endExecutionDate", executionDate);
    }

    public void setSigner(User signer) {
        filters.put("signer", signer);
    }

    public void setResponsible(User responsible) {
        filters.put("responsible", responsible);
    }

    public void setRecipientUsers(List<User> recipientUsers) {
        filters.put("recipientUsers", recipientUsers);
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
        filters.put("form", form);
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

    public User getInitiator() {
        return (User) filters.get("initiator");
    }

    public Date getStartExecutionDate() {
        return (Date) filters.get("startExecutionDate");
    }

    public Date getEndExecutionDate() {
        return (Date) filters.get("endExecutionDate");
    }

    public User getSigner() {
        return (User) filters.get("signer");
    }

    public User getResponsible() {
        return (User) filters.get("responsible");
    }

    public List<User> getRecipientUsers() {
        return (List<User>) filters.get("recipientUsers");
    }

    public String getShortDescription() {
        return (String) filters.get("shortDescription");
    }

    public DocumentForm getForm() {
        return (DocumentForm) filters.get("form");
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

    private List<InternalDocument> hashDocuments;
    private boolean needRefresh = true;

    @Inject
    @Named("sessionManagement")
    private transient SessionManagementBean sessionManagement;

    private static final long serialVersionUID = 8535420074467871583L;
}