package ru.efive.dms.uifaces.beans.incoming;

import org.joda.time.LocalDate;
import org.primefaces.model.DefaultTreeNode;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.uifaces.bean.AbstractDocumentTreeHolderBean;
import ru.entity.model.document.IncomingDocument;
import ru.hitsl.sql.dao.IncomingDocumentDAOImpl;
import ru.hitsl.sql.dao.ViewFactDaoImpl;
import ru.util.ApplicationHelper;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;

import static ru.hitsl.sql.dao.util.ApplicationDAONames.INCOMING_DOCUMENT_FORM_DAO;
import static ru.hitsl.sql.dao.util.ApplicationDAONames.VIEW_FACT_DAO;

@Named("in_documents_by_executing_date")
@ViewScoped
public class IncomingDocumentsByExecutingDate extends AbstractDocumentTreeHolderBean<IncomingDocument> {

    private final static SimpleDateFormat year_month_day = new SimpleDateFormat("yyyy,MM,dd");
    private DateFormatSymbols dateFormatSymbols;
    private String filter;
    private boolean showExpiredFlag = false;
    private LocalDate currentDate;
    @Inject
    @Named("sessionManagement")
    private transient SessionManagementBean sessionManagement;

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
    protected List<IncomingDocument> loadDocuments() {
        final List<IncomingDocument> resultList = sessionManagement.getDAO(IncomingDocumentDAOImpl.class, INCOMING_DOCUMENT_FORM_DAO)
                .findControlledDocumentsByUser(
                        filter, sessionManagement.getAuthData(), showExpiredFlag ? currentDate.toDate() : null
                );
        if (!resultList.isEmpty()) {
            sessionManagement.getDAO(ViewFactDaoImpl.class, VIEW_FACT_DAO).applyViewFlagsOnIncomingDocumentList(
                    resultList, sessionManagement.getAuthData().getAuthorized()
            );
        }
        return resultList;
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
            boolean samePeriod = true;
            IncomingDocument current = iterator.next();
            String[] date = year_month_day.format(current.getExecutionDate()).split(",");
            if (!lastYear.equals(date[0])) {
                lastYearNode = new DefaultTreeNode(
                        "year", new IncomingDocumentNode(
                        IncomingDocumentNode.DOC_TYPE.YEAR, date[0]
                ), rootElement
                );
                lastYear = date[0];
                samePeriod = false;
            }
            if (!lastMonth.equals(date[1]) || !samePeriod) {
                lastMonthNode = new DefaultTreeNode(
                        "month", new IncomingDocumentNode(
                        IncomingDocumentNode.DOC_TYPE.MONTH, dateFormatSymbols.getMonths()[Integer.valueOf(date[1]) - 1]
                ), lastYearNode
                );
                lastMonth = date[1];
            }
            new DefaultTreeNode("document", new IncomingDocumentNode(current, date[2]), lastMonthNode);
        }
        return rootElement;
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
}