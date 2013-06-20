package ru.efive.dms.uifaces.security.impl;
import java.util.Hashtable;  

import javax.naming.Context;  
import javax.naming.NamingEnumeration;  
import javax.naming.NamingException;  
import javax.naming.directory.Attributes;  
import javax.naming.directory.SearchControls;  
import javax.naming.directory.SearchResult;  
import javax.naming.ldap.InitialLdapContext;  
import javax.naming.ldap.LdapContext; 

public class RetrieveUserAttributes {  
<<<<<<< .mine
  
    public static void main(String[] args) {  
        RetrieveUserAttributes retrieveUserAttributes = new RetrieveUserAttributes();  
        retrieveUserAttributes.getUserBasicAttributes("e5company", retrieveUserAttributes.getLdapContext());  
    }  
  
    public LdapContext getLdapContext(){  
        LdapContext ctx = null;  
        try{  
            Hashtable<String, String> env=new Hashtable<String, String>();  
            env.put(Context.INITIAL_CONTEXT_FACTORY,  
                    "com.sun.jndi.ldap.LdapCtxFactory");  
            env.put(Context.SECURITY_AUTHENTICATION, "Simple");  
            env.put(Context.SECURITY_PRINCIPAL, "e5company");  
            env.put(Context.SECURITY_CREDENTIALS, "1q2w3e4r5t6y");  
            env.put(Context.PROVIDER_URL, "ldap://10.128.225.17:389");  
            ctx = new InitialLdapContext(env, null);  
            System.out.println("Connection Successful.");  
        }catch(NamingException nex){  
            System.out.println("LDAP Connection: FAILED");  
            nex.printStackTrace();  
        }  
        return ctx;  
    }  
  
    private void getUserBasicAttributes(String username, LdapContext ctx) {           
        try {  
  
            SearchControls constraints = new SearchControls();  
            constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);  
            String[] attrIDs = { "distinguishedName",  
                    "sn",  
                    "givenname",  
                    "mail",  
                    "telephonenumber"};  
            constraints.setReturningAttributes(attrIDs);  
            //First input parameter is search bas, it can be "CN=Users,DC=YourDomain,DC=com"  
            //Second Attribute can be uid=username  
            NamingEnumeration<SearchResult> answer = ctx.search("DC=niidg,DC=ru", "sAMAccountName="  
                    + username, constraints);  
            if (answer.hasMore()) {  
                Attributes attrs = ((SearchResult) answer.next()).getAttributes();  
                System.out.println("distinguishedName "+ attrs.get("distinguishedName"));  
                System.out.println("givenname "+ attrs.get("givenname"));  
                System.out.println("sn "+ attrs.get("sn"));  
                System.out.println("mail "+ attrs.get("mail"));  
                System.out.println("telephonenumber "+ attrs.get("telephonenumber"));  
            }else{  
                throw new Exception("Invalid User");  
            }  
  
        } catch (Exception ex) {  
            ex.printStackTrace();  
        }  
        return;  
    }  
  
=======

	public static void main(String[] args) {  
		RetrieveUserAttributes retrieveUserAttributes = new RetrieveUserAttributes();  
		retrieveUserAttributes.getUserBasicAttributes("*", retrieveUserAttributes.getLdapContext());  
	}  

	public LdapContext getLdapContext(){  
		LdapContext ctx = null;  
		try{  
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
		}catch(NamingException nex){  
			System.out.println("LDAP Connection: FAILED");  
			nex.printStackTrace();  
		}  
		return ctx;  
	}  

	private void getUserBasicAttributes(String username, LdapContext ctx) {           
		try {  

			SearchControls constraints = new SearchControls();  
			constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);  
			String[] attrIDs = { "*"};  
			constraints.setReturningAttributes(attrIDs);  
			//First input parameter is search bas, it can be "CN=Users,DC=YourDomain,DC=com"  
			//Second Attribute can be uid=username  
			NamingEnumeration<SearchResult> answer = ctx.search("CN=users,DC=fccho-moscow,DC=ru", "sAMAccountName="  
					+ username, constraints);  
			if (answer.hasMore()) {
				while(answer.hasMore()){
					SearchResult user=(SearchResult) answer.next();
					//Attributes attrs = ((SearchResult) answer.next()).getAttributes();
					Attributes attrs = user.getAttributes();
					NamingEnumeration<String> attrsIDs=attrs.getIDs();
					while(attrsIDs.hasMore()){
						String attrID=attrsIDs.nextElement();
						System.out.println(attrID+"="+attrs.get(attrID));
					}
					System.out.println("###########################################################################");
				}
			}else{  
				throw new Exception("Invalid User");  
			}  

		} catch (Exception ex) {  
			ex.printStackTrace();  
		}  
		return ;  
	}  

>>>>>>> .r3510
<<<<<<< .mine
}  =======
}  
>>>>>>> .r3510
