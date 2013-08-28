package ru.efive.dms.dao;


import ru.efive.dms.data.Version;
import ru.efive.sql.dao.GenericDAOHibernate;

public class VersionDAOImpl extends GenericDAOHibernate<Version> {

    @Override
    protected Class<Version> getPersistentClass() {
        return Version.class;
    }

}
