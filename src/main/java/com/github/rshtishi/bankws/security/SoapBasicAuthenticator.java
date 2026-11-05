package com.github.rshtishi.bankws.security;

import java.util.Map;

import com.github.rshtishi.bankws.config.SoapProperties;
import com.sun.net.httpserver.BasicAuthenticator;

/**
 * HTTP Basic authentication for the SOAP endpoint against {@code soap.security.users}.
 */
public class SoapBasicAuthenticator extends BasicAuthenticator {

	private static final String UNKNOWN_USER_HASH = PasswordHashes.hash("unknown-user".toCharArray());

	private final Map<String, SoapProperties.User> users;

	public SoapBasicAuthenticator(SoapProperties.Security security) {
		super(security.getRealm());
		this.users = security.getUsers();
	}

	@Override
	public boolean checkCredentials(String username, String password) {
		SoapProperties.User user = username == null ? null : users.get(username);
		if (user == null || user.getPasswordHash() == null) {
			// Same work as a real check so response time does not reveal valid usernames.
			PasswordHashes.matches(String.valueOf(password).toCharArray(), UNKNOWN_USER_HASH);
			return false;
		}
		return PasswordHashes.matches(password == null ? null : password.toCharArray(), user.getPasswordHash());
	}
}
