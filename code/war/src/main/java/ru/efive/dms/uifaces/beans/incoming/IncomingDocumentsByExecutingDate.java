package ru.efive.dms.uifaces.beans.incoming;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import ru.efive.sql.dao.user.UserDAOHibernate;
import ru.efive.sql.entity.user.User;
import ru.efive.dms.dao.IncomingDocumentDAOImpl;
import ru.efive.dms.data.IncomingDocument;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.util.ApplicationHelper;
import ru.efive.uifaces.bean.AbstractDocumentListHolderBean;

@Named("in_documents_by_executing_date")
@SessionScoped
public class IncomingDocumentsByExecutingDate extends AbstractDocumentListHolderBean<IncomingDocument> {

    private DateFormatSymbols dateFormatSymbols;
    private TreeNode rootElement;

    @PostConstruct
    public void init() {
        dateFormatSymbols = new DateFormatSymbols(sessionManagement.getUserLocale());
    }


    @Override
    protected Pagination initPagination() {
        return new Pagination(0, getTotalCount(), 50);
        //Ранжирование по страницам по-умолчанию  (50 на страницу, начиная с нуля)
    }

    @Override
    protected Sorting initSorting() {
        return new Sorting("executionDate", false);
        //Сортировка по-умолчанию (дата регистрации документа)
    }

    @Override
    protected int getTotalCount() {
        return new Long(
                sessionManagement.getDAO(IncomingDocumentDAOImpl.class, ApplicationHelper.INCOMING_DOCUMENT_FORM_DAO)
                        .countControlledDocumentsByUser(filter, sessionManagement.getLoggedUser(), false)
        ).intValue();
    }

    @Override
    protected List<IncomingDocument> loadDocuments() {
        final List<IncomingDocument> docs = sessionManagement.getDAO(IncomingDocumentDAOImpl.class, ApplicationHelper.INCOMING_DOCUMENT_FORM_DAO)
                .findControlledDocumentsByUser(filter, sessionManagement.getLoggedUser(), false, getPagination().getOffset(), getPagination().getPageSize(), getSorting().getColumnId(), getSorting().isAsc());
        rootElement = new DefaultTreeNode("ROOT", null);
        Iterator<IncomingDocument> iterator = docs.iterator();
        while(iterator.hasNext()){
            IncomingDocument current = iterator.next();
            new DefaultTreeNode(current, rootElement);
        }
        return docs;

    }

   public TreeNode getRootElement(){

       return rootElement;
   }


    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    private String filter;

    @Inject
    @Named("sessionManagement")
    private transient SessionManagementBean sessionManagement;

    private static final long serialVersionUID = 8535420074467871583L;
}