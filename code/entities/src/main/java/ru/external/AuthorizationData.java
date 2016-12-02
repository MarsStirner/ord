package ru.external;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.entity.model.referenceBook.UserAccessLevel;
import ru.entity.model.user.Group;
import ru.entity.model.user.Role;
import ru.entity.model.user.Substitution;
import ru.entity.model.user.User;
import ru.util.StoredCodes;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Author: Upatov Egor <br>
 * Date: 31.03.2015, 11:59 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
public class AuthorizationData implements Serializable{

    private final static Logger logger = LoggerFactory.getLogger("AUTH");

    /**
     * Авторизованный пользователь
     */
    private final User authorized;

    /**
     * Суммарный набор ролей
     */
    private final Set<Role> roles;
    private final Set<Integer> roleIds;

    /**
     * Суммарный набор групп
     */
    private final Set<Group> groups;
    private final Set<Integer> groupIds;

    /**
     * Список замещаемых пользователей
     */
    private final Set<User> substitutedUsers = new HashSet<User>(0);
    private final Set<Integer> userIds = new HashSet<Integer>(1);

    /**
     * Уровни допуска
     */
    private final UserAccessLevel maxAccessLevel;
    private UserAccessLevel currentAccessLevel;

    //Замещает ли текущий пользователь кого-либо
    private boolean isSubstitution = false;

    /**
     * Создаем данные авторизации (авторизованный пользователь и его группы + роли и уровни допуска)
     * @param authorized авторизованный пользователь
     */
    public AuthorizationData(final User authorized){
        this.authorized = authorized;
        this.userIds.add(authorized.getId());
        final Set<Role> authorizedRoles = authorized.getRoles();
        this.roles = new HashSet<Role>(authorizedRoles.size());
        this.roleIds = new HashSet<Integer>(authorizedRoles.size());
        for(Role current : authorizedRoles){
            roles.add(current);
            roleIds.add(current.getId());
        }
        final Set<Group> authorizedGroups = authorized.getGroups();
        if(!authorizedGroups.isEmpty()) {
            this.groups = new HashSet<Group>(authorizedGroups.size());
            this.groupIds = new HashSet<Integer>(authorizedGroups.size());
            for(Group current : authorizedGroups){
                groups.add(current);
                groupIds.add(current.getId());
            }
        } else {
            this.groups = new HashSet<Group>(0);
            this.groupIds = new HashSet<Integer>(0);
        }
        this.currentAccessLevel = authorized.getCurrentUserAccessLevel();
        this.maxAccessLevel= authorized.getMaxUserAccessLevel();
    }

    /**
     * Создаем данные авторизации (авторизованный пользователь и список его замещений (где он = заместитель))
     * @param authorized  авторизованный пользователь
     * @param substitutions список замещений (где авторизованный пользователь = заместитель)
     */
    public AuthorizationData(final User authorized, final List<Substitution> substitutions){
        this(authorized);
        if(!substitutions.isEmpty()) {
            logger.info("User[{}-{}] has {} substitutions", authorized.getId(), authorized.getDescription(), substitutions.size());
            //Выставляем флаг замещения
            this.isSubstitution = true;
            for (Substitution currentSubstitution : substitutions) {
                logger.debug("{}", currentSubstitution);
                //Текущий замещаемый пользователь
                final User currentSubstituted = currentSubstitution.getPerson();
                if (currentSubstituted != null) {
                    //Добавляем текущего замещаемого пользователя в список замещаемых
                    this.substitutedUsers.add(currentSubstituted);
                    this.userIds.add(currentSubstituted.getId());
                    // Прибавляем группы замещаемого в список групп авторизованного
                    final Set<Group> currentSubstitutedGroups  = currentSubstituted.getGroups();
                    if(currentSubstitutedGroups != null && !currentSubstitutedGroups.isEmpty()){
                        for(Group current : currentSubstitutedGroups){
                            groups.add(current);
                            groupIds.add(current.getId());
                        }
                    }
                    // Прибавляем роли замещаемого в список ролей авторизованного
                    final Set<Role> currentSubstitutedRoles = currentSubstituted.getRoles();
                    if(currentSubstitutedRoles != null && !currentSubstitutedRoles.isEmpty()){
                        for(Role current : currentSubstitutedRoles){
                            roles.add(current);
                            roleIds.add(current.getId());
                        }
                    }
                } else {
                    logger.warn("Substitution[{}] has NULL Person!", currentSubstitution.getId());
                }
            }
        }
    }

