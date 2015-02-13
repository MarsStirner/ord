package ru.efive.dms.uifaces.beans.contragent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.util.ApplicationDAONames;
import ru.efive.sql.dao.RbContragentTypeDAOImpl;
import ru.efive.uifaces.bean.AbstractDocumentListHolderBean;
import ru.entity.model.crm.ContragentType;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.List;

/**
 * Author: Upatov Egor <br>
 * Date: 12.02.2015, 15:25 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
@ManagedBean(name = "contragentTypeList")
@ViewScoped
public class ContragentTypeListHolderBean extends AbstractDocumentListHolderBean<ContragentType> {


    private static final Logger logger = LoggerFactory.getLogger("RB_CONTRAGENT_TYPE");

    private String filter;

    @Inject
    @Named("sessionManagement")
    private SessionManagementBean sessionManagement = new SessionManagementBean();

    @Override
    public Sorting initSorting() {
        return new Sorting("code", true);
    }

    @Override
    protected int getTotalCount() {
        try {
            return new Long(sessionManagement.getDAO(RbContragentTypeDAOImpl.class, ApplicationDAONames
                    .RB_CONTRAGENT_TYPE_DAO).countDocument(filter, false)).intValue();
        } catch (Exception e) {
            logger.error("Exception on count documents", e);
            return 0;
        }
    }

    @Override
    protected List<ContragentType> loadDocuments() {
        try {
            return sessionManagement.getDAO(RbContragentTypeDAOImpl.class, ApplicationDAONames.RB_CONTRAGENT_TYPE_DAO).findDocuments(filter, false, -1, -1, getSorting().getColumnId(), getSorting().isAsc());

        } catch (Exception e){
            logger.error("Exception on get documents", e);
            return Collections.emptyList();
        }
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }


}

