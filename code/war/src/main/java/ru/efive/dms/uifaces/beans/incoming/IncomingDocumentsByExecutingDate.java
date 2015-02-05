package ru.efive.dms.uifaces.beans.incoming;

import org.joda.time.LocalDate;
import org.primefaces.model.DefaultTreeNode;
import ru.efive.dms.dao.IncomingDocumentDAOImpl;
import ru.efive.dms.dao.ViewFactDaoImpl;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.uifaces.bean.AbstractDocumentTreeHolderBean;
import ru.efive.uifaces.bean.Pagination;
import ru.entity.model.document.IncomingDocument;
import ru.entity.model.user.User;
import ru.util.ApplicationHelper;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static ru.efive.dms.util.ApplicationDAONames.INCOMING_DOCUMENT_FORM_DAO;
import static ru.efive.dms.util.ApplicationDAONames.VIEW_FACT_DAO;

@Named("in_documents_by_executing_date")
@SessionScoped
public class IncomingDocumentsByExecutingDate extends AbstractDocumentTreeHolderBean<IncomingDocument> {

    private DateFormatSymbols dateFormatSymbols;
    private final static SimpleDateFormat year_month_day = new SimpleDateFormat("yyyy,MM,dd");
    private String filter;
    private boolean showExpiredFlag = false;
    private LocalDate currentDate;

    /**
     * Сменить значение флажка "Показать просроченные"
     *
     * @return новое значение флага
     */
    public boolean changeExpiredFlag() {
        showExpiredFlag = !showExpiredFlag;
        if (showExpiredFlag) {
            currentDate = new LocalDate();
        }
        refresh();
        return showExpiredFlag;
    }

    @Override
    protected List<IncomingDocument> loadDocuments(final Pagination pagination) {
        final List<IncomingDocument> resultList;
        if (!sessionManagement.isSubstitution()) {
            // Без замещения
            if (showExpiredFlag) {
                //для просроченных
                resultList = sessionManagement.getDAO(IncomingDocumentDAOImpl.class, INCOMING_DOCUMENT_FORM_DAO)
                        .findControlledDocumentsByUser(filter, sessionManagement.getLoggedUser(), false, currentDate.toDate(), pagination.getOffset(), pagination.getPageSize(), "executionDate", false);
            } else {
                // все
                resultList = sessionManagement.getDAO(IncomingDocumentDAOImpl.class, INCOMING_DOCUMENT_FORM_DAO)
                        .findControlledDocumentsByUser(filter, sessionManagement.getLoggedUser(), false, pagination.getOffset(), pagination.getPageSize(), "executionDate", false);
            }
        } else {
            //С замещением
            final List<User> userList = new ArrayList<User>(sessionManagement.getSubstitutedUsers());
            userList.add(sessionManagement.getLoggedUser());
            if (showExpiredFlag) {
                //для просроченных
                resultList = sessionManagement.getDAO(IncomingDocumentDAOImpl.class, INCOMING_DOCUMENT_FORM_DAO)
                        .findControlledDocumentsByUserList(filter, userList, false, currentDate.toDate(), pagination.getOffset(), pagination.getPageSize(), "executionDate", false);
            } else {
                // все
                resultList = sessionManagement.getDAO(IncomingDocumentDAOImpl.class, INCOMING_DOCUMENT_FORM_DAO)
                        .findControlledDocumentsByUserList(filter, userList, false, pagination.getOffset(), pagination.getPageSize(), "executionDate", false);
            }
        }
        if(!resultList.isEmpty()){
            sessionManagement.getDAO(ViewFactDaoImpl.class, VIEW_FACT_DAO).applyViewFlagsOnIncomingDocumentList(resultList, sessionManagement.getLoggedUser());
        }
        return resultList;
    }

    protected int getTotalCount() {
        if (!sessionManagement.isSubstitution()) {
            // Без замещения
            if (showExpiredFlag) {
                return new Long(
                        sessionManagement.getDAO(IncomingDocumentDAOImpl.class, INCOMING_DOCUMENT_FORM_DAO)
                                .countControlledDocumentsByUser(filter, sessionManagement.getLoggedUser(), false, currentDate.toDate())
                ).intValue();
            } else {
                return new Long(
                        sessionManagement.getDAO(IncomingDocumentDAOImpl.class, INCOMING_DOCUMENT_FORM_DAO)
                                .countControlledDocumentsByUser(filter, sessionManagement.getLoggedUser(), false)
                ).intValue();
            }
        } else {
            //С замещением
            final List<User> userList = new ArrayList<User>(sessionManagement.getSubstitutedUsers());
            userList.add(sessionManagement.getLoggedUser());
            if (showExpiredFlag) {
                return new Long(
                        sessionManagement.getDAO(IncomingDocumentDAOImpl.class, INCOMING_DOCUMENT_FORM_DAO)
                                .countControlledDocumentsByUserList(filter, userList, false, currentDate.toDate())
                ).intValue();
            } else {
                return new Long(
                        sessionManagement.getDAO(IncomingDocumentDAOImpl.class, INCOMING_DOCUMENT_FORM_DAO)
                                .countControlledDocumentsByUserList(filter, userList, false)
                ).intValue();
            }
        }

    }

    @Override
    protected DefaultTreeNode constructTreeFromDocumentList(final List<IncomingDocument> documents) {
        if (dateFormatSymbols == null) {
            dateFormatSymbols = new DateFormatSymbols(ApplicationHelper.getLocale());
        }
        final DefaultTreeNode rootElement = new DefaultTreeNode("ROOT", null);
        Iterator<IncomingDocument> iterator = documents.iterator();
        DefaultTreeNode lastYearNode = null;
        DefaultTreeNode lastMonthNode = null;
        String lastYear = "";
        String lastMonth = "";
        while (iterator.hasNext()) {
            IncomingDocument current = iterator.next();
            String[] date = year_month_day.format(current.getExecutionDate()).split(",");
            if (!lastYear.equals(date[0])) {
                lastYearNode = new DefaultTreeNode(
                        "year",
                        new IncomingDocumentNode(
                                IncomingDocumentNode.DOC_TYPE.YEAR,
                                date[0]
                        ),
                        rootElement
                );
                lastYear = date[0];
            }
            if (!lastMonth.equals(date[1])) {
                lastMonthNode = new DefaultTreeNode(
                        "month",
                        new IncomingDocumentNode(
                                IncomingDocumentNode.DOC_TYPE.MONTH,
                                dateFormatSymbols.getMonths()[Integer.valueOf(date[1]) - 1]
                        ),
                        lastYearNode
                );
                lastMonth = date[1];
            }
            new DefaultTreeNode("document", new IncomingDocumentNode(current, date[2]), lastMonthNode);
        }
        return rootElement;
    }

    @Override
    protected Pagination initPagination() {
        return new Pagination(0, getTotalCount(), 100);
    }


    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public boolean getShowExpiredFlag() {
        return showExpiredFlag;
    }

    public void setShowExpiredFlag(boolean showExpiredFlag) {
        this.showExpiredFlag = showExpiredFlag;
    }

    @Inject
    @Named("sessionManagement")
    private transient SessionManagementBean sessionManagement;
}