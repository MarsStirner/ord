package ru.efive.dms.uifaces.beans;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.PreDestroy;
import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import ru.efive.dao.InitializationException;
import ru.efive.dao.alfresco.AlfrescoDAO;
import ru.efive.dao.alfresco.AlfrescoNode;
import ru.efive.dms.dao.RequestDocumentDAOImpl;
import ru.efive.dms.util.ApplicationHelper;
import ru.efive.sql.dao.DictionaryDAO;
import ru.efive.sql.dao.GenericDAO;
import ru.efive.sql.dao.user.UserDAO;
import ru.efive.sql.dao.user.UserDAOHibernate;
import ru.efive.sql.entity.user.User;
import ru.efive.sql.entity.user.UserAccessLevel;

@Named("sessionManagement")
@SessionScoped
public class SessionManagementBean implements Serializable {

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public boolean isLoggedIn() {
        return FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(AUTH_KEY) != null;
    }

    public synchronized void logIn() {
        if (userName != null && !userName.equals("") && password != null && !password.equals("")) {
            try {
                UserDAO dao = getDAO(UserDAOHibernate.class, ApplicationHelper.USER_DAO);
                loggedUser = dao.findByLoginAndPassword(userName, password);
                if (loggedUser != null) {
                    RequestDocumentDAOImpl docDao = getDAO(RequestDocumentDAOImpl.class, ApplicationHelper.REQUEST_DOCUMENT_FORM_DAO);
                    loggedUser.setReqDocumentsCount(docDao.countAllDocumentsByUser((String)null, loggedUser, false, false));
                    FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(AUTH_KEY, loggedUser.getLogin());

                    Object requestUrl = FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(BACK_URL);

                    if (requestUrl != null) {
                        backUrl = requestUrl.toString();
                        System.out.println("back url: " + backUrl);
                    }
                } else {
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                            FacesMessage.SEVERITY_ERROR, "Введены неверные данные.", ""));
                }
            } catch (Exception e) {
                FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove(AUTH_KEY);
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_ERROR, "Ошибка при входе в систему. Попробуйте повторить позже.", ""));
                loggedUser = null;
                logger.error("Exception while processing login action", e);
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public <T extends DictionaryDAO> T getDictionaryDAO(Class<T> persistentClass, String beanName) {
        return (T) indexManagement.getContext().getBean(beanName);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public <T extends GenericDAO> T getDAO(Class<T> persistentClass, String beanName) {
        return (T) indexManagement.getContext().getBean(beanName);
    }

    public synchronized <M extends AlfrescoNode> AlfrescoDAO<M> getAlfrescoDAO(Class<M> class_) {
        ClassPathXmlApplicationContext context = indexManagement.getContext();
        AlfrescoDAO alfrescoDao = (AlfrescoDAO) context.getBean("alfrescoDao");
        try {
            alfrescoDao.initClass(class_);
            if (!alfrescoDao.connect()) throw new InitializationException();
        } catch (InitializationException e) {
            logger.error("Unable to instantiate connection to Alfresco remote service");
            alfrescoDao = null;
        }
        return alfrescoDao;
    }

    public synchronized String logOut() {
        loggedUser = null;
        userName = null;
        password = null;
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();
        externalContext.getSessionMap().remove(AUTH_KEY);
        System.out.println("is logged: " + isLoggedIn());
        return "/index?faces-redirect=true";//
    }

    @PreDestroy
    public void destroy() {
        logOut();
    }

    public User getLoggedUser() {
        return loggedUser;
    }

    public User getLoggedUser(boolean isFromSessionCached) {
          return loggedUser;
//        if (isFromSessionCached) {
//            return loggedUser;
//        } else {
//            User user = getDAO(UserDAOHibernate.class, ApplicationHelper.USER_DAO).findByLoginAndPassword(loggedUser.getLogin(), loggedUser.getPassword());
//            return user;
//        }
    }

    public String getBackUrl() {
        String url = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath();
        if (backUrl == null || backUrl.equals("")) {
            return url + "/component/in/in_documents.xhtml";
        } else {
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
            ELContext elContext = context.getELContext();
            Application application = context.getApplication();
            ExpressionFactory expressionFactory = application.getExpressionFactory();
            for (String beanName : registratedBeanNames) {
                System.out.println("beanName->" + beanName);
                ValueExpression valExpr = expressionFactory.createValueExpression(elContext, "#{" + beanName + ".needRefresh}", Boolean.class);
                valExpr.setValue(elContext, true);
            }
        } catch (Exception e) {
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
            if (this.registratedBeanNames.contains(bean)) {
                this.registratedBeanNames.remove(bean);
            }
            ;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private User loggedUser;

    private String userName;
    private String password;


    private String backUrl;


    @Inject
    @Named("indexManagement")
    private transient IndexManagementBean indexManagement;

    public IndexManagementBean getIndexManagement() {
        return indexManagement;
    }

    public void setIndexManagement(IndexManagementBean indexManagement) {
        this.indexManagement = indexManagement;
    }

    public static final String AUTH_KEY = "app.user.name";
    public static final String BACK_URL = "app.back.url";

    private final static Logger logger = Logger.getLogger(SessionManagementBean.class);
    private Set<String> registratedBeanNames = new HashSet<String>();

    private static final long serialVersionUID = -916300301346029630L;
}