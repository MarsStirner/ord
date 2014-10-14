package ru.efive.dms.uifaces.security.impl;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.RememberMeAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

//import ru.efive.sql.dao.user.UserDAOHibernate;
import ru.efive.sql.dao.user.UserDAOHibernate;
import ru.entity.model.user.User;
import ru.efive.dms.uifaces.security.UserLoginService;

public class KerberosUserLoginService implements UserLoginService {

    public KerberosUserLoginService(UserDAOHibernate userRepository, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
    }

    @Override
    public boolean login(String login, String password) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(login, password));
        boolean isAuthenticated = isAuthenticated(authentication);
        if (isAuthenticated) {
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        return isAuthenticated;
    }

    @Override
    public boolean login(Long userId) {
        boolean isLoginSuccesfull = false;
        User user = userRepository.get(userId);
        if (user != null) {
            AuthenticationUserDetails userDetails = new AuthenticationUserDetails(user);
            final RememberMeAuthenticationToken rememberMeAuthenticationToken = new RememberMeAuthenticationToken(
                    internalHashKeyForAutomaticLoginAfterRegistration, userDetails, null);
            rememberMeAuthenticationToken.setAuthenticated(true);
            SecurityContextHolder.getContext().setAuthentication(rememberMeAuthenticationToken);
            isLoginSuccesfull = true;
        }
        return isLoginSuccesfull;
    }

    @Override
    public void logout() {
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Override
    public boolean isLoggedIn() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return isAuthenticated(authentication);
    }

    @Override
    public AuthenticationUserDetails getLoggedUserDetails() {
        AuthenticationUserDetails loggedUserDetails = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (isAuthenticated(authentication)) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof AuthenticationUserDetails) {
                loggedUserDetails = ((AuthenticationUserDetails) principal);
            } else {
                System.out.println("Expected class of authentication principal is AuthenticationUserDetails. Given: " + principal.getClass());
                return null;
            }
        }
        return loggedUserDetails;
    }

    private boolean isAuthenticated(Authentication authentication) {
        return authentication != null && !(authentication instanceof AnonymousAuthenticationToken) && authentication.isAuthenticated();
    }

    @Override
    public User getLoggedUser() {
        User loggedUser = null;
        AuthenticationUserDetails userDetails = getLoggedUserDetails();
        if (userDetails != null) {
            loggedUser = userRepository.getByLogin(userDetails.getLogin());
        }
        return loggedUser;
    }


    private UserDAOHibernate userRepository;
    private AuthenticationManager authenticationManager;
    private static final String internalHashKeyForAutomaticLoginAfterRegistration = "magicInternalHashKeyForAutomaticLoginAfterRegistration";
}