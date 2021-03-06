package ru.efive.dms.uifaces.beans.position;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentHolderBean;
import ru.entity.model.referenceBook.Position;
import ru.hitsl.sql.dao.referenceBook.PositionDAOImpl;

import javax.enterprise.context.ConversationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;

import static ru.hitsl.sql.dao.util.ApplicationDAONames.POSITION_DAO;

/**
 * Author: Upatov Egor <br>
 * Date: 18.09.2014, 12:13 <br>
 * Company: Korus Consulting IT <br>
 * Description: Бин для работы с должностью<br>
 */

@Named("position")
@ConversationScoped
public class PositionHolderBean extends AbstractDocumentHolderBean<Position> implements Serializable {
    private static final Logger LOGGER = LoggerFactory.getLogger("POSITION");

    /**
     * Меняет значение флажка Deleted и сохраняет это в БД
     *
     * @return успешность сохранения изменений в БД
     */
    public boolean changeDeleted() {
        Position item = getDocument();
        item.setDeleted(!item.isDeleted());
        return saveDocument();
    }

    public boolean isCanCreate() {
        return sessionManagementBean.isAdministrator();
    }

    public boolean isCanDelete() {
        return sessionManagementBean.isAdministrator();
    }

    public boolean isCanEdit() {
        return sessionManagementBean.isAdministrator();
    }

    @Override
    protected boolean deleteDocument() {
        getDocument().setDeleted(true);
        return getDocument().isDeleted();
    }


    @Override
    protected void initNewDocument() {
        final Position newItem = new Position();
        newItem.setDeleted(false);
        newItem.setValue("");
        setDocument(newItem);
    }

    @Override
    protected void initDocument(Integer documentId) {
        setDocument(sessionManagementBean.getDAO(PositionDAOImpl.class, POSITION_DAO).get(documentId));
    }

    @Override
    protected boolean saveNewDocument() {
        try {
            setDocument(sessionManagementBean.getDAO(PositionDAOImpl.class, POSITION_DAO).save(getDocument()));
            return true;
        } catch (Exception e) {
            LOGGER.error("CANT SAVE NEW:", e);
            return false;
        }
    }

    @Override
    protected boolean saveDocument() {
        try {
            setDocument(sessionManagementBean.getDAO(PositionDAOImpl.class, POSITION_DAO).update(getDocument()));
            return true;
        } catch (Exception e) {
            LOGGER.error("CANT SAVE:", e);
            return false;
        }
    }


    @Inject
    @Named("sessionManagement")
    private transient SessionManagementBean sessionManagementBean;
}