    public User getAuthorized() {
        return authorized;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public Set<Group> getGroups() {
        return groups;
    }

    public Set<User> getSubstitutedUsers() {
        return substitutedUsers;
    }

    public UserAccessLevel getMaxAccessLevel() {
        return maxAccessLevel;
    }

    public UserAccessLevel getCurrentAccessLevel() {
        return currentAccessLevel;
    }

    public void setCurrentAccessLevel(final UserAccessLevel currentAccessLevel) {
        this.currentAccessLevel = currentAccessLevel;
        this.authorized.setCurrentUserAccessLevel(currentAccessLevel);
    }

    public boolean isSubstitution() {
        return isSubstitution;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Списки идентификаторов
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public Set<Integer> getRoleIds() {
        return roleIds;
    }

    public Set<Integer> getGroupIds() {
        return groupIds;
    }

    public Set<Integer> getUserIds() {
        return userIds;
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Флаги ролей
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public boolean isAdministrator() {
        for (Role role : roles) {
            if (StoredCodes.RoleType.ADMINISTRATOR.equals(role.getRoleType().name())) {
                return true;
            }
        }
        return false;
    }

    public boolean isRecorder() {
        for (Role role : roles) {
            if (StoredCodes.RoleType.RECORDER.equals(role.getRoleType().name())) {
                return true;
            }
        }
        return false;
    }

    public boolean isOfficeManager() {
        for (Role role : roles) {
            if (StoredCodes.RoleType.OFFICE_MANAGER.equals(role.getRoleType().name())) {
                return true;
            }
        }
        return false;
    }

    public boolean isRequestManager() {
        for (Role role : roles) {
            if (StoredCodes.RoleType.REQUEST_MANAGER.equals(role.getRoleType().name())) {
                return true;
            }
        }
        return false;
    }

    public boolean isEmployer() {
        for (Role role : roles) {
            if (StoredCodes.RoleType.EMPLOYER.equals(role.getRoleType().name())) {
                return true;
            }
        }
        return false;
    }

    public boolean isOuter() {
        for (Role role : roles) {
            if (StoredCodes.RoleType.OUTER.equals(role.getRoleType().name())) {
                return true;
            }
        }
        return false;
    }

    public boolean isHr() {
        for (Role role : roles) {
            if (StoredCodes.RoleType.HR.equals(role.getRoleType().name())) {
                return true;
            }
        }
        return false;
    }

    public boolean isFilling() {
        for (Role role : roles) {
            if (StoredCodes.RoleType.FILLING.equals(role.getRoleType().name())) {
                return true;
            }
        }
        return false;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // @Override 's
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AuthorizationData[");
        sb.append("\n\t AuthorizedUser: [").append(authorized.getId()).append("] ").append(authorized.getDescription());
        sb.append("\n\t AccessLevels: ").append("current=").append(currentAccessLevel.getLevel()).append(" ; max=").append(maxAccessLevel.getLevel());
        sb.append("\n\t SubstitutedUsers: ");
        if(isSubstitution){
            sb.append(substitutedUsers.size()).append(" substitutions");
            for(User current : substitutedUsers){
                sb.append("\n\t\t\t[").append(current.getId()).append("] ").append(current.getDescription());
            }
        } else {
            sb.append("NO substitutions");
        }
        sb.append("\n\t Roles: ").append(roles.size());
        for(Role current : roles){
            sb.append("\n\t\t\t").append(current.getRoleType());
        }
        sb.append("\n\t Groups: ").append(groups.size());
        for(Group current : groups){
            sb.append("\n\t\t\t").append(current.getCode());
        }
        sb.append("\n]");
        return sb.toString();
    }

    public String getDefaultPage() {
        return "/component/in/in_documents.xhtml";
    }
}
