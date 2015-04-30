package ru.efive.dms.uifaces.beans.officekeeping;

import ru.efive.dms.dao.OfficeKeepingFileDAOImpl;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentLazyDataModelBean;
import ru.efive.dms.uifaces.lazyDataModel.LazyDataModelForOfficeKeepingFile;
import ru.entity.model.document.OfficeKeepingFile;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import static ru.efive.dms.util.ApplicationDAONames.OFFICE_KEEPING_FILE_DAO;

@ManagedBean(name="officeKeepingFiles")
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