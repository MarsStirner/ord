package ru.efive.dms.uifaces.beans.request;

import ru.efive.dms.dao.RequestDocumentDAOImpl;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.uifaces.beans.user.UserListSelectModalBean;
import ru.efive.dms.uifaces.beans.user.UserSelectModalBean;
import ru.efive.uifaces.bean.AbstractDocumentListHolderBean;
import ru.entity.model.document.DeliveryType;
import ru.entity.model.document.DocumentForm;
import ru.entity.model.document.RequestDocument;
import ru.entity.model.user.User;
import ru.util.ApplicationHelper;

import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

import static ru.efive.dms.util.ApplicationDAONames.REQUEST_DOCUMENT_FORM_DAO;

@Named("request_search_documents")
@SessionScoped
public class RequestDocumentSearchListHolder extends AbstractDocumentListHolderBean<RequestDocument> {

    private static final String beanName = "request_search_documents";

    public boolean isNeedRefresh() {
        return needRefresh;
    }

    public void setNeedRefresh(boolean needRefresh) {
        if (needRefresh == true) {
            this.markNeedRefresh();
        }
        this.needRefresh = needRefresh;
    }

    protected List<RequestDocument> getHashDocuments(int fromIndex, int toIndex) {
        toIndex = (this.getHashDocuments().size() < fromIndex + toIndex) ? this.getHashDocuments().size() : fromIndex + toIndex;
        List<RequestDocument> result = new ArrayList<RequestDocument>(this.getHashDocuments().subList(fromIndex, toIndex));
        return result;
    }

