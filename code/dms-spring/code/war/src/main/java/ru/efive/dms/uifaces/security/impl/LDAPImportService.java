package ru.efive.dms.uifaces.security.impl;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Named;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.PagedResultsControl;
import javax.naming.ldap.PagedResultsResponseControl;

import ru.efive.sql.dao.user.UserDAOHibernate;
import ru.efive.sql.entity.user.User;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.util.ApplicationHelper;

public class LDAPImportService  {

	private String DOMAIN_VALUE="fccho-moscow.ru";

	// Адрес сервера LDAP
	private final String ADDRESS_NAME = "java.naming.provider.url";
	private String ADDRESS_VALUE = "ldap://ns2.fccho-moscow.ru:389";

	// DN пользователя для соединения
	private final String LOGIN_NAME = "java.naming.security.principal";
	private String LOGIN_VALUE = "cn=testkorus1,ou=users,dc=fccho-moscow,dc=ru";

	// Пароль пользователя
	private final String PASSWORD_NAME = "java.naming.security.credentials";
	private String PASSWORD_VALUE = "tRebreC2ch6S";

	// Базовый DN
	private final String BASE_NAME = "baseDN";
	private String BASE_VALUE = "ou=users,dc=fccho-moscow,dc=ru";

	// Атрибут универсального идентификатора
	private final String UNID_ATTR_NAME = "userGUIDAttribute";
	private String UNID_ATTR_VALUE = "objectGUID";

	// Атрибут логина
	private final String LOGIN_ATTR_NAME = "userLoginAttribute";
	private String LOGIN_ATTR_VALUE = "mail";

	// Атрибут имени
	private final String FIRST_NAME_ATTR_NAME = "userFirstNameAttribute";
	private String FIRST_NAME_ATTR_VALUE = "givenName";
	
	// Атрибут фамилии
	private final String LAST_NAME_ATTR_NAME = "userLastNameAttribute";
	private String LAST_NAME_ATTR_VALUE = "sn";

	// Атрибут почтового адреса (если не указан, равен логин + @ + почтовый
	// домен)
	private final String MAIL_ATTR_NAME = "userMailAttribute";
	private String MAIL_ATTR_VALUE = "mail";

	// Атрибут телефона
	private final String PHONE_ATTR_NAME = "userPhoneAttribute";
	private String PHONE_ATTR_VALUE = "telephoneNumber";

	// Фильтр пользователей
	private final String FILTER_NAME = "userFilter";
	private String FILTER_VALUE = "(objectClass=user)";

	private String LAST_MODIFIED_ATTR_VALUE = "whenChanged";
	
