package ru.efive.dms.uifaces.beans.abstractBean;

import ru.entity.model.mapped.IdentifiedEntity;
import ru.hitsl.sql.dao.util.AuthorizationData;

/**
 * Created by EUpatov on 05.05.2017.
 */
public interface BeforeOperationCallback<I extends IdentifiedEntity> {

    default boolean beforeCreate(I document, AuthorizationData authData) {
        return true;
    }

    default boolean beforeRead(AuthorizationData authData) {
        return true;
    }

    default boolean beforeUpdate(I document, AuthorizationData authData) {
        return true;
    }

    default boolean beforeDelete(I document, AuthorizationData authData) {
        return true;
    }
}
