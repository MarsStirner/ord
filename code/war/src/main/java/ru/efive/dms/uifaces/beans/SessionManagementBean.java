package ru.efive.dms.uifaces.beans;

import com.github.javaplugs.jsf.SpringScopeSession;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.MenuModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
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
@Transactional("ordTransactionManager")
public class SessionManagementBean implements Serializable {

    public static final String AUTH_KEY = "app.user.name";
    public static final String BACK_URL = "app.back.url";
    private final static Logger log = LoggerFactory.getLogger("AUTH");
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

    private DefaultMenuModel accessLevelsMenuModel;


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

    public MenuModel getAccessLevelsMenuModel() {
        if (accessLevelsMenuModel == null) {
            log.debug("Initialize new AccessLevelsMenuModel for PrimeFaces dropdown");
            accessLevelsMenuModel = new DefaultMenuModel();
            for (UserAccessLevel current : userAccessLevelDao.findLowerThenLevel(authData.getMaxAccessLevel().getLevel())) {
                final DefaultMenuItem currentItem = new DefaultMenuItem(current.getValue());
                currentItem.setCommand("#{sessionManagement.setCurrentUserAccessLevel(\'".concat(String.valueOf(current.getId())).concat("\')}"));
                currentItem.setUpdate("accessMenuButton accessMenu");
                accessLevelsMenuModel.addElement(currentItem);
            }
        }
        return accessLevelsMenuModel;
    }

    public void logIn() {
        if (StringUtils.isNotEmpty(userName) && StringUtils.isNotEmpty(password)) {
            log.info("Try to login [{}][{}]", userName, password);
            try {
                User loggedUser = userDao.findByLoginAndPassword(userName, ru.util.ApplicationHelper.getMD5(password));
                if (loggedUser != null) {
                    log.debug("By userName[{}] founded User[{}]", userName, loggedUser.getId());
                    //Проверка удаленности\уволенности сотрудника
                    if (loggedUser.isDeleted() || loggedUser.isFired()) {
                        log.error("USER[{}] IS {}", loggedUser.getId(), loggedUser.isDeleted() ? "DELETED" : "FIRED");
                        FacesContext.getCurrentInstance().addMessage(null, loggedUser.isDeleted() ? MSG_AUTH_DELETED : MSG_AUTH_FIRED);
                        return;
                    }
                    //Проверка наличия у пользователя ролей
                    if (loggedUser.getRoles().isEmpty()) {
                        log.warn("USER[{}] HAS NO ONE ROLE", loggedUser.getId());
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
                        log.info("back url={}", backUrl);
                    }
                    log.info("SUCCESSFUL LOGIN:{}\n AUTH_DATA={}", loggedUser.getId(), authData);
                } else {
                    log.error("USER[{}] NOT FOUND", userName);
                    FacesContext.getCurrentInstance().addMessage(null, MSG_AUTH_NOT_FOUND);
                }
            } catch (Exception e) {
                FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove(AUTH_KEY);
                FacesContext.getCurrentInstance().addMessage(null, MSG_AUTH_ERROR);
                log.error("Exception while processing login action:", e);
            }
        }
    }


    public String logOut() {
        log.info("LOGOUT: {}", authData);
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
        log.info("redirectUrl: \'{}\'", result.toString());
        return result.toString();
    }

    public void setCurrentUserAccessLevel(final String id) {
        final UserAccessLevel userAccessLevel = userAccessLevelDao.get(Integer.valueOf(id));
        if (userAccessLevel != null) {
            try {
                authData.setCurrentAccessLevel(userAccessLevel);
                userDao.update(authData.getAuthorized());
                log.info("UserAccessLevel changed to {}", userAccessLevel);
            } catch (Exception e) {
                log.error("CANNOT change UserAccessLevel:", e);
                //TODO msg for user
            }
        } else {
            log.error("CANNOT change UserAccessLevel to {}", id);
            //TODO msg for user
        }
    }
}