package com.github.rshtishi.bankws.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.security.Principal;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.rshtishi.bankws.config.SoapProperties;

class TransactionAccessPolicyTest {

	private TransactionAccessPolicy policy;

	@BeforeEach
	public void setUp() {
		SoapProperties properties = new SoapProperties();
		SoapProperties.User partner = new SoapProperties.User();
		partner.setClients(Collections.singletonList("kbalaj"));
		SoapProperties.User admin = new SoapProperties.User();
		admin.setAdmin(true);
		properties.getSecurity().getUsers().put("partner", partner);
		properties.getSecurity().getUsers().put("admin", admin);
		policy = new TransactionAccessPolicy(properties);
	}

	@Test
	public void testPartnerCanOnlyReadItsOwnClients() {
		assertTrue(policy.canReadClient(principal("partner"), "kbalaj"));
		assertFalse(policy.canReadClient(principal("partner"), "rshtishi"));
		assertFalse(policy.canReadClient(principal("partner"), null));
		assertFalse(policy.canReadAllClients(principal("partner")));
	}

	@Test
	public void testAdminCanReadEverything() {
		assertTrue(policy.canReadAllClients(principal("admin")));
		assertTrue(policy.canReadClient(principal("admin"), "rshtishi"));
	}

	@Test
	public void testUnknownOrMissingCallerIsDenied() {
		assertFalse(policy.canReadAllClients(null));
		assertFalse(policy.canReadClient(null, "kbalaj"));
		assertFalse(policy.canReadAllClients(principal("mallory")));
		assertFalse(policy.canReadClient(principal("mallory"), "kbalaj"));
	}

	private static Principal principal(String name) {
		return () -> name;
	}
}
