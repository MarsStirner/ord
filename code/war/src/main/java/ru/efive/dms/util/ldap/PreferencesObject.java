package ru.efive.dms.util.ldap;

import javax.naming.directory.SearchControls;
import javax.naming.ldap.LdapContext;

/**
 * Author: Upatov Egor <br>
 * Date: 01.04.2016, 18:26 <br>
 * Company: hitsl (Hi-Tech Solutions) <br>
 * Description: <br>
 */
public class PreferencesObject {
    private int pageSize;
    private SearchControls searchControls;
    private LdapContext ldapContext;

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(final int pageSize) {
        this.pageSize = pageSize;
    }

    public SearchControls getSearchControls() {
        return searchControls;
    }

    public void setSearchControls(final SearchControls searchControls) {
        this.searchControls = searchControls;
    }

    public LdapContext getLdapContext() {
        return ldapContext;
    }

    public void setLdapContext(final LdapContext ldapContext) {
        this.ldapContext = ldapContext;
    }
}
