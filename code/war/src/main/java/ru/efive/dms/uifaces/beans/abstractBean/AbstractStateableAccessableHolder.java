package ru.efive.dms.uifaces.beans.abstractBean;


import ru.hitsl.sql.dao.util.AuthorizationData;

public abstract class AbstractStateableAccessableHolder extends AbstractStateableHolder {

    final AuthorizationData authData;

    public AbstractStateableAccessableHolder(AuthorizationData authData) {
        this.authData = authData;
    }

    // is can CRUD


    public boolean isCanCreate(final AuthorizationData authData) {
        return true;
    }

    public boolean isCanRead(final AuthorizationData authData) {
        return !isErrorState();
    }

    public boolean isCanUpdate(final AuthorizationData authData) {
        return !isErrorState();
    }

    public boolean isCanDelete(final AuthorizationData authData) {
        return !isErrorState();
    }


}
