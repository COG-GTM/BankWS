package com.github.rshtishi.bankws.security;

import java.security.Principal;

import org.springframework.stereotype.Component;

import com.github.rshtishi.bankws.config.SoapProperties;

/**
 * Decides which transactions an authenticated SOAP caller may read.
 * Admin users may read every client; other users only the clients listed for them.
 */
@Component
public class TransactionAccessPolicy {

	private final SoapProperties soapProperties;

	public TransactionAccessPolicy(SoapProperties soapProperties) {
		this.soapProperties = soapProperties;
	}

	public boolean canReadAllClients(Principal caller) {
		SoapProperties.User user = findUser(caller);
		return user != null && user.isAdmin();
	}

	public boolean canReadClient(Principal caller, String client) {
		SoapProperties.User user = findUser(caller);
		if (user == null || client == null) {
			return false;
		}
		return user.isAdmin() || user.getClients().contains(client);
	}

	private SoapProperties.User findUser(Principal caller) {
		if (caller == null || caller.getName() == null) {
			return null;
		}
		return soapProperties.getSecurity().getUsers().get(caller.getName());
	}
}
