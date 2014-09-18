package ru.efive.dms.uifaces.beans.position;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.util.ApplicationHelper;
import ru.efive.sql.dao.user.DepartmentDAO;
import ru.efive.sql.dao.user.PositionDAO;
import ru.efive.sql.entity.user.Department;
import ru.efive.sql.entity.user.Position;
import ru.efive.uifaces.bean.AbstractDocumentHolderBean;
import ru.efive.uifaces.bean.FromStringConverter;

import javax.enterprise.context.ConversationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;

/**
 * Author: Upatov Egor <br>
 * Date: 18.09.2014, 12:13 <br>
 * Company: Korus Consulting IT <br>
 * Description: Бин для работы с должностью<br>
 */

@Named("position")
@ConversationScoped
public class PositionHolderBean  extends AbstractDocumentHolderBean<Position, Integer> implements Serializable {
    private static final Logger LOGGER = LoggerFactory.getLogger("POSITION");

    /**
     * Меняет значение флажка Deleted и сохраняет это в БД
     * @return  успешность сохранения изменений в БД
     */
    public boolean changeDeleted(){
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
    protected Integer getDocumentId() {
        return getDocument().getId();
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
        setDocument(sessionManagementBean.getDAO(PositionDAO.class, ApplicationHelper.POSITION_DAO).get(documentId));
    }

    @Override
    protected boolean saveNewDocument() {
        try {
            setDocument(sessionManagementBean.getDAO(PositionDAO.class, ApplicationHelper.POSITION_DAO).save(getDocument()));
            return true;
        }catch (Exception e){
            LOGGER.error("CANT SAVE NEW:", e);
            return false;
        }
    }

    @Override
    protected boolean saveDocument() {
        try {
            setDocument(sessionManagementBean.getDAO(PositionDAO.class, ApplicationHelper.POSITION_DAO).update(getDocument()));
            return true;
        }catch (Exception e){
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
    private transient SessionManagementBean sessionManagementBean;
}
