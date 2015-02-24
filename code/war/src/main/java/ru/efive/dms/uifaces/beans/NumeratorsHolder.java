package ru.efive.dms.uifaces.beans;

import ru.efive.dms.dao.NumeratorDAOImpl;
import ru.efive.uifaces.bean.AbstractDocumentListHolderBean;
import ru.efive.uifaces.bean.Pagination;
import ru.entity.model.document.Numerator;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

import static ru.efive.dms.util.ApplicationDAONames.NUMERATOR_DAO;

@ManagedBean(name="numerators")
@ViewScoped
public class NumeratorsHolder extends AbstractDocumentListHolderBean<Numerator> {

    @Inject
    @Named("sessionManagement")
    private transient SessionManagementBean sessionManagement;

    @Override
    protected Pagination initPagination() {
        return new Pagination(0, getTotalCount(), 50);
        //Ранжирование по страницам по-умолчанию  (50 на страницу, начиная с нуля)
    }

    @Override
    protected Sorting initSorting() {
        return new Sorting("creationDate", true);
        //Сортировка по-умолчанию (дата регистрации документа)
    }


    @Override
    protected int getTotalCount() {
        return (int) sessionManagement.getDAO(NumeratorDAOImpl.class, NUMERATOR_DAO).countDocument(false);
    }

    @Override
    protected List<Numerator> loadDocuments() {
        return sessionManagement.getDAO(NumeratorDAOImpl.class, NUMERATOR_DAO).findDocuments(false, getPagination().getOffset(), getPagination().getPageSize(), getSorting().getColumnId(), getSorting().isAsc());
    }
}