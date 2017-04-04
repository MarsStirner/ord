package ru.efive.dms.uifaces.beans.incoming;

import com.github.javaplugs.jsf.SpringScopeView;
import org.primefaces.model.DefaultTreeNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.uifaces.bean.AbstractDocumentTreeHolderBean;
import ru.entity.model.document.IncomingDocument;
import ru.hitsl.sql.dao.interfaces.ViewFactDao;
import ru.hitsl.sql.dao.interfaces.document.IncomingDocumentDao;
import ru.hitsl.sql.dao.util.AuthorizationData;
import ru.util.ApplicationHelper;

import org.springframework.stereotype.Controller;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;

@Controller("in_documents_by_executing_date")
@SpringScopeView
public class IncomingDocumentsByExecutingDate extends AbstractDocumentTreeHolderBean<IncomingDocument> {

    private final static SimpleDateFormat year_month_day = new SimpleDateFormat("yyyy,MM,dd");
    private DateFormatSymbols dateFormatSymbols;
    private String filter;
    private boolean showExpiredFlag = false;
    @Autowired
    @Qualifier("incomingDocumentDao")
    private IncomingDocumentDao incomingDocumentDao;
    @Autowired
    @Qualifier("viewFactDao")
    private ViewFactDao viewFactDao;
    @Autowired
    @Qualifier("authData")
    private AuthorizationData authData;

    /**
     * Сменить значение флажка "Показать просроченные"
     *
     * @return новое значение флага
     */
    public boolean changeExpiredFlag() {
        showExpiredFlag = !showExpiredFlag;
        refresh();
        return showExpiredFlag;
    }

    @Override
    protected List<IncomingDocument> loadDocuments() {
        final List<IncomingDocument> resultList = incomingDocumentDao
                .findControlledDocumentsByUser(
                        filter, authData, showExpiredFlag ? LocalDateTime.now() : null
                );
        if (!resultList.isEmpty()) {
            viewFactDao.applyViewFlagsOnDocumentList(
                    resultList, authData.getAuthorized()
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