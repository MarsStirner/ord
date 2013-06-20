package ru.efive.dms.uifaces.security.impl;

import java.util.Hashtable;  

import javax.naming.Context;  
import javax.naming.NamingException;  
import javax.naming.ldap.InitialLdapContext;  
import javax.naming.ldap.LdapContext;  
  
  
public class LdapContextCreation {  
    public static void main(String[] args) {  
        LdapContextCreation ldapContxCrtn = new LdapContextCreation();  
        LdapContext ctx = ldapContxCrtn.getLdapContext();  
    }  
    public LdapContext getLdapContext(){  
        LdapContext ctx = null;  
        try{  
            Hashtable env = new Hashtable();  
            env.put(Context.INITIAL_CONTEXT_FACTORY,  
                    "com.sun.jndi.ldap.LdapCtxFactory");  
            env.put(Context.SECURITY_AUTHENTICATION, "Simple");  
            //it can be <domain\\userid> something that you use for windows login  
            //it can also be  
            env.put(Context.SECURITY_PRINCIPAL, "Enter user name here");  
            env.put(Context.SECURITY_CREDENTIALS, "Password goes here");  
            //in following property we specify ldap protocol and connection url.  
            //generally the port is 389  
            env.put(Context.PROVIDER_URL, "ldap://xxxxxxxx:389");  
            ctx = new InitialLdapContext(env, null);  
            System.out.println("Connection Successful.");  
        }catch(NamingException nex){  
            System.out.println("LDAP Connection: FAILED");  
            nex.printStackTrace();  
        }  
        return ctx;  
    }  
  
}  