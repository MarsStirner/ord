package ru.efive.dms.uifaces.beans;

import ru.efive.dms.dao.ReportDAOImpl;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentLazyDataModelBean;
import ru.efive.dms.uifaces.lazyDataModel.LazyDataModelForReportTemplate;
import ru.entity.model.document.ReportTemplate;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import static ru.efive.dms.util.ApplicationDAONames.REPORT_DAO;

@ManagedBean(name="reportTemplateList")
@ViewScoped
public class ReportTemplateListBean extends AbstractDocumentLazyDataModelBean<ReportTemplate>{
    @Inject
    @Named("sessionManagement")
    private transient SessionManagementBean sessionManagement;

    @PostConstruct
    public void init() {
        final ReportDAOImpl dao = sessionManagement.getDAO(ReportDAOImpl.class, REPORT_DAO);
        setLazyModel(new LazyDataModelForReportTemplate(dao));
    }

}