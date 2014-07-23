package ru.efive.dms.uifaces.beans.incoming;

import ru.efive.crm.data.Contragent;
import ru.efive.dms.dao.IncomingDocumentDAOImpl;
import ru.efive.dms.data.DeliveryType;
import ru.efive.dms.data.DocumentForm;
import ru.efive.dms.data.IncomingDocument;
import ru.efive.dms.data.OfficeKeepingVolume;
import ru.efive.dms.uifaces.beans.ContragentSelectModalBean;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.uifaces.beans.officekeeping.OfficeKeepingVolumeSelectModal;
import ru.efive.dms.uifaces.beans.user.UserListSelectModalBean;
import ru.efive.dms.uifaces.beans.user.UserSelectModalBean;
import ru.efive.dms.util.ApplicationHelper;
import ru.efive.sql.dao.user.UserDAOHibernate;
import ru.efive.sql.entity.user.User;
import ru.efive.uifaces.bean.AbstractDocumentListHolderBean;

import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

@Named("in_search_documents")
@SessionScoped
public class IncomingDocumentSearchListHolder extends AbstractDocumentListHolderBean<IncomingDocument> {
    private static final String beanName = "in_search_documents";

    public boolean isNeedRefresh() {
        return needRefresh;
    }

    public void setNeedRefresh(boolean needRefresh) {
        if (needRefresh == true) {
            this.markNeedRefresh();
        }
        this.needRefresh = needRefresh;
    }

    protected List<IncomingDocument> getHashDocuments(int fromIndex, int toIndex) {
        toIndex = (this.getHashDocuments().size() < fromIndex + toIndex) ? this.getHashDocuments().size() : fromIndex + toIndex;
        List<IncomingDocument> result = new ArrayList<IncomingDocument>(this.getHashDocuments().subList(fromIndex, toIndex));
        return result;
    }

