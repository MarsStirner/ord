package ru.efive.dms.uifaces.beans.nomenclature;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.uifaces.bean.AbstractDocumentHolderBean;
import ru.efive.uifaces.bean.FromStringConverter;
import ru.entity.model.referenceBook.Nomenclature;
import ru.hitsl.sql.dao.referenceBook.NomenclatureDAOImpl;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import static ru.hitsl.sql.dao.util.ApplicationDAONames.RB_NOMENCLATURE_DAO;

/**
 * Author: Upatov Egor <br>
 * Date: 16.02.2015, 13:29 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
@Named("nomenclature")
@ViewScoped
public class NomenclatureHolderBean extends AbstractDocumentHolderBean<Nomenclature, Integer> {

    private static final Logger LOGGER = LoggerFactory.getLogger("RB_NOMENCLATURE");

    @Override
    public boolean isCanCreate() {
        return sessionManagementBean.isAdministrator();
    }

    @Override
    public boolean isCanDelete() {
        return sessionManagementBean.isAdministrator();
    }

    @Override
    public boolean isCanEdit() {
        return sessionManagementBean.isAdministrator();
    }

    @Override
    protected boolean deleteDocument() {
        return false;
    }

    @Override
    protected Integer getDocumentId() {
        return getDocument().getId();
    }

    @Override
    protected void initNewDocument() {
        final Nomenclature newItem = new Nomenclature();
        newItem.setDeleted(false);
        newItem.setValue("");
        setDocument(newItem);
    }

    @Override
    protected void initDocument(Integer documentId) {
        if(documentId != null) {
            setDocument(sessionManagementBean.getDAO(NomenclatureDAOImpl.class, RB_NOMENCLATURE_DAO).get(documentId));
        }
    }

    @Override
    protected boolean saveNewDocument() {
        try {
            setDocument(sessionManagementBean.getDAO(NomenclatureDAOImpl.class, RB_NOMENCLATURE_DAO).save(getDocument()));
            return true;
        } catch (Exception e) {
            LOGGER.error("CANT SAVE NEW:", e);
            return false;
        }
    }

    @Override
    protected boolean saveDocument() {
        try {
            setDocument(sessionManagementBean.getDAO(NomenclatureDAOImpl.class, RB_NOMENCLATURE_DAO).update(getDocument()));
            return true;
        } catch (Exception e) {
            LOGGER.error("CANT SAVE:", e);
            return false;
        }
    }

    @Override
    protected FromStringConverter<Integer> getIdConverter() {
        return FromStringConverter.INTEGER_CONVERTER;
    }

    @Inject
    @Named("sessionManagement")
    private SessionManagementBean sessionManagementBean;
}
