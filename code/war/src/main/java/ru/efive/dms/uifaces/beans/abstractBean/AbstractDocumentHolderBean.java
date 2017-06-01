package ru.efive.dms.uifaces.beans.abstractBean;

import org.springframework.transaction.annotation.Transactional;
import ru.efive.dms.util.message.MessageHolder;
import ru.efive.dms.util.message.MessageKey;
import ru.efive.dms.util.message.MessageUtils;
import ru.entity.model.mapped.DeletableEntity;
import ru.hitsl.sql.dao.interfaces.mapped.criteria.Deletable;
import ru.hitsl.sql.dao.util.AuthorizationData;

import javax.annotation.PreDestroy;
import javax.faces.context.FacesContext;
import java.io.IOException;

import static ru.efive.dms.util.message.MessageHolder.MSG_ERROR_ON_SAVE;
import static ru.efive.dms.util.message.MessageHolder.MSG_ERROR_ON_SAVE_NEW;

/**
 * Абстарктный бин-обработчик страницы документа (Serializable)
 *
 * @param <I> тип документа (должен наследовать от DeletableEntity)
 * @param <D> Dao для работы с документом
 */

public abstract class AbstractDocumentHolderBean<I extends DeletableEntity, D extends Deletable<I>>
        extends AbstractStateableAccessableHolder
        implements OperationCallback<I> {
    /**
     * Значение GET-параметра REQUEST_PARAM_DOC_ACTION, означающее создание нового документа
     */
    public static final String REQUEST_PVALUE_DOC_ACTION_CREATE = "create";
    /**
     * Значение GET-параметра REQUEST_PARAM_DOC_ACTION, означающее редактирование документа
     */
    public static final String REQUEST_PVALUE_DOC_ACTION_EDIT = "edit";


    protected final D dao;

    /**
     * Поле, где будет храниться сам документ
     */
    private I document;

    public AbstractDocumentHolderBean(D dao, AuthorizationData authData) {
        super(authData);
        this.dao = dao;
    }


    protected Integer getDocumentId() {
        return getDocument() == null ? null : getDocument().getId();
    }


    protected abstract I newModel(AuthorizationData authData);

    @Transactional("ordTransactionManager")
    protected I readModel(Integer id, AuthorizationData authData) {
        log.info("Read Model[{}] by {}", id, authData.getLogString());
        return dao.get(id);
    }

    @Transactional("ordTransactionManager")
    protected boolean createModel(I document, final AuthorizationData authData) {
        log.info("Create Model[{}] by {}", document.getId(), authData.getLogString());
        try {
            setDocument(dao.save(document));
            return true;
        } catch (Exception e) {
            log.error("Error on create Model[{}] by {} ", document.getId(), authData.getLogString(), e);
            MessageUtils.addMessage(MSG_ERROR_ON_SAVE_NEW);
            return false;
        }
    }

    @Transactional("ordTransactionManager")
    protected boolean updateModel(I document, final AuthorizationData authData) {
        log.info("Update Model[{}] by {}", document.getId(), authData.getLogString());
        try {
            setDocument(dao.update(document));
            return true;
        } catch (Exception e) {
            log.error("Error on update Model[{}] by {} ", document.getId(), authData.getLogString(), e);
            MessageUtils.addMessage(MSG_ERROR_ON_SAVE);
            return false;
        }
    }

    /**
     * Удаление документа (из БД или только флаг решается в реализации)
     *
     * @return упсешность удаления
     */
    @Transactional("ordTransactionManager")
    protected boolean deleteModel(I document, final AuthorizationData authData) {
        log.info("Delete Model[{}] by {}", document.getId(), authData.getLogString());
        try {
            return dao.delete(document);
        } catch (Exception e) {
            log.error("Error on update Model[{}] by {} ", document.getId(), authData.getLogString(), e);
            MessageUtils.addMessage(MSG_ERROR_ON_SAVE);
            return false;
        }
    }


    // ----------------------------------------------------------------------------------------------------------------
    @Transactional("ordTransactionManager")
    public boolean delete() {
        // should add error messages to context themself. (For example, concurrent modify).
        // If chained method fail -> break operation execution
        // [check user privilege for operation] -> [before operation callback] -> [operation] -> [after operation callback]
        return !isErrorState() && isCanDelete(authData) && beforeDelete(document, authData) && deleteModel(document, authData) & afterDelete(document, authData);
    }

    /**
     * Меняет значение флажка Deleted и сохраняет это в БД
     *
     * @return успешность сохранения изменений в БД
     */
    @Transactional("ordTransactionManager")
    public boolean changeDeleted() {
        if (!isErrorState() && isCanDelete(authData)) {
            document.setDeleted(!document.isDeleted());
            return updateModel(document, authData);
        } else {
            return false;
        }
    }

    public void edit() {
        if (!isErrorState() && isCanUpdate(authData) && beforeUpdate(document, authData)) {
            setState(State.EDIT);
        }
    }

    @Transactional("ordTransactionManager")
    public void save() {
        if (!isErrorState()) {
            boolean success;
            // If chained method fail -> break operation execution
            // [check user privilege for operation] -> [before operation callback] -> [operation] -> [after operation callback]
            if (isCreateState()) {
                success = isCanCreate(authData) && beforeCreate(document, authData) && createModel(document, authData) & afterCreate(document, authData);
            } else {
                success = isCanUpdate(authData) && beforeUpdate(document, authData) && updateModel(document, authData) && afterUpdate(document, authData);
            }
            if (success) {
                setState(State.VIEW);
            }
        }
    }


    @Transactional("ordTransactionManager")
    public void view() {
        if (!isErrorState() && isCanRead(authData) && beforeRead(authData)) {
            document = readModel(getDocumentId(), authData);
            setState(State.VIEW);
        }
    }

    @PreDestroy
    public void destroy() {
        log.info("Bean[{}] destroyed", getBeanName());
    }

    @Transactional("ordTransactionManager")
    public void init() {
        Integer id = null;
        try {
            id = Integer.valueOf(getRequestParamByName("docId"));
        } catch (NumberFormatException e) {

        }
        init(getRequestParamByName("docAction"), id);
    }


    @Transactional("ordTransactionManager")
    public void init(final String action, final Integer id) {
        log.debug("init: [@{}] as '{}' with[action={}, id={}]", Integer.toHexString(hashCode()), getBeanName(), action, id);
        //CREATE
        if (REQUEST_PVALUE_DOC_ACTION_CREATE.equals(action)) {
            if (isCanCreate(authData)) {
                document = newModel(authData);
                setState(State.CREATE);
                return;
            } else {
                setState(State.ERROR);
                return;
            }
        }
        //EDIT OR VIEW EXISTING DOCUMENT
        if (id != null && beforeRead(authData)) {
            setDocument(readModel(id, authData));
        }
        if (document == null) {
            setState(State.ERROR);
            MessageUtils.addMessage(MessageKey.ERROR, MessageHolder.MSG_DOCUMENT_NOT_FOUND);
            return;
        } else if (document.isDeleted()) {
            setState(State.ERROR);
            MessageUtils.addMessage(MessageKey.ERROR, MessageHolder.MSG_DOCUMENT_IS_DELETED);
            return;
        }
        if (!afterRead(document, authData)) {
            setState(State.ERROR);
        } else if (REQUEST_PVALUE_DOC_ACTION_EDIT.equals(action)) {
            setState(isCanUpdate(authData) ? State.EDIT : State.ERROR);
        } else {
            setState(isCanRead(authData) ? State.VIEW : State.ERROR);
        }
    }

    // ----------------------------------------------------------------------------------------------------------------


    public I getDocument() {
        return document;
    }

    private void setDocument(I document) {
        this.document = document;
    }

    /**
     * returns GET param value from URL by his name
     *
     * @param paramName name of the GET parameter
     * @return String value of named get parameter, if not set returns null
     */
    public String getRequestParamByName(final String paramName) {
        return FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get(paramName);
    }

    @Override
    public boolean afterDelete(I document, AuthorizationData authData) {
        try {
            FacesContext.getCurrentInstance().getExternalContext().redirect("/component/delete_document.xhtml");
            //Удалить ViewScope бин
            //@see http://stackoverflow.com/questions/9458911/reset-jsf-backing-beanview-or-session-scope
            FacesContext.getCurrentInstance().getViewRoot().getViewMap().remove(getBeanName());
            return true;
        } catch (IOException e) {
            log.error("Error on redirect", e);
            return false;
        }
    }
}
