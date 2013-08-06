package ru.efive.dms.uifaces.beans.user;

import ru.efive.dms.dao.RequestDocumentDAOImpl;
import ru.efive.dms.data.RequestDocument;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.util.ApplicationHelper;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.List;


@Named("userInfo")
@SessionScoped
public class UserInfoBean implements Serializable {

    public boolean isCanViewRequestDocuments() {
        RequestDocumentDAOImpl docDao = sessionManagement.getDAO(RequestDocumentDAOImpl.class, ApplicationHelper.REQUEST_DOCUMENT_FORM_DAO);
        List<RequestDocument> docList = docDao.findAllDocumentsByUser(null, sessionManagement.getLoggedUser(false), false, true);
        return (docList.size() > 0);
    }

    @Inject
    @Named("sessionManagement")
    SessionManagementBean sessionManagement = new SessionManagementBean();

}