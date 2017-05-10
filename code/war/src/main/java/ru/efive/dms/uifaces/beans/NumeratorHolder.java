package ru.efive.dms.uifaces.beans;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentHolderBean;
import ru.efive.dms.uifaces.beans.annotations.ViewScopedController;
import ru.entity.model.document.Numerator;
import ru.hitsl.sql.dao.interfaces.NumeratorDao;
import ru.hitsl.sql.dao.util.AuthorizationData;


@ViewScopedController("numerator")
public class NumeratorHolder extends AbstractDocumentHolderBean<Numerator, NumeratorDao> {

    @Autowired
    public NumeratorHolder(@Qualifier("numeratorDao") NumeratorDao dao, @Qualifier("authData") AuthorizationData authData) {
        super(dao, authData);
    }

    @Override
    protected Numerator newModel(AuthorizationData authData) {
        return new Numerator();
    }


}