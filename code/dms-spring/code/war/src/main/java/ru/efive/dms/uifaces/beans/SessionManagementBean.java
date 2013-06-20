package ru.efive.dms.uifaces.beans;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.Application;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.log4j.Logger;

import ru.efive.dao.InitializationException;
import ru.efive.dao.alfresco.AlfrescoDAO;
import ru.efive.dao.alfresco.AlfrescoNode;
import ru.efive.sql.dao.DictionaryDAO;
import ru.efive.sql.dao.GenericDAO;
import ru.efive.sql.dao.user.UserDAOHibernate;
import ru.efive.sql.entity.user.User;
import ru.efive.sql.entity.user.UserAccessLevel;
import ru.efive.dms.uifaces.security.UserLoginService;
import ru.efive.dms.util.ApplicationHelper;

@Named("sessionManagement")
@SessionScoped
public class SessionManagementBean implements Serializable {

	public boolean isLoggedIn() {
		return userLoginService.isLoggedIn();
    }
    
	public String logIn() throws IOException, ServletException {
		ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
		RequestDispatcher dispatcher = ((ServletRequest) context.getRequest()).getRequestDispatcher("/j_spring_security_check");
		
		dispatcher.forward((ServletRequest) context.getRequest(), (ServletResponse) context.getResponse());
		
		FacesContext.getCurrentInstance().responseComplete();
		return null;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public <T extends DictionaryDAO> T getDictionaryDAO(Class<T> persistentClass, String beanName) {
		return (T) indexManagement.getContext().getBean(beanName);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public <T extends GenericDAO> T getDAO(Class<T> persistentClass, String beanName) {
		return (T) indexManagement.getContext().getBean(beanName);
	}

	public synchronized <M extends AlfrescoNode> AlfrescoDAO<M> getAlfrescoDAO(Class<M> class_) {
		AlfrescoDAO<M> dao = null;
		try {
			dao = new AlfrescoDAO<M>(class_);
			dao.setUserName("admin");
			dao.setPassword("admin");
			if (!dao.connect()) throw new InitializationException();
		}
		catch (InitializationException e) {
			logger.error("Unable to instantiate connection to Alfresco remote service");
			dao = null;
		}
		return dao;
	}

	public String logout() throws IOException, ServletException {
		ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
		RequestDispatcher dispatcher = ((ServletRequest) context.getRequest()).getRequestDispatcher("/j_spring_security_logout");
		
		dispatcher.forward((ServletRequest) context.getRequest(), (ServletResponse) context.getResponse());
		
		FacesContext.getCurrentInstance().responseComplete();
		return null;
	}

	@PostConstruct
	public void initialize() {
		userLoginService = (UserLoginService) indexManagement.getContext().getBean("userLoginService");
	}

	@PreDestroy
	public void destroy() {
		userLoginService.logout();
	}

	public User getLoggedUser() {
		return userLoginService.getLoggedUser();
	}

	public User getLoggedUser(boolean isFromSessionCached) {
		if(isFromSessionCached){
			return loggedUser;
		}else{
			User user=getDAO(UserDAOHibernate.class, ApplicationHelper.USER_DAO).findByLoginAndPassword(loggedUser.getLogin(),loggedUser.getPassword());
			return user;
		}
	}

	public String getBackUrl() {
		String url = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath();
		if (backUrl == null || backUrl.equals("")) {
			return url + "/component/in_documents.xhtml";
		}
		else {
			logger.info("redirectUrl: " + backUrl);
			return url + backUrl;
		}
	}

	public void setCurrentUserAccessLevel(UserAccessLevel userAccessLevel) {
		//this.currentRole = currentRole;
		try {
			loggedUser.setCurrentUserAccessLevel(userAccessLevel);
			loggedUser = getDAO(UserDAOHibernate.class, ApplicationHelper.USER_DAO).save(loggedUser);
			FacesContext context = FacesContext.getCurrentInstance();			 
			ELContext elContext=context.getELContext();
			Application application=context.getApplication();
			ExpressionFactory expressionFactory=application.getExpressionFactory();
			for(String beanName:registratedBeanNames){
				System.out.println("beanName->"+beanName);
				ValueExpression valExpr=expressionFactory.createValueExpression(elContext, "#{"+beanName+".needRefresh}", Boolean.class);
				valExpr.setValue(elContext,true);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		//return getNavigationRule() + "?faces-redirect=true";
	}   	

	public Set<String> getRegistratedBeanNames() {
		return registratedBeanNames;
	}

	public void registrateBeanName(String bean) {
		try {
			this.registratedBeanNames.add(bean);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void deleteRegistratedBeanName(Object bean) {
		try {
			if(this.registratedBeanNames.contains(bean)){this.registratedBeanNames.remove(bean);};
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private User loggedUser;
	
	private String backUrl;


	@Inject @Named("indexManagement")
	private transient IndexManagementBean indexManagement;
    private UserLoginService userLoginService;

	public static final String BACK_URL = "app.back.url";

	private final static Logger logger = Logger.getLogger(SessionManagementBean.class);
	private Set<String> registratedBeanNames=new HashSet<String>();

	private static final long serialVersionUID = -916300301346029630L;
}