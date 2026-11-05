package com.github.rshtishi.bankws.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class PasswordHashesTest {

	private final String secret = UUID.randomUUID().toString();

	@Test
	public void testMatchesOnlyTheOriginalSecret() {
		String encoded = PasswordHashes.hash(secret.toCharArray(), 1_000);

		assertTrue(encoded.startsWith("pbkdf2-sha256$1000$"));
		assertTrue(PasswordHashes.matches(secret.toCharArray(), encoded));
		assertFalse(PasswordHashes.matches((secret + "x").toCharArray(), encoded));
		assertFalse(PasswordHashes.matches(new char[0], encoded));
	}

	@Test
	public void testUsesRandomSalt() {
		assertNotEquals(PasswordHashes.hash(secret.toCharArray(), 1_000), PasswordHashes.hash(secret.toCharArray(), 1_000));
	}

	@Test
	public void testRejectsMalformedHashes() {
		assertFalse(PasswordHashes.matches(secret.toCharArray(), null));
		assertFalse(PasswordHashes.matches(secret.toCharArray(), ""));
		assertFalse(PasswordHashes.matches(secret.toCharArray(), secret));
		assertFalse(PasswordHashes.matches(secret.toCharArray(), "pbkdf2-sha256$0$AAAA$AAAA"));
		assertFalse(PasswordHashes.matches(secret.toCharArray(), "pbkdf2-sha256$x$AAAA$AAAA"));
		assertFalse(PasswordHashes.matches(secret.toCharArray(), "md5$1000$AAAA$AAAA"));
		assertFalse(PasswordHashes.matches(null, PasswordHashes.hash(secret.toCharArray(), 1_000)));
	}
}
