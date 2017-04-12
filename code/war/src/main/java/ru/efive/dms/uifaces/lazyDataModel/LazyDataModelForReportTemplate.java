package ru.efive.dms.uifaces.lazyDataModel;

import com.github.javaplugs.jsf.SpringScopeView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.efive.dms.uifaces.beans.annotations.ViewScopedLazyDataModel;
import ru.entity.model.document.ReportTemplate;
import ru.hitsl.sql.dao.interfaces.ReportDao;

/**
 * Author: Upatov Egor <br>
 * Date: 20.04.2015, 18:57 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
@ViewScopedLazyDataModel("reportTemplateLDM")
public class LazyDataModelForReportTemplate extends AbstractFilterableLazyDataModel<ReportTemplate> {

    @Autowired
    public LazyDataModelForReportTemplate(
            @Qualifier("reportDao") final ReportDao reportDao) {
        super(reportDao);
    }
}
