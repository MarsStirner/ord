package ru.efive.dms.uifaces.security.impl;

import org.springframework.dao.DataAccessException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import ru.efive.sql.dao.user.UserDAOHibernate;
import ru.efive.sql.entity.user.User;

public class AuthenticationUserDetailsGetter implements UserDetailsService {
	
	protected AuthenticationUserDetailsGetter() {
	
	}
	
	public AuthenticationUserDetailsGetter(UserDAOHibernate userRepository) {
		this.userRepository = userRepository;
	}
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException {
		System.out.println("Username: " + username);
		User user = userRepository.getByLogin(username);
		throwExceptionIfNotFound(user, username);
		return new AuthenticationUserDetails(user);
	}
	
	private void throwExceptionIfNotFound(User user, String login) {
		if (user == null) {
			throw new UsernameNotFoundException("User with login " + login + "  has not been found.");
		}
	}
	
	
	private UserDAOHibernate userRepository;
}