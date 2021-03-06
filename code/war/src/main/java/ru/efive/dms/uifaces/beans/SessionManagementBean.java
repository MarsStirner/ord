package ru.efive.dms.uifaces.beans;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import ru.efive.dao.InitializationException;
import ru.efive.dao.alfresco.AlfrescoDAO;
import ru.efive.dao.alfresco.AlfrescoNode;
import ru.entity.model.referenceBook.UserAccessLevel;
import ru.entity.model.user.Substitution;
import ru.entity.model.user.User;
import ru.external.AuthorizationData;
import ru.hitsl.sql.dao.GenericDAO;
import ru.hitsl.sql.dao.RequestDocumentDAOImpl;
import ru.hitsl.sql.dao.SubstitutionDaoImpl;
import ru.hitsl.sql.dao.interfaces.DictionaryDAO;
import ru.hitsl.sql.dao.referenceBook.UserAccessLevelDAOImpl;
import ru.hitsl.sql.dao.user.UserDAO;
import ru.hitsl.sql.dao.user.UserDAOHibernate;

import javax.annotation.PreDestroy;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static ru.efive.dms.uifaces.beans.utils.MessageHolder.*;
import static ru.hitsl.sql.dao.util.ApplicationDAONames.*;


@Named("sessionManagement")
@SessionScoped
public class SessionManagementBean implements Serializable {

    public static final String AUTH_KEY = "app.user.name";
    public static final String BACK_URL = "app.back.url";

    private final static Logger LOGGER = LoggerFactory.getLogger("AUTH");
    private static final long serialVersionUID = -916300301346029630L;
    private AuthorizationData authData;
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
    private boolean isFilling = false;
    private boolean isSubstitution = false;
    @Inject
    @Named("indexManagement")
    private IndexManagementBean indexManagement;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isLoggedIn() {
        return FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(AUTH_KEY) != null;
    }

    public void logIn() {
        if (StringUtils.isNotEmpty(userName) && StringUtils.isNotEmpty(password)) {
            LOGGER.info("Try to login [{}][{}]", userName, password);
            try {
                UserDAO dao = getDAO(UserDAOHibernate.class, USER_DAO);
                User loggedUser = dao.findByLoginAndPassword(userName, ru.util.ApplicationHelper.getMD5(password));
                if (loggedUser != null) {
                    LOGGER.debug("By userName[{}] founded User[{}]", userName, loggedUser.getId());
                    //Проверка удаленности\уволенности сотрудника
                    if (loggedUser.isDeleted() || loggedUser.isFired()) {
                        LOGGER.error("USER[{}] IS {}", loggedUser.getId(), loggedUser.isDeleted() ? "DELETED" : "FIRED");
                        FacesContext.getCurrentInstance().addMessage(null, loggedUser.isDeleted() ? MSG_AUTH_DELETED : MSG_AUTH_FIRED);
                        return;
                    }
                    //Проверка наличия у пользователя ролей
                    if (loggedUser.getRoles().isEmpty()) {
                        LOGGER.warn("USER[{}] HAS NO ONE ROLE", loggedUser.getId());
                        FacesContext.getCurrentInstance().addMessage(null, MSG_AUTH_NO_ROLE);
                        return;
                    }
                    // Проверка уровня доступа
                    if (loggedUser.getMaxUserAccessLevel() == null) {
                        LOGGER.warn("USER[{}] HAS NULL MAX_ACCESS_LEVEL", loggedUser.getId());
                        FacesContext.getCurrentInstance().addMessage(null, MSG_AUTH_NO_MAX_ACCESS_LEVEL);
                        return;
                    }
                    // Выставление текущего уровня допуска
                    if (loggedUser.getCurrentUserAccessLevel() == null) {
                        LOGGER.warn("USER[{}] HAS NO CURRENT_ACCESS_LEVEL", loggedUser.getId());
                        loggedUser.setCurrentUserAccessLevel(loggedUser.getMaxUserAccessLevel());
                        loggedUser = dao.save(loggedUser);
                    }
                    //Поиск замещений, где найденный пользователь является заместителем
                    final List<Substitution> substitutions = getDAO(SubstitutionDaoImpl.class, SUBSTITUTION_DAO).findCurrentDocumentsOnSubstitution(
                            loggedUser,
                            false
                    );
                    if (!substitutions.isEmpty()) {
                        this.authData = new AuthorizationData(loggedUser, substitutions);
                    } else {
                        this.authData = new AuthorizationData(loggedUser);
                    }
                    RequestDocumentDAOImpl docDao = getDAO(RequestDocumentDAOImpl.class, REQUEST_DOCUMENT_FORM_DAO);
                    reqDocumentsCount = docDao.countDocumentListByFilters(authData, null, null, false, false);
                    FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(AUTH_KEY, loggedUser.getLogin());

                    Object requestUrl = FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(BACK_URL);
                    if (requestUrl != null) {
                        backUrl = requestUrl.toString();
                        LOGGER.info("back url={}", backUrl);
                    }

                    setRoleFlags();

                    LOGGER.info("SUCCESSFUL LOGIN:{}\n AUTH_DATA={}", loggedUser.getId(), authData);
                } else {
                    LOGGER.error("USER[{}] NOT FOUND", userName);
                    FacesContext.getCurrentInstance().addMessage(null, MSG_AUTH_NOT_FOUND);
                }
            } catch (Exception e) {
                FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove(AUTH_KEY);
                FacesContext.getCurrentInstance().addMessage(null, MSG_AUTH_ERROR);
                this.authData = null;
                LOGGER.error("Exception while processing login action:", e);
            }
        }
    }