    protected List<RequestDocument> getHashDocuments() {
        List<RequestDocument> result = new ArrayList<RequestDocument>();
        if (needRefresh) {
            try {
                User user = sessionManagement.getLoggedUser();
                //user = sessionManagement.getDAO(UserDAOHibernate.class,USER_DAO).findByLoginAndPassword(user.getLogin(), user.getPassword());
                result = new ArrayList<RequestDocument>(new HashSet<RequestDocument>(sessionManagement.getDAO(RequestDocumentDAOImpl.class, REQUEST_DOCUMENT_FORM_DAO).findAllDocumentsByUser(filters, filter, user, false, false)));

                Collections.sort(result, new Comparator<RequestDocument>() {
                    public int compare(RequestDocument o1, RequestDocument o2) {
                        int result = 0;
                        String colId = getSorting().getColumnId();

                        if (colId.equalsIgnoreCase("registrationDate")) {
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
                            if (c1.equals(c2)) {
                                try {
                                    Integer i1 = Integer.parseInt(ApplicationHelper.getNotNull(o1.getRegistrationNumber()));
                                    Integer i2 = Integer.parseInt(ApplicationHelper.getNotNull(o2.getRegistrationNumber()));
                                    result = i1.compareTo(i2);
                                } catch (NumberFormatException e) {
                                    result = ApplicationHelper.getNotNull(o1.getRegistrationNumber()).compareTo(ApplicationHelper.getNotNull(o2.getRegistrationNumber()));
                                }
                            } else {
                                result = c1.compareTo(c2);
                            }
                        } else if (colId.equalsIgnoreCase("registrationNumber")) {
                            try {
                                Integer i1 = Integer.parseInt(ApplicationHelper.getNotNull(o1.getRegistrationNumber()));
                                Integer i2 = Integer.parseInt(ApplicationHelper.getNotNull(o2.getRegistrationNumber()));
                                result = i1.compareTo(i2);
                            } catch (NumberFormatException e) {
                                result = ApplicationHelper.getNotNull(o1.getRegistrationNumber()).compareTo(ApplicationHelper.getNotNull(o2.getRegistrationNumber()));
                            }
                        } else if (colId.equalsIgnoreCase("deliveryDate")) {
                            Calendar c1 = Calendar.getInstance(ApplicationHelper.getLocale());
                            c1.setTime(ApplicationHelper.getNotNull(o1.getDeliveryDate()));
                            Calendar c2 = Calendar.getInstance(ApplicationHelper.getLocale());
                            c2.setTime(ApplicationHelper.getNotNull(o2.getDeliveryDate()));
                            result = c2.compareTo(c1);
                        } else if (colId.equalsIgnoreCase("contragent")) {
                            result = ApplicationHelper.getNotNull(o1.getSenderDescriptionShort()).compareTo(ApplicationHelper.getNotNull(o2.getSenderDescriptionShort()));
                        } else if (colId.equalsIgnoreCase("form")) {
                            result = ApplicationHelper.getNotNull(ApplicationHelper.getNotNull(o1.getForm()).toString()).compareTo(ApplicationHelper.getNotNull(ApplicationHelper.getNotNull(o2.getForm()).toString()));
                        } else if (colId.equalsIgnoreCase("executionDate")) {
                            Calendar c1 = Calendar.getInstance(ApplicationHelper.getLocale());
                            c1.setTime(ApplicationHelper.getNotNull(o1.getExecutionDate()));
                            Calendar c2 = Calendar.getInstance(ApplicationHelper.getLocale());
                            c2.setTime(ApplicationHelper.getNotNull(o2.getExecutionDate()));
                            result = c1.compareTo(c2);
                        } else if (colId.equalsIgnoreCase("status_id")) {
                            result = ApplicationHelper.getNotNull(ApplicationHelper.getNotNull(o1.getDocumentStatus()).getName()).compareTo(ApplicationHelper.getNotNull(ApplicationHelper.getNotNull(o2.getDocumentStatus()).getName()));
                        }

                        if (getSorting().isAsc()) {
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
    protected List<RequestDocument> loadDocuments() {
        List<RequestDocument> result = new ArrayList<RequestDocument>();
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
        try {
            markNeedRefresh();
            FacesContext.getCurrentInstance().getExternalContext().redirect("request_documents_by_conjunction_search.xhtml");
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
            this.setExecutor(null);
            this.setRecipientUsers(null);
            this.setShortDescription("");
            this.setStartCreationDate(null);
            this.setEndCreationDate(null);
            this.setForm(null);
            this.setStartDeliveryDate(null);
            this.setEndDeliveryDate(null);
            this.setSenderFirstName(null);
            this.setSenderMiddleName(null);
            this.setSenderLastName(null);
            this.setCopiesCount(0);
            this.setSheetsCount(0);
            this.setAppendixiesCount(0);
            this.setFundNumber(0);
            this.setStandNumber(0);
            this.setShelfNumber(0);
            this.setBoxNumber(0);
            this.setStatusId("");

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

    public void setExecutorSelectModal(UserSelectModalBean executorSelectModal) {
        this.executorSelectModal = executorSelectModal;
    }

    public void setRecipientUsersSelectModal(
            UserListSelectModalBean recipientUsersSelectModal) {
        this.recipientUsersSelectModal = recipientUsersSelectModal;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

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
    public UserSelectModalBean getAuthorSelectModal() {
        return authorSelectModal;
    }

    public UserSelectModalBean getExecutorSelectModal() {
        return executorSelectModal;
    }

    public UserListSelectModalBean getRecipientUsersSelectModal() {
        return recipientUsersSelectModal;
    }

    public void setRegistrationNumber(String registrationNumber) {
        filters.put("registrationNumber", registrationNumber);
    }

    /*public void setRegistrationDate(Date registrationDate) {
         filters.put("registrationDate",registrationDate);
     }*/
    public void setStartRegistrationDate(Date registrationDate) {
        filters.put("startRegistrationDate", registrationDate);
    }

    public void setEndRegistrationDate(Date registrationDate) {
        filters.put("endRegistrationDate", registrationDate);
    }


    public void setAuthor(User author) {
        filters.put("author", author);
    }

    /*public void setExecutionDate(Date executionDate) {
         filters.put("executionDate",executionDate);
     }*/

    public void setStartExecutionDate(Date startExecutionDate) {
        filters.put("startExecutionDate", startExecutionDate);
    }

    public void setEndExecutionDate(Date endExecutionDate) {
        filters.put("endExecutionDate", endExecutionDate);
    }

    public void setExecutor(User executor) {
        filters.put("executor", executor);
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

    public void setStartDeliveryDate(Date deliveryDate) {
        filters.put("startDeliveryDate", deliveryDate);
    }

    public void setEndDeliveryDate(Date deliveryDate) {
        filters.put("endDeliveryDate", deliveryDate);
    }

    public void setDeliveryType(DeliveryType deliveryType) {
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

    public void setSenderMiddleName(String senderMiddleName) {
        filters.put("senderMiddleName", senderMiddleName);
    }

    public void setSenderFirstName(String senderFirstName) {
        filters.put("senderFirstName", senderFirstName);
    }

    public void setSenderLastName(String senderLastName) {
        filters.put("senderLastName", senderLastName);
    }

    public void setStatusId(String statusId) {
        filters.put("statusId", statusId);
    }

    public String getContragent() {
        return (String) filters.get("contragent");
    }

    //public void setContragent(Contragent contragent) {
    //this.contragent = contragent);
    //}


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

    public Date getStartExecutionDate() {
        return (Date) filters.get("startExecutionDate");
    }

    public Date getEndExecutionDate() {
        return (Date) filters.get("endExecutionDate");
    }

    public User getController() {
        //if((User) filters.get("controller") !=null){System.out.println("######"+((User) filters.get("controller")).getLastName());}
        return (User) filters.get("controller");
    }

    public User getExecutor() {
        return (User) filters.get("executor");
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

    public Date getReceivedDocumentDate() {
        return (Date) filters.get("receivedDocumentDate");
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

    public String getSenderFirstName() {
        return (String) filters.get("senderFirstName");
    }

    public String getSenderLastName() {
        return (String) filters.get("senderLastName");
    }

    public String getSenderMiddleName() {
        return (String) filters.get("senderMiddleName");
    }

    public String getStatusId() {
        return (String) filters.get("statusId");
    }

    private String filter;
    /**
     * Набор пар ключ-значение для расширенного поиска
     */
    private Map<String, Object> filters = new HashMap<String, Object>();

    private List<RequestDocument> hashDocuments;
    private boolean needRefresh = true;

    @Inject
    @Named("sessionManagement")
    private transient SessionManagementBean sessionManagement;

    private static final long serialVersionUID = 8535420074467871583L;
}