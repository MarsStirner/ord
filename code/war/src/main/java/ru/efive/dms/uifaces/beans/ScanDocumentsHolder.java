package ru.efive.dms.uifaces.beans;

import ru.efive.dms.dao.ScanCopyDocumentDAOImpl;
import ru.efive.dms.data.ScanCopyDocument;
import ru.efive.dms.util.ApplicationHelper;
import ru.efive.dms.util.ReadInboxService;
import ru.efive.sql.dao.user.UserDAOHibernate;
import ru.efive.sql.entity.user.User;
import ru.efive.uifaces.bean.AbstractDocumentListHolderBean;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.mail.MessagingException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * @author Nastya Peshekhonova
 */
@Named("scan_documents")
@SessionScoped
public class ScanDocumentsHolder extends AbstractDocumentListHolderBean<ScanCopyDocument> {
    private static final String beanName = "scan_documents";

    private String filter;
    private List<ScanCopyDocument> hashDocuments;
    private boolean needRefresh = true;

    @Inject
    @Named("sessionManagement")
    private transient SessionManagementBean sessionManagement;

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.needRefresh = true;
        this.filter = filter;
    }

    public void refreshDocumentsList() {
        this.needRefresh = true;
        ReadInboxService service = new ReadInboxService();
        try {
            service.run();
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.refresh();
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
    protected List<ScanCopyDocument> loadDocuments() {
        List<ScanCopyDocument> result = new ArrayList<ScanCopyDocument>();
        try {
            this.needRefresh = true;
            result = this.getHashDocuments();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    protected List<ScanCopyDocument> getHashDocuments() {
        List<ScanCopyDocument> result = new ArrayList<ScanCopyDocument>();
        if (needRefresh) {
            sessionManagement.registrateBeanName(beanName);
            try {
                User user = sessionManagement.getLoggedUser();
                //user = sessionManagement.getDAO(UserDAOHibernate.class, ApplicationHelper.USER_DAO).findByLoginAndPassword(user.getLogin(), user.getPassword());
                result = new ArrayList<ScanCopyDocument>(new HashSet<ScanCopyDocument>(sessionManagement.getDAO(ScanCopyDocumentDAOImpl.class, ApplicationHelper.SCAN_DAO).
                        findDocumentsByAuthor(filter, user.getId(), false, getPagination().getOffset(), getPagination().getPageSize(), getSorting().getColumnId(),
                                getSorting().isAsc())));
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
}
