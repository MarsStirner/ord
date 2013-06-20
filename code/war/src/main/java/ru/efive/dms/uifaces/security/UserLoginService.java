package ru.efive.dms.uifaces.security;

import ru.efive.sql.entity.user.User;
import ru.efive.dms.uifaces.security.impl.AuthenticationUserDetails;

public interface UserLoginService {

    User getLoggedUser();

    AuthenticationUserDetails getLoggedUserDetails();

    boolean login(Long userId);

    boolean login(String login, String password);

    void logout();

    boolean isLoggedIn();

}