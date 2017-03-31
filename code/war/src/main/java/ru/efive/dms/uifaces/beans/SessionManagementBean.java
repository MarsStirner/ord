package ru.efive.dms.uifaces.beans;

import com.github.javaplugs.jsf.SpringScopeSession;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import ru.entity.model.referenceBook.UserAccessLevel;
import ru.entity.model.user.Substitution;
import ru.entity.model.user.User;
import ru.hitsl.sql.dao.interfaces.SubstitutionDao;
import ru.hitsl.sql.dao.interfaces.UserDao;
import ru.hitsl.sql.dao.interfaces.referencebook.UserAccessLevelDao;
import ru.hitsl.sql.dao.util.AuthorizationData;

import javax.annotation.PreDestroy;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.List;

import static ru.efive.dms.uifaces.beans.utils.MessageHolder.*;


@Controller("sessionManagement")
@SpringScopeSession
public class SessionManagementBean implements Serializable {

    public static final String AUTH_KEY = "app.user.name";
    public static final String BACK_URL = "app.back.url";
    private final static Logger LOGGER = LoggerFactory.getLogger("AUTH");
    @Autowired
    @Qualifier("userAccessLevelDao")
    private UserAccessLevelDao userAccessLevelDao;
    @Autowired
    @Qualifier("userDao")
    private UserDao userDao;
    @Autowired
    @Qualifier("substitutionDao")
    private SubstitutionDao substitutionDao;
    @Autowired
    @Qualifier("authData")
    private AuthorizationData authData;

    private String userName;
    private String password;
    private String backUrl;


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
                User loggedUser = userDao.findByLoginAndPassword(userName, ru.util.ApplicationHelper.getMD5(password));
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
                    //Поиск замещений, где найденный пользователь является заместителем
                    final List<Substitution> substitutions = substitutionDao.getCurrentItemsOnSubstitution(
                            loggedUser,
                            false
                    );
                    if (!substitutions.isEmpty()) {
                        authData.init(loggedUser, substitutions);
                    } else {
                        authData.init(loggedUser);
                    }
                    FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(AUTH_KEY, loggedUser.getLogin());

                    Object requestUrl = FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(BACK_URL);
                    if (requestUrl != null) {
                        backUrl = requestUrl.toString();
                        LOGGER.info("back url={}", backUrl);
                    }
                    LOGGER.info("SUCCESSFUL LOGIN:{}\n AUTH_DATA={}", loggedUser.getId(), authData);
                } else {
                    LOGGER.error("USER[{}] NOT FOUND", userName);
                    FacesContext.getCurrentInstance().addMessage(null, MSG_AUTH_NOT_FOUND);
                }
            } catch (Exception e) {
                FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove(AUTH_KEY);
                FacesContext.getCurrentInstance().addMessage(null, MSG_AUTH_ERROR);
                LOGGER.error("Exception while processing login action:", e);
            }
        }
    }


    public String logOut() {
        LOGGER.info("LOGOUT: {}", authData);
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
        final UserAccessLevel userAccessLevel = userAccessLevelDao.get(Integer.valueOf(id));
        if (userAccessLevel != null) {
            try {
                authData.setCurrentAccessLevel(userAccessLevel);
                userDao.save(authData.getAuthorized());
                LOGGER.info("UserAccessLevel changed to {}", userAccessLevel);
            } catch (Exception e) {
                LOGGER.error("CANNOT change UserAccessLevel:", e);
            }
        } else {
            LOGGER.error("CANNOT change UserAccessLevel to {}", id);
        }
    }
}