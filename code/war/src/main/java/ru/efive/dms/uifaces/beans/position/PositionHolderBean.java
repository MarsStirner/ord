package ru.efive.dms.uifaces.beans.position;

import com.github.javaplugs.jsf.SpringScopeView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentHolderBean;
import ru.entity.model.referenceBook.Position;
import ru.hitsl.sql.dao.interfaces.referencebook.PositionDao;
import ru.hitsl.sql.dao.util.AuthorizationData;

import org.springframework.stereotype.Controller;
import java.io.Serializable;

/**
 * Author: Upatov Egor <br>
 * Date: 18.09.2014, 12:13 <br>
 * Company: Korus Consulting IT <br>
 * Description: Бин для работы с должностью<br>
 */

@Controller("position")
@SpringScopeView
public class PositionHolderBean extends AbstractDocumentHolderBean<Position> implements Serializable {
    private static final Logger LOGGER = LoggerFactory.getLogger("POSITION");

    @Autowired
    @Qualifier("authData")
    private AuthorizationData authData;

    @Autowired
    @Qualifier("positionDao")
    private PositionDao positionDao;

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
        return authData.isAdministrator();
    }

    public boolean isCanDelete() {
        return authData.isAdministrator();
    }

    public boolean isCanEdit() {
        return authData.isAdministrator();
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
        setDocument(positionDao.get(documentId));
    }

    @Override
    protected boolean saveNewDocument() {
        try {
            setDocument(positionDao.save(getDocument()));
            return true;
        } catch (Exception e) {
            LOGGER.error("CANT SAVE NEW:", e);
            return false;
        }
    }

    @Override
    protected boolean saveDocument() {
        try {
            setDocument(positionDao.update(getDocument()));
            return true;
        } catch (Exception e) {
            LOGGER.error("CANT SAVE:", e);
            return false;
        }
    }


}