    /**
     * Установка флагов ролей
     */
    private void setRoleFlags() {
        isAdministrator = authData.isAdministrator();
        isRecorder = authData.isRecorder();
        isOfficeManager = authData.isOfficeManager();
        isRequestManager = authData.isRequestManager();
        isEmployer = authData.isEmployer();
        isOuter = authData.isOuter();
        isHr = authData.isHr();
        isFilling = authData.isFilling();
        isSubstitution = authData.isSubstitution();
    }


    @SuppressWarnings({"rawtypes", "unchecked"})
    public <T extends DictionaryDAO> T getDictionaryDAO(Class<T> persistentClass, String beanName) {
        return (T) indexManagement.getContext().getBean(beanName);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public <T extends GenericDAO> T getDAO(Class<T> persistentClass, String beanName) {
        return (T) indexManagement.getContext().getBean(beanName);
    }

    public <M extends AlfrescoNode> AlfrescoDAO<M> getAlfrescoDAO(Class<M> class_) {
        ApplicationContext context = indexManagement.getContext();
        AlfrescoDAO alfrescoDao = (AlfrescoDAO) context.getBean("alfrescoDao");
        try {
            alfrescoDao.initClass(class_);
            if (!alfrescoDao.connect()) {
                throw new InitializationException();
            }
        } catch (InitializationException e) {
            LOGGER.error("Unable to instantiate connection to Alfresco remote service");
            alfrescoDao = null;
        }
        return alfrescoDao;
    }

    public String logOut() {
        LOGGER.info("LOGOUT: {}", authData);
        authData = null;
        userName = null;
        password = null;
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext != null) {
            ExternalContext externalContext = facesContext.getExternalContext();
            externalContext.invalidateSession();
        }
        return "/index?faces-redirect=true";
    }

    @PreDestroy
    public void destroy() {
        logOut();
    }

    public User getLoggedUser() {
        return authData.getAuthorized();
    }


    public String getBackUrl() {
        final StringBuilder result = new StringBuilder(FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath());
        if (StringUtils.isEmpty(backUrl)) {
            result.append(authData.getDefaultPage());
        } else {
            result.append(backUrl);
            //Должно стрелять только один раз
            backUrl = "";
        }
        LOGGER.info("redirectUrl: \'{}\'", result.toString());
        return result.toString();
    }

    public void setCurrentUserAccessLevel(final String id) {
        final UserAccessLevel userAccessLevel = getDictionaryDAO(UserAccessLevelDAOImpl.class, USER_ACCESS_LEVEL_DAO).get(Integer.valueOf(id));
        if (userAccessLevel != null) {
            try {
                authData.setCurrentAccessLevel(userAccessLevel);
                getDAO(UserDAOHibernate.class, USER_DAO).save(authData.getAuthorized());
                LOGGER.info("UserAccessLevel changed to {}", userAccessLevel);
            } catch (Exception e) {
                LOGGER.error("CANNOT change UserAccessLevel:", e);
            }
        } else {
            LOGGER.error("CANNOT change UserAccessLevel to {}", id);
        }
    }

    public boolean isCanViewRequestDocuments() {
        return reqDocumentsCount > 0;
    }

    public boolean isAdministrator() {
        return isAdministrator;
    }

    public boolean isRecorder() {
        return isRecorder;
    }

    public boolean isOfficeManager() {
        return isOfficeManager;
    }


    public boolean isRequestManager() {
        return isRequestManager;
    }

    public boolean isEmployer() {
        return isEmployer;
    }

    public boolean isOuter() {
        return isOuter;
    }


    public boolean isHr() {
        return isHr;
    }


    public boolean isSubstitution() {
        return isSubstitution;
    }


    public IndexManagementBean getIndexManagement() {
        return indexManagement;
    }

    public void setIndexManagement(IndexManagementBean indexManagement) {
        this.indexManagement = indexManagement;
    }

    public boolean isFilling() {
        return isFilling;
    }


    public List<User> getSubstitutedUsers() {
        return new ArrayList<User>(authData.getSubstitutedUsers());
    }


    public AuthorizationData getAuthData() {
        return authData;
    }
}