	public void run() {
		try {
			importActiveDirectoryUsers();
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	private void importActiveDirectoryUsers() throws Exception {
		LdapContext context = null;
		try {
			Properties properties=new Properties();
			properties.put(ADDRESS_NAME, ADDRESS_VALUE);
			properties.put(LOGIN_NAME, LOGIN_VALUE);
			properties.put(PASSWORD_NAME, PASSWORD_VALUE);
			properties.put("java.naming.factory.initial", "com.sun.jndi.ldap.LdapCtxFactory");
			
			context=new InitialLdapContext(properties,null);
			
			SearchControls controls=new SearchControls();
			controls.setReturningAttributes(new String[] { "distinguishedName",
					UNID_ATTR_VALUE, LOGIN_ATTR_VALUE, LAST_NAME_ATTR_VALUE, FIRST_NAME_ATTR_VALUE, MAIL_ATTR_VALUE,
					PHONE_ATTR_VALUE });
			controls.setSearchScope(SearchControls.SUBTREE_SCOPE);

			//NamingEnumeration ldapUsers=context.search(BASE_VALUE, FILTER_VALUE, controls);
			
			int pageSize=100;
			byte[] cookie=null;
			Control[] paginator = new Control[]{new PagedResultsControl(pageSize, Control.CRITICAL)};
			context.setRequestControls(paginator);		
			do{
				NamingEnumeration ldapUsers=context.search(BASE_VALUE, FILTER_VALUE, controls);

				UserDAOHibernate userDAO=sessionManagement.getDAO(UserDAOHibernate.class, ApplicationHelper.USER_DAO);
				List<User> localUsers=userDAO.findAllUsers(true, true, true);
				Map<String,User> localUsersCache=new HashMap<String,User>();
				for(User user:localUsers){
					localUsersCache.put(user.getGUID(), user);
				}

				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

				
				while (ldapUsers != null && ldapUsers.hasMore())
				{
					SearchResult result=(SearchResult) ldapUsers.next();

					Attributes attributes=result.getAttributes();

					Attribute attribute=attributes.get(UNID_ATTR_VALUE);
					byte[] userByteGUID = (byte[]) attributes.get(UNID_ATTR_VALUE).get();
					String userStrGUID=getByte2StrGUID(userByteGUID);

					attribute = attributes.get(LAST_MODIFIED_ATTR_VALUE);
					Date lastModified = null;
					if (attribute != null) {
						lastModified =sdf.parse((String) attribute.get()); ;									    			   				  
					}

					if (localUsersCache.containsKey(userStrGUID)) {					
						// Пользователь уже есть в ORD
						// Обновляем поля
						User user=localUsersCache.get(userStrGUID);					
						if(user!=null){
							if(user.getLastModified()!=null){
								if(user.getLastModified().before(lastModified)){
								}	
								continue;
							}
						}
					}

					attribute=attributes.get(LAST_NAME_ATTR_VALUE);
					String userLastName="";
					if (attribute != null) {
						userLastName = (String) attribute.get();				
					}

					attribute=attributes.get(FIRST_NAME_ATTR_VALUE);
					String userFirstName="";
					if (attribute != null) {
						userFirstName = (String) attribute.get();				
					}
					
					attribute = attributes.get(MAIL_ATTR_VALUE);
					String userMail;
					if (attribute != null) {
						userMail = (String) attribute.get();
					} else {
						userMail = userStrGUID.replace(' ', '.') + '@' + DOMAIN_VALUE;
					}

					attribute = attributes.get(PHONE_ATTR_VALUE);
					String userPhone = null;
					if (attribute != null) {
						userPhone = ((String) attribute.get()).replace("(", "")
								.replace(")", "");
					}						

					// Создаем пользователя
					User newUser=new User();
					newUser.setGUID(userStrGUID);
					newUser.setCreated(Calendar.getInstance(ApplicationHelper.getLocale()).getTime());
					newUser.setLastModified(lastModified);
					newUser.setDeleted(false);						
					newUser.setEmail(userMail);
					newUser.setLastName(userLastName);
					newUser.setFirstName(userFirstName);
					newUser.setWorkPhone(userPhone);
					newUser=userDAO.save(newUser);

				}
				// examine the response controls
				cookie = parseControls(context.getResponseControls());

		        // pass the cookie back to the server for the next page
				context.setRequestControls(new Control[]{new PagedResultsControl(pageSize, cookie, Control.CRITICAL) });
			} while ((cookie != null) && (cookie.length != 0));
						
		} finally {
			if (context != null) {
				context.close();
			}
		}
	}

	static byte[] parseControls(Control[] controls) throws NamingException {

		byte[] cookie = null;

		if (controls != null) {

			for (int i = 0; i < controls.length; i++) {
				if (controls[i] instanceof PagedResultsResponseControl) {
					PagedResultsResponseControl prrc = (PagedResultsResponseControl)controls[i];
					cookie = prrc.getCookie();
				}
			}
		}

		return (cookie == null) ? new byte[0] : cookie;
	}
	
	public String getName() {
		return "LDAP Importer";
	}

	public String getDescription() {
		return "Импортирует пользователей из LDAP и меняет имена пользователей, которых нет в LDAP";
	}

	public String getDescription(Map params) {
		return "Раз в день импортирует пользователей из LDAP и изменяет имя пользователям, которых нет в LDAP";
	}

	public int getFieldType(String key) {
		return 0;
	}

	public Map getFieldValues(String key) {
		return null;
	}

	public void init(Map params) {
	}


	public boolean isEnabled(String key) {
		return true;
	}

	public boolean isI18NValues(String key) {
		return false;
	}

	private static String getByte2StrGUID ( byte [] inArr )
	{
		StringBuffer guid = new StringBuffer ();
		for ( int i = 0; i < inArr.length; i++ )
		{
			StringBuffer dblByte = new StringBuffer ( Integer.toHexString ( inArr [ i ] & 0xff ) );
			if ( dblByte.length () == 1 ) {
				guid.append ( "0" );
			}
			guid.append ( dblByte );
		}
		return guid.toString ();
	}

	@Inject @Named("sessionManagement")
	SessionManagementBean sessionManagement = new SessionManagementBean();
}