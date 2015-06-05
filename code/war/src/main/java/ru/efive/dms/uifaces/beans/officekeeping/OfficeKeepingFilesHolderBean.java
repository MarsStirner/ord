package ru.efive.dms.uifaces.beans.officekeeping;

import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentLazyDataModelBean;
import ru.efive.dms.uifaces.lazyDataModel.LazyDataModelForOfficeKeepingFile;
import ru.entity.model.document.OfficeKeepingFile;
import ru.hitsl.sql.dao.OfficeKeepingFileDAOImpl;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import static ru.hitsl.sql.dao.util.ApplicationDAONames.OFFICE_KEEPING_FILE_DAO;

@Named("officeKeepingFiles")
@ViewScoped
public class OfficeKeepingFilesHolderBean extends AbstractDocumentLazyDataModelBean<OfficeKeepingFile>{

    @Inject
    @Named("sessionManagement")
    private transient SessionManagementBean sessionManagement;

    @PostConstruct
    public void init() {
        final OfficeKeepingFileDAOImpl dao = sessionManagement.getDAO(OfficeKeepingFileDAOImpl.class, OFFICE_KEEPING_FILE_DAO);
        setLazyModel(new LazyDataModelForOfficeKeepingFile(dao, sessionManagement.getAuthData()));
    }
}