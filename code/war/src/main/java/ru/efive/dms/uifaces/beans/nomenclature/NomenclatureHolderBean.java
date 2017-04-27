package ru.efive.dms.uifaces.beans.nomenclature;

import com.github.javaplugs.jsf.SpringScopeView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentHolderBean;
import ru.efive.dms.uifaces.beans.annotations.ViewScopedController;
import ru.entity.model.referenceBook.Nomenclature;

import org.springframework.stereotype.Controller;

/**
 * Author: Upatov Egor <br>
 * Date: 16.02.2015, 13:29 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
@ViewScopedController("nomenclature")
public class NomenclatureHolderBean extends AbstractDocumentHolderBean<Nomenclature> {

    private static final Logger LOGGER = LoggerFactory.getLogger("RB_NOMENCLATURE");


    @Override
    protected boolean deleteDocument() {
        return false;
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
//        if(documentId != null) {
//            setDocument(sessionManagementBean.getDAO(NomenclatureDAOImpl.class, RB_NOMENCLATURE_DAO).get(documentId));
//        }
    }

    @Override
    public boolean saveNewDocument() {
//        try {
//            setDocument(sessionManagementBean.getDAO(NomenclatureDAOImpl.class, RB_NOMENCLATURE_DAO).save(getDocument()));
//            return true;
//        } catch (Exception e) {
//            LOGGER.error("CANT SAVE NEW:", e);
//            return false;
//        }
        return false;
    }

    @Override
    protected boolean saveDocument() {
//
        return false;
    }


}
