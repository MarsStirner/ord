package ru.efive.dms.uifaces.security.impl;

import java.util.Collection;
import java.util.HashSet;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import ru.entity.model.user.User;

public class AuthenticationUserDetails implements UserDetails {

    public AuthenticationUserDetails(User user) {
        id = user.getId();
        this.login = user.getLogin();
        this.passwordHash = user.getPassword();
        this.enabled = !user.isDeleted();
        //this.grantedAuthorities.addAll(user.getAuthorities());
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    @Override
    public Collection<GrantedAuthority> getAuthorities() {
        return grantedAuthorities;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return login;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    public String getLogin() {
        return login;
    }


    private int id;
    private final String login;
    private final String passwordHash;
    private final boolean enabled;
    private HashSet<GrantedAuthority> grantedAuthorities = new HashSet<GrantedAuthority>();


    private static final long serialVersionUID = -7882237543371730144L;
}