package ru.efive.dms.uifaces.beans;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import ru.efive.dao.InitializationException;
import ru.efive.dao.alfresco.AlfrescoDAO;
import ru.efive.dao.alfresco.AlfrescoNode;
import ru.efive.dms.dao.RequestDocumentDAOImpl;
import ru.efive.dms.dao.ejb.SubstitutionDaoImpl;
import ru.efive.sql.dao.DictionaryDAO;
import ru.efive.sql.dao.GenericDAO;
import ru.efive.sql.dao.user.UserDAO;
import ru.efive.sql.dao.user.UserDAOHibernate;
import ru.entity.model.user.Substitution;
import ru.entity.model.user.User;
import ru.entity.model.user.UserAccessLevel;

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
import static ru.efive.dms.util.ApplicationDAONames.*;


@Named("sessionManagement")
@SessionScoped
public class SessionManagementBean implements Serializable {

    public static final String AUTH_KEY = "app.user.name";
    public static final String BACK_URL = "app.back.url";

    private final static Logger LOGGER = LoggerFactory.getLogger("AUTH");

    private User loggedUser;
    private long reqDocumentsCount = 0;

    private String userName;
    private String password;

    private String backUrl;

    //Замещает ли текущий пользователь кого-либо
    private boolean isSubstitution;
    private List<User> substitutedUsers;

    public List<User> getSubstitutedUsers() {
        return substitutedUsers;
    }

    //Назначенные роли
    private boolean isAdministrator = false;
    private boolean isRecorder = false;
    private boolean isOfficeManager = false;
    private boolean isRequestManager = false;
    private boolean isEmployer = false;
    private boolean isOuter = false;
    private boolean isHr = false;
    private boolean isFilling = false;

    @Inject
    @Named("indexManagement")
    private IndexManagementBean indexManagement;

    private static final long serialVersionUID = -916300301346029630L;


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
                LOGGER.info("Try to login [{}][{}]", userName, password);
                UserDAO dao = getDAO(UserDAOHibernate.class, USER_DAO);
                loggedUser = dao.findByLoginAndPassword(userName, ru.util.ApplicationHelper.getMD5(password));
                if (loggedUser != null) {
                    LOGGER.debug("By userName[{}] founded User[{}]", userName, loggedUser.getId());
                    //Проверка удаленности\уволенности сотрудника
                    if (loggedUser.isDeleted() || loggedUser.isFired()) {
                        LOGGER.error("USER[{}] IS {}", loggedUser.getId(), loggedUser.isDeleted() ? "DELETED" : "FIRED");
                        FacesContext.getCurrentInstance().addMessage(null, loggedUser.isDeleted() ? MSG_AUTH_DELETED : MSG_AUTH_FIRED);
                        loggedUser = null;
                        return;
                    }
                    //Проверка наличия у пользователя ролей
                    if (loggedUser.getRoles().isEmpty()) {
                        LOGGER.warn("USER[{}] HAS NO ONE ROLE", loggedUser.getId());
                        FacesContext.getCurrentInstance().addMessage(null, MSG_AUTH_NO_ROLE);
                        loggedUser = null;
                        return;
                    }
                    // Проверка уровня доступа
                    if (loggedUser.getMaxUserAccessLevel() == null) {
                        LOGGER.warn("USER[{}] HAS NULL MAX_ACCESS_LEVEL", loggedUser.getId());
                        FacesContext.getCurrentInstance().addMessage(null, MSG_AUTH_NO_MAX_ACCESS_LEVEL);
                        loggedUser = null;
                        return;
                    }
                    // Выставление текущего уровня допуска
                    if (loggedUser.getCurrentUserAccessLevel() == null) {
                        LOGGER.warn("USER[{}] HAS NO CURRENT_ACCESS_LEVEL", loggedUser.getId());
                        loggedUser.setCurrentUserAccessLevel(loggedUser.getMaxUserAccessLevel());
                        loggedUser = dao.save(loggedUser);
                    }
                    //Поиск замещений, где найденный пользователь является заместителем
                    final List<Substitution> substitutions = getDAO(SubstitutionDaoImpl.class, SUBSTITUTION_DAO).findCurrentDocumentsOnSubstitution(loggedUser, false);
                    substitutedUsers = new ArrayList<User>(substitutions.size());
                    if (!substitutions.isEmpty()) {
                        LOGGER.debug("Current user is substitution for: {} users", substitutions.size());
                        for (Substitution substitution : substitutions) {
                            if (LOGGER.isTraceEnabled()) {
                                LOGGER.trace(substitution.toString());
                            }
                            if (substitution.getPerson() != null) {
                                substitutedUsers.add(substitution.getPerson());
                                LOGGER.debug("[{}] {}", substitution.getPerson().getId(), substitution.getPerson().getDescription());
                            } else {
                                LOGGER.warn("Substitution[{}] has NULL Person! DATA={}", substitution.getId(), substitution.toString());
                            }
                        }
                    }
                    //Высталвение признака замещения
                    isSubstitution = !substitutedUsers.isEmpty();

                    //Выставление признаков ролей
                    setRoleFlags(loggedUser);
                    if (isSubstitution) {
                        addRoleFlags(substitutedUsers);
                    }
                    //В лог флажки ролей
                    if (LOGGER.isDebugEnabled()) {
                        printRoleFlags();
                    }

                    RequestDocumentDAOImpl docDao = getDAO(RequestDocumentDAOImpl.class, REQUEST_DOCUMENT_FORM_DAO);
                    reqDocumentsCount = docDao.countAllDocumentsByUser((String) null, loggedUser, false, false);
                    FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(AUTH_KEY, loggedUser.getLogin());

                    Object requestUrl = FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(BACK_URL);
                    if (requestUrl != null) {
                        backUrl = requestUrl.toString();
                        LOGGER.info("back url={}", backUrl);
                    }
                    LOGGER.info("SUCCESSFUL LOGIN:{}", loggedUser.getId());
                } else {
                    LOGGER.error("USER[{}] NOT FOUND", userName);
                    FacesContext.getCurrentInstance().addMessage(null, MSG_AUTH_NOT_FOUND);
                }
            } catch (Exception e) {
                FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove(AUTH_KEY);
                FacesContext.getCurrentInstance().addMessage(null, MSG_AUTH_ERROR);
                loggedUser = null;
                LOGGER.error("Exception while processing login action:", e);
            }
        }
    }

    /**
     * Установка флагов ролей для группы пользователей
     * @param userList  группа пользователей
     */
    private void addRoleFlags(List<User> userList) {
        if(!hasAllRoles()) {
            for (User user : userList) {
                if (!isAdministrator) {
                    isAdministrator = user.isAdministrator();
                }
                if (!isRecorder) {
                    isRecorder = user.isRecorder();
                }
                if (!isOfficeManager) {
                    isOfficeManager = user.isOfficeManager();
                }
                if (!isRequestManager) {
                    isRequestManager = user.isRequestManager();
                }
                if (!isEmployer) {
                    isEmployer = user.isEmployer();
                }
                if (!isOuter) {
                    isOuter = user.isOuter();
                }
                if (!isHr) {
                    isHr = user.isHr();
                }
                if(!isFilling){
                    isFilling = user.isFilling();
                }
                //Все роли есть - поиск дальше не нужен
                if(hasAllRoles()){
                    break;
                }
            }
        }
    }

    /**
     * Выставлены все роли?
     * @return true - все, false - есть невыставленные роли
     */
    private boolean hasAllRoles() {
        return isAdministrator && isRecorder && isOfficeManager && isRequestManager && isEmployer && isOuter && isHr && isFilling;
    }

    /**
     * Установка флагов ролей
     * @param loggedUser пользователь для которого устанавливаются флаги ролей
     */
    private void setRoleFlags(User loggedUser) {
        isAdministrator = loggedUser.isAdministrator();
        isRecorder = loggedUser.isRecorder();
        isOfficeManager = loggedUser.isOfficeManager();
        isRequestManager = loggedUser.isRequestManager();
        isEmployer = loggedUser.isEmployer();
        isOuter = loggedUser.isOuter();
        isHr = loggedUser.isHr();
        isFilling = loggedUser.isFilling();
    }

    private void printRoleFlags() {
        if (isAdministrator) {
            LOGGER.debug("ADMINISTRATOR");
        }
        if (isRecorder) {
            LOGGER.debug("RECORDER");
        }
        if (isOfficeManager) {
            LOGGER.debug("OFFICE_MANAGER");
        }
        if (isRequestManager) {
            LOGGER.debug("REQUEST_MANAGER");
        }
        if (isEmployer) {
            LOGGER.debug("EMPLOYER");
        }
        if (isOuter) {
            LOGGER.debug("OUTER");
        }
        if (isHr) {
            LOGGER.debug("HR");
        }
        if(isFilling){
            LOGGER.debug("FILLING");
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
        ApplicationContext context = indexManagement.getContext();
        AlfrescoDAO alfrescoDao = (AlfrescoDAO) context.getBean("alfrescoDao");
        try {
            alfrescoDao.initClass(class_);
            if (!alfrescoDao.connect()) throw new InitializationException();
        } catch (InitializationException e) {
            LOGGER.error("Unable to instantiate connection to Alfresco remote service");
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
        externalContext.invalidateSession();
        LOGGER.info("LOGOUT");
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
            LOGGER.warn("GET USER WITHOUT Session");
            return getDAO(UserDAOHibernate.class, USER_DAO).findByLoginAndPassword(loggedUser.getLogin(), loggedUser.getPassword());
        }
    }

    public String getBackUrl() {
        String url = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath();
        if (StringUtils.isEmpty(backUrl)) {
            return url + "/component/in/in_documents.xhtml";
        } else {
            LOGGER.info("redirectUrl:{}", backUrl);
            final String redirectTo = url + backUrl;
            //Должно стрелять только один раз
            backUrl = "";
            return redirectTo;
        }
    }

    public void setCurrentUserAccessLevel(final UserAccessLevel userAccessLevel) {
        try {
            loggedUser.setCurrentUserAccessLevel(userAccessLevel);
            loggedUser = getDAO(UserDAOHibernate.class, USER_DAO).save(loggedUser);
        } catch (Exception e) {
            LOGGER.error("CANNOT change UserAccessLevel:", e);
        }
    }

    public boolean isCanViewRequestDocuments() {
        return reqDocumentsCount > 0;
    }

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

    public boolean isSubstitution() {
        return isSubstitution;
    }

    public void setSubstitution(boolean isSubstitution) {
        this.isSubstitution = isSubstitution;
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
}