package ru.efive.dms.uifaces.beans;

import ru.efive.dms.dao.NumeratorDAOImpl;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentLazyDataModelBean;
import ru.efive.dms.uifaces.lazyDataModel.LazyDataModelForNumerator;
import ru.entity.model.document.Numerator;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import static ru.efive.dms.util.ApplicationDAONames.NUMERATOR_DAO;

@ManagedBean(name="numerators")
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