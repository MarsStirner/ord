package ru.efive.dms.uifaces.beans.user;

import org.primefaces.model.LazyDataModel;
import ru.efive.dms.dao.ejb.SubstitutionDaoImpl;
import ru.efive.dms.uifaces.beans.IndexManagementBean;
import ru.efive.dms.uifaces.lazyDataModel.LazyDataModelForSubstitution;
import ru.efive.dms.util.ApplicationDAONames;
import ru.entity.model.user.Substitution;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.Serializable;

/**
 * Author: Upatov Egor <br>
 * Date: 17.11.2014, 18:26 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
@ManagedBean(name="substitutions")
@ViewScoped
public class SubstitutionListHolderBean implements Serializable{

    @EJB(name = "indexManagement")
    private IndexManagementBean indexManagementBean;


    @PostConstruct
    public void init(){
       lazyModel = new LazyDataModelForSubstitution((SubstitutionDaoImpl) indexManagementBean.getContext().getBean(ApplicationDAONames.SUBSTITUTION_DAO));
    }

    private LazyDataModel<Substitution> lazyModel;

    public LazyDataModel<Substitution> getLazyModel() {
        return lazyModel;
    }
    private String filter;

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public void refresh(){
        lazyModel = new LazyDataModelForSubstitution((SubstitutionDaoImpl) indexManagementBean.getContext().getBean(ApplicationDAONames.SUBSTITUTION_DAO));
    }
}
