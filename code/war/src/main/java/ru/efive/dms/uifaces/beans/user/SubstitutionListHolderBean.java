package ru.efive.dms.uifaces.beans.user;

import ru.efive.dms.dao.ejb.SubstitutionDaoImpl;
import ru.efive.dms.uifaces.beans.IndexManagementBean;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentLazyDataModelBean;
import ru.efive.dms.uifaces.lazyDataModel.LazyDataModelForSubstitution;
import ru.efive.dms.util.ApplicationDAONames;
import ru.entity.model.user.Substitution;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.view.ViewScoped;

/**
 * Author: Upatov Egor <br>
 * Date: 17.11.2014, 18:26 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
@ManagedBean(name="substitutions")
@ViewScoped
public class SubstitutionListHolderBean extends AbstractDocumentLazyDataModelBean<Substitution>{

    @EJB(name = "indexManagement")
    private IndexManagementBean indexManagementBean;

    @PostConstruct
    public void init(){
       setLazyModel(new LazyDataModelForSubstitution((SubstitutionDaoImpl) indexManagementBean.getContext().getBean(ApplicationDAONames.SUBSTITUTION_DAO)));
    }

}
