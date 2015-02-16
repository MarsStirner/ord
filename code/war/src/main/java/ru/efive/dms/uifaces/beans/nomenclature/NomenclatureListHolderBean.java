package ru.efive.dms.uifaces.beans.nomenclature;

/**
 * Author: Upatov Egor <br>
 * Date: 16.02.2015, 13:05 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */

import ru.efive.dms.dao.NomenclatureDAOImpl;
import ru.efive.dms.uifaces.beans.IndexManagementBean;
import ru.efive.uifaces.bean.AbstractDocumentListHolderBean;
import ru.entity.model.document.Nomenclature;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

import static ru.efive.dms.util.ApplicationDAONames.RB_NOMENCLATURE_DAO;

@ManagedBean(name="nomenclatureList")
@ViewScoped
public class NomenclatureListHolderBean extends AbstractDocumentListHolderBean<Nomenclature> {
    private String filter;
    private static NomenclatureDAOImpl dao;
    @Inject
    @Named("indexManagement")
    private IndexManagementBean indexManagement;

    @PostConstruct
    public void init() {
        dao = indexManagement.getContext().getBean(RB_NOMENCLATURE_DAO, NomenclatureDAOImpl.class);
    }


    @Override
    protected int getTotalCount() {
        return (int)dao.countDocument(false);
    }

    @Override
    protected List<Nomenclature> loadDocuments() {
        return dao.findDocuments(filter, false, -1, -1, getSorting().getColumnId(), getSorting().isAsc());
    }

    @Override
    public Sorting initSorting() {
        return new Sorting("code", true);
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }
}