    protected List<IncomingDocument> getHashDocuments() {
        List<IncomingDocument> result = new ArrayList<IncomingDocument>();
        if (needRefresh) {
            sessionManagement.registrateBeanName(beanName);
            try {
                User user = sessionManagement.getLoggedUser();
                //user = sessionManagement.getDAO(UserDAOHibernate.class, ApplicationHelper.USER_DAO).findByLoginAndPassword(user.getLogin(), user.getPassword());
                result = new ArrayList<IncomingDocument>(new HashSet<IncomingDocument>(sessionManagement.
                        getDAO(IncomingDocumentDAOImpl.class, ApplicationHelper.INCOMING_DOCUMENT_FORM_DAO).findAllDocumentsByUser(filters, filter, user, false, false)));

                Collections.sort(result, new Comparator<IncomingDocument>() {
                    public int compare(IncomingDocument o1, IncomingDocument o2) {
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
                        } else if(colId.equalsIgnoreCase("registrationNumber")) {
                            try {
                                Integer i1 = Integer.parseInt(ApplicationHelper.getNotNull(o1.getRegistrationNumber()));
                                Integer i2 = Integer.parseInt(ApplicationHelper.getNotNull(o2.getRegistrationNumber()));
                                result = i1.compareTo(i2);
                            } catch(NumberFormatException e) {
                                result = ApplicationHelper.getNotNull(o1.getRegistrationNumber()).compareTo(ApplicationHelper.getNotNull(o2.getRegistrationNumber()));
                            }
                        } else if(colId.equalsIgnoreCase("deliveryDate")) {
                            Calendar c1 = Calendar.getInstance(ApplicationHelper.getLocale());
                            c1.setTime(ApplicationHelper.getNotNull(o1.getDeliveryDate()));
                            Calendar c2 = Calendar.getInstance(ApplicationHelper.getLocale());
                            c2.setTime(ApplicationHelper.getNotNull(o2.getDeliveryDate()));
                            result = c2.compareTo(c1);
                        } else if(colId.equalsIgnoreCase("contragent")) {
                            result = ApplicationHelper.getNotNull(ApplicationHelper.getNotNull(o1.getContragent()).getShortName()).compareTo(ApplicationHelper.getNotNull(ApplicationHelper.getNotNull(o2.getContragent()).getShortName()));
                        } else if(colId.equalsIgnoreCase("form")) {
                            result = ApplicationHelper.getNotNull(ApplicationHelper.getNotNull(o1.getForm()).toString()).compareTo(ApplicationHelper.getNotNull(ApplicationHelper.getNotNull(o2.getForm()).toString()));
                        } else if(colId.equalsIgnoreCase("executionDate")) {
                            Calendar c1 = Calendar.getInstance(ApplicationHelper.getLocale());
                            c1.setTime(ApplicationHelper.getNotNull(o1.getExecutionDate()));
                            Calendar c2 = Calendar.getInstance(ApplicationHelper.getLocale());
                            c2.setTime(ApplicationHelper.getNotNull(o2.getExecutionDate()));
                            result = c1.compareTo(c2);
                        } else if(colId.equalsIgnoreCase("status_id")) {
                            result = ApplicationHelper.getNotNull(ApplicationHelper.getNotNull(o1.getDocumentStatus()).getName()).compareTo(ApplicationHelper.getNotNull(ApplicationHelper.getNotNull(o2.getDocumentStatus()).getName()));
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
    protected List<IncomingDocument> loadDocuments() {
        List<IncomingDocument> result = new ArrayList<IncomingDocument>();
        try {
            this.needRefresh = true;
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
        this.needRefresh = true;
        try {
            markNeedRefresh();
            FacesContext.getCurrentInstance().getExternalContext().redirect("in_documents_by_conjunction_search.xhtml");
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
            this.setStartDeliveryDate(null);
            this.setEndDeliveryDate(null);
            this.setAuthor(null);
            this.setStartExecutionDate(null);
            this.setEndExecutionDate(null);
            this.setController(null);
            this.setExecutors(null);
            this.setContragent(null);
            this.setRecipientUsers(null);
            this.setShortDescription("");
            this.setStartCreationDate(null);
            this.setEndCreationDate(null);
            this.setStartReceivedDocumentDate(null);
            this.setEndCreationDate(null);
            this.setForm(null);
            this.setDeliveryType(null);
            this.setReceivedDocumentNumber("");
            this.setStartReceivedDocumentDate(null);
            this.setEndReceivedDocumentDate(null);
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

    public void setControllerSelectModal(UserSelectModalBean controllerSelectModal) {
        this.controllerSelectModal = controllerSelectModal;
    }

    public void setExecutorsSelectModal(UserListSelectModalBean executorsSelectModal) {
        this.executorsSelectModal = executorsSelectModal;
    }

    public void setRecipientUsersSelectModal(
            UserListSelectModalBean recipientUsersSelectModal) {
        this.recipientUsersSelectModal = recipientUsersSelectModal;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    private ContragentSelectModalBean contragentSelectModal = new ContragentSelectModalBean() {
        @Override
        protected void doSave() {
            super.doSave();
            filters.put("contragent", getContragent());
        }

        @Override
        protected void doHide() {
            super.doHide();
            getContragentList().setFilter("");
            getContragentList().markNeedRefresh();
            setContragent(null);
        }
    };

    private UserSelectModalBean controllerSelectModal = new UserSelectModalBean() {
        @Override
        protected void doSave() {
            super.doSave();
            filters.put("controller", getUser());
        }

        @Override
        protected void doHide() {
            super.doHide();
            getUserList().setFilter("");
            getUserList().markNeedRefresh();
            setUser(null);
        }
    };

    private UserListSelectModalBean executorsSelectModal = new UserListSelectModalBean() {
        @Override
        protected void doSave() {
            super.doSave();
            filters.put("executors", getUsers());
        }

        @Override
        protected void doHide() {
            super.doHide();
            getUserList().setFilter("");
            getUserList().markNeedRefresh();
            setUsers(null);
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

    public OfficeKeepingVolumeSelectModal getOfficeKeepingVolumeSelectModal() {
        return officeKeepingVolumeSelectModal;
    }

    private OfficeKeepingVolumeSelectModal officeKeepingVolumeSelectModal = new OfficeKeepingVolumeSelectModal() {

        @Override
        protected void doSave() {
            super.doSave();
            filters.put("officeKeepingVolume", getOfficeKeepingVolume());
        }

        @Override
        protected void doHide() {
            super.doHide();
            getOfficeKeepingVolumes().markNeedRefresh();
        }
    };

    //--------------------------------------------------------------------------------------------------------//

    public void setRegistrationNumber(String registrationNumber) {
        filters.put("registrationNumber", registrationNumber);
    }

    public void setStartRegistrationDate(Date registrationDate) {
        filters.put("startRegistrationDate", registrationDate);
    }

    public void setEndRegistrationDate(Date registrationDate) {
        filters.put("endRegistrationDate", registrationDate);
    }

    public void setStartDeliveryDate(Date deliveryDate) {
        filters.put("startDeliveryDate", deliveryDate);
    }

    public void setEndDeliveryDate(Date deliveryDate) {
        filters.put("endDeliveryDate", deliveryDate);
    }

    public void setAuthor(User author) {
        filters.put("author", author);
    }

    public void setStartExecutionDate(Date executionDate) {
        filters.put("startExecutionDate", executionDate);
    }

    public void setEndExecutionDate(Date executionDate) {
        filters.put("endExecutionDate", executionDate);
    }

    public void setController(User controller) {
        filters.put("controller", controller);
    }

    public void setExecutors(List<User> executors) {
        filters.put("executors", executors);
    }

    public void setContragent(Contragent contragent) {
        filters.put("contragent", contragent);
    }

    public void setOfficeKeepingVolume(OfficeKeepingVolume volume) {
        filters.put("officeKeepingVolume", volume);
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

    public Date getStartCreationDate() {
        return (Date) filters.get("startCreationDate");
    }

    public void setEndCreationDate(Date creationDate) {
        filters.put("endCreationDate", creationDate);
    }

    public Date getEndCreationDate() {
        return (Date) filters.get("endCreationDate");
    }

    public void setForm(DocumentForm form) {
        filters.put("form", form);
    }

    public void setDeliveryType(DeliveryType deliveryType) {
        filters.put("deliveryType", deliveryType);
    }

    public void setReceivedDocumentNumber(String receivedDocumentNumber) {
        filters.put("receivedDocumentNumber", receivedDocumentNumber);
    }

    public void setStartReceivedDocumentDate(Date receivedDocumentDate) {
        filters.put("startReceivedDocumentDate", receivedDocumentDate);
    }

    public void setEndReceivedDocumentDate(Date receivedDocumentDate) {
        filters.put("endReceivedDocumentDate", receivedDocumentDate);
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

    public ContragentSelectModalBean getContragentSelectModal() {
        return contragentSelectModal;
    }

    public UserSelectModalBean getAuthorSelectModal() {
        return authorSelectModal;
    }

    public UserListSelectModalBean getExecutorsSelectModal() {
        return executorsSelectModal;
    }

    public UserSelectModalBean getControllerSelectModal() {
        return controllerSelectModal;
    }

    public UserListSelectModalBean getRecipientUsersSelectModal() {
        return recipientUsersSelectModal;
    }

    public Contragent getContragent() {
        return (Contragent) filters.get("contragent");
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

    public Date getStartDeliveryDate() {
        return (Date) filters.get("startDeliveryDate");
    }

    public Date getEndDeliveryDate() {
        return (Date) filters.get("endDeliveryDate");
    }

    public User getAuthor() {
        return (User) filters.get("author");
    }

    public OfficeKeepingVolume getOfficeKeepingVolume() {
        return (OfficeKeepingVolume) filters.get("officeKeepingVolume");
    }

    public Date getStartExecutionDate() {
        return (Date) filters.get("startExecutionDate");
    }

    public Date getEndExecutionDate() {
        return (Date) filters.get("endExecutionDate");
    }

    public User getController() {
        return (User) filters.get("controller");
    }

    public List<User> getExecutors() {
        return (List<User>) filters.get("executors");
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

    public DeliveryType getDeliveryType() {
        return (DeliveryType) filters.get("deliveryType");
    }

    public String getReceivedDocumentNumber() {
        return (String) filters.get("receivedDocumentNumber");
    }

    public Date getStartReceivedDocumentDate() {
        return (Date) filters.get("startReceivedDocumentDate");
    }

    public Date getEndReceivedDocumentDate() {
        return (Date) filters.get("endReceivedDocumentDate");
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

    private String filter;
    /**
     * Набор пар ключ-значение для расширенного поиска
     */
    private Map<String, Object> filters = new HashMap<String, Object>();

    private List<IncomingDocument> hashDocuments;
    private boolean needRefresh = true;

    @Inject
    @Named("sessionManagement")
    private transient SessionManagementBean sessionManagement;

    private static final long serialVersionUID = 8535420074467871583L;
}