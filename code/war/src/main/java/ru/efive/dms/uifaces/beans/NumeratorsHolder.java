package ru.efive.dms.uifaces.beans;

import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentLazyDataModelBean;
import ru.efive.dms.uifaces.lazyDataModel.LazyDataModelForNumerator;
import ru.entity.model.document.Numerator;
import ru.hitsl.sql.dao.NumeratorDAOImpl;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import static ru.hitsl.sql.dao.util.ApplicationDAONames.NUMERATOR_DAO;

@Named("numerators")
@ViewScoped
public class NumeratorsHolder extends AbstractDocumentLazyDataModelBean<Numerator>{
    @Inject
    @Named("sessionManagement")
    private transient SessionManagementBean sessionManagement;

    @PostConstruct
    public void init() {
        final NumeratorDAOImpl dao = sessionManagement.getDAO(NumeratorDAOImpl.class, NUMERATOR_DAO);
        setLazyModel(new LazyDataModelForNumerator(dao));
    }
}