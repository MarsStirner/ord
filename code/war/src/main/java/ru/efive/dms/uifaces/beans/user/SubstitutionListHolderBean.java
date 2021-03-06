package ru.efive.dms.uifaces.beans.user;

import ru.efive.dms.uifaces.beans.IndexManagementBean;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentLazyDataModelBean;
import ru.efive.dms.uifaces.lazyDataModel.LazyDataModelForSubstitution;
import ru.entity.model.user.Substitution;
import ru.hitsl.sql.dao.SubstitutionDaoImpl;
import ru.hitsl.sql.dao.util.ApplicationDAONames;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

/**
 * Author: Upatov Egor <br>
 * Date: 17.11.2014, 18:26 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
@Named("substitutions")
@ViewScoped
public class SubstitutionListHolderBean extends AbstractDocumentLazyDataModelBean<Substitution>{

    @EJB(name = "indexManagement")
    private IndexManagementBean indexManagementBean;

    @PostConstruct
    public void init(){
       setLazyModel(new LazyDataModelForSubstitution((SubstitutionDaoImpl) indexManagementBean.getContext().getBean(ApplicationDAONames.SUBSTITUTION_DAO)));
    }

}
