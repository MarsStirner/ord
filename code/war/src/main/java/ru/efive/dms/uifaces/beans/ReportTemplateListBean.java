package ru.efive.dms.uifaces.beans;

import com.github.javaplugs.jsf.SpringScopeView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentLazyDataModelBean;
import ru.efive.dms.uifaces.lazyDataModel.LazyDataModelForReportTemplate;
import ru.entity.model.document.ReportTemplate;

import org.springframework.stereotype.Controller;

@Controller("reportTemplateList")
@SpringScopeView
public class ReportTemplateListBean extends AbstractDocumentLazyDataModelBean<ReportTemplate> {
    @Autowired
    @Qualifier("reportTemplateLDM")
    LazyDataModelForReportTemplate ldm;

}