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
import javax.persistence.Transient;

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
        if (userName != null && !userName.isEmpty() && password != null && !password.isEmpty()) {
            try {
                UserDAO dao = getDAO(UserDAOHibernate.class, ApplicationHelper.USER_DAO);
                loggedUser = dao.findByLoginAndPassword(userName, ru.efive.sql.util.ApplicationHelper.getMD5(password));
                if (loggedUser != null) {
                    //Проверка удаленности\уволенности сотрудника
                    if(loggedUser.isDeleted() || loggedUser.isFired()){
                        FacesContext.getCurrentInstance().addMessage(null, loggedUser.isDeleted() ? MSG_DELETED : MSG_FIRED);
                        loggedUser = null;
                        return;
                    }
                   //Проверка наличия у пользователя ролей
                    if(loggedUser.getRoles().isEmpty()){
                        FacesContext.getCurrentInstance().addMessage(null, MSG_NO_ROLE);
                        loggedUser = null;
                        return;
                    }
                    //Выставление признаков ролей
                    isAdministrator = loggedUser.isAdministrator();
                    isRecorder = loggedUser.isRecorder();
                    isOfficeManager = loggedUser.isOfficeManager();
                    isRequestManager = loggedUser.isRequestManager();
                    isEmployer = loggedUser.isEmployer();
                    isOuter = loggedUser.isOuter();
                    isHr = loggedUser.isHr();

                    RequestDocumentDAOImpl docDao = getDAO(RequestDocumentDAOImpl.class, ApplicationHelper.REQUEST_DOCUMENT_FORM_DAO);
                    reqDocumentsCount = docDao.countAllDocumentsByUser((String) null, loggedUser, false, false);
                    FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(AUTH_KEY, loggedUser.getLogin());

                    Object requestUrl = FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(BACK_URL);

                    if (requestUrl != null) {
                        backUrl = requestUrl.toString();
                        System.out.println("back url: " + backUrl);
                    }
                } else {
                    FacesContext.getCurrentInstance().addMessage(null, MSG_NOT_FOUND);
                }
            } catch (Exception e) {
                FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove(AUTH_KEY);
                FacesContext.getCurrentInstance().addMessage(null, MSG_ERROR);
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
        //WTF !!?!
        ExternalContext externalContext = facesContext.getExternalContext();
        //externalContext.getSessionMap().remove(AUTH_KEY);
        //Вот как это делается
        externalContext.invalidateSession();
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
        if (isFromSessionCached) {
            return loggedUser;
        } else {
            User user = getDAO(UserDAOHibernate.class, ApplicationHelper.USER_DAO).findByLoginAndPassword(loggedUser.getLogin(), loggedUser.getPassword());
            return user;
        }
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

    public boolean isCanViewRequestDocuments() {
        return reqDocumentsCount > 0;
    }


    private User loggedUser;
    private long reqDocumentsCount = 0;

    private String userName;
    private String password;

    private String backUrl;

    //Назначенные роли
    private boolean isAdministrator = false;
    private boolean isRecorder = false;
    private boolean isOfficeManager = false;
    private boolean isRequestManager = false;
    private boolean isEmployer = false;
    private boolean isOuter = false;
    private boolean isHr = false;

    //Сообщения об ошибках авторизации
    private final static FacesMessage MSG_FIRED = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Невозможно выполнить вход, потому что сотрудник уволен", "");
    private final static FacesMessage MSG_DELETED = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Невозможно выполнить вход, потому что сотрудник удален", "");
    private final static FacesMessage MSG_ERROR = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ошибка при входе в систему. Попробуйте повторить позже.", "");
    private final static FacesMessage MSG_NOT_FOUND = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Введены неверные данные.", "");
    private final static FacesMessage MSG_NO_ROLE = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Обратитесь к администратору системы для получения доступа", "");



    public boolean isAdministrator() {
        return isAdministrator;
    }

    public void setAdministrator(boolean isAdministrator) {
        this.isAdministrator = isAdministrator;
    }

    public boolean isRecorder() {
        return isRecorder;
    }

    public void setRecorder(boolean isRecorder) {
        this.isRecorder = isRecorder;
    }

    public boolean isOfficeManager() {
        return isOfficeManager;
    }

    public void setOfficeManager(boolean isOfficeManager) {
        this.isOfficeManager = isOfficeManager;
    }

    public boolean isRequestManager() {
        return isRequestManager;
    }

    public void setRequestManager(boolean isRequestManager) {
        this.isRequestManager = isRequestManager;
    }

    public boolean isEmployer() {
        return isEmployer;
    }

    public void setEmployer(boolean isEmployer) {
        this.isEmployer = isEmployer;
    }

    public boolean isOuter() {
        return isOuter;
    }

    public void setOuter(boolean isOuter) {
        this.isOuter = isOuter;
    }

    public boolean isHr() {
        return isHr;
    }

    public void setHr(boolean isHr) {
        this.isHr = isHr;
    }

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