package ru.efive.dms.uifaces.beans.nomenclature;

/**
 * Author: Upatov Egor <br>
 * Date: 16.02.2015, 13:05 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */

import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentLazyDataModelBean;
import ru.efive.dms.uifaces.lazyDataModel.LazyDataModelForNomenclature;
import ru.entity.model.referenceBook.Nomenclature;
import ru.hitsl.sql.dao.referenceBook.NomenclatureDAOImpl;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import static ru.hitsl.sql.dao.util.ApplicationDAONames.RB_NOMENCLATURE_DAO;

@Named("nomenclatureList")
@ViewScoped
public class NomenclatureListHolderBean extends AbstractDocumentLazyDataModelBean<Nomenclature>{
    private NomenclatureDAOImpl dao;
    @Inject
    @Named("sessionManagement")
    private transient SessionManagementBean sessionManagement;

    @PostConstruct
    public void init() {
        dao = sessionManagement.getDAO(NomenclatureDAOImpl.class, RB_NOMENCLATURE_DAO);
        setLazyModel(new LazyDataModelForNomenclature(dao));
    }
}
