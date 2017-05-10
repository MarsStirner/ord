package ru.efive.dms.uifaces.beans.abstractBean;

import ru.entity.model.mapped.IdentifiedEntity;
import ru.hitsl.sql.dao.util.AuthorizationData;

/**
 * Created by EUpatov on 05.05.2017.
 */
public interface AfterOperationCallback<I extends IdentifiedEntity> {
    default boolean afterCreate(I document, AuthorizationData authData) {
        return true;
    }

    default boolean afterRead(I document, AuthorizationData authData) {
        return true;
    }


    default boolean afterUpdate(I document, AuthorizationData authData) {
        return true;
    }

    default boolean afterDelete(I document, AuthorizationData authData) {
        return true;
    }

}
