package ru.efive.dms.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

public class RetrieveUserAttributes {

    public static void main(String[] args) {
        //String userID="0000000828";
        //userID=String.valueOf(Long.parseLong(userID));
        System.out.println(">>");
        //Date defaultRegistrationDate = new Date(Calendar.getInstance().getTimeInMillis()-1000 * 60 * 60 * 24);
        //SimpleDateFormat sdf= new SimpleDateFormat("dd/MM/yy");
        //System.out.println(sdf.format(defaultRegistrationDate));

        RetrieveUserAttributes retrieveUserAttributes = new RetrieveUserAttributes();
        retrieveUserAttributes.getUserBasicAttributes("*", retrieveUserAttributes.getLdapContext());
    }

    public LdapContext getLdapContext() {
        LdapContext ctx = null;
        try {
            Hashtable<String, String> env = new Hashtable<String, String>();
            env.put(Context.INITIAL_CONTEXT_FACTORY,
                    "com.sun.jndi.ldap.LdapCtxFactory");
            env.put(Context.SECURITY_AUTHENTICATION, "Simple");
            env.put(Context.SECURITY_PRINCIPAL, "testkorus1");
            env.put(Context.SECURITY_CREDENTIALS, "tRebreC2ch6S");
            env.put(Context.PROVIDER_URL, "ldap://ns2.fccho-moscow.ru:389");
            //env.put(Context.SECURITY_PRINCIPAL, "e5company");
            //env.put(Context.SECURITY_CREDENTIALS, "q1w2e3r4t5y6");
            //env.put(Context.PROVIDER_URL, "ldap://10.128.225.17:389");
            ctx = new InitialLdapContext(env, null);
            System.out.println("Connection Successful.");
        } catch (NamingException nex) {
            System.out.println("LDAP Connection: FAILED");
            nex.printStackTrace();
        }
        return ctx;
    }

    private void getUserBasicAttributes(String username, LdapContext ctx) {
        try {

            SearchControls constraints = new SearchControls();
            constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
            String[] attrIDs = {"*"};
            constraints.setReturningAttributes(attrIDs);
            //First input parameter is search bas, it can be "CN=Users,DC=YourDomain,DC=com"
            //Second Attribute can be uid=username
            //NamingEnumeration<SearchResult> answer = ctx.search("CN=users,DC=fccho-moscow,DC=ru", "sAMAccountName="
            //NamingEnumeration<SearchResult> answer = ctx.search("DC=fccho-moscow,DC=ru", "(&(objectClass=user)(employeeID=*))", constraints);
            NamingEnumeration<SearchResult> answer = ctx.search("OU=fnkc-users,OU=FNKC,DC=fccho-moscow,DC=ru", "(&(objectClass=user)(employeeID=*))", constraints);
            if (answer.hasMore()) {
                while (answer.hasMore()) {
                    SearchResult user = (SearchResult) answer.next();
                    //Attributes attrs = ((SearchResult) answer.next()).getAttributes();
                    Attributes attrs = user.getAttributes();
                    NamingEnumeration<String> attrsIDs = attrs.getIDs();
                    //while(attrsIDs.hasMore()){
                    //String attrID=attrsIDs.nextElement();
                    //System.out.println(attrID+"="+attrs.get(attrID));
                    //}
                    Attribute attribute = attrs.get("givenName");
                    String userFirstName = "";
                    if (attribute != null) {
                        userFirstName = (String) attribute.get();
                    }

                    String userSecondName = "";
                    userSecondName = (userFirstName.indexOf(" ") >= 0) ? userFirstName.substring(userFirstName.lastIndexOf(" ")).trim() : "";
                    userFirstName = (userFirstName.indexOf(" ") >= 0) ? userFirstName.substring(0, userFirstName.lastIndexOf(" ")) : userFirstName;
                    System.out.println(userSecondName);
                    System.out.println(userFirstName);

                    System.out.println("###########################################################################");
                }
            } else {
                throw new Exception("Invalid User");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return;
    }

}  
