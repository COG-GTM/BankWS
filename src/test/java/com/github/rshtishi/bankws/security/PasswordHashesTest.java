package com.github.rshtishi.bankws.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class PasswordHashesTest {

	private final String secret = UUID.randomUUID().toString();

	@Test
	public void testMatchesOnlyTheOriginalSecret() {
		String encoded = PasswordHashes.hash(secret.toCharArray(), PasswordHashes.MIN_ITERATIONS);

		assertTrue(encoded.startsWith("pbkdf2-sha256$" + PasswordHashes.MIN_ITERATIONS + "$"));
		assertTrue(PasswordHashes.matches(secret.toCharArray(), encoded));
		assertFalse(PasswordHashes.matches((secret + "x").toCharArray(), encoded));
		assertFalse(PasswordHashes.matches(new char[0], encoded));
	}

	@Test
	public void testUsesRandomSalt() {
		assertNotEquals(PasswordHashes.hash(secret.toCharArray(), PasswordHashes.MIN_ITERATIONS), PasswordHashes.hash(secret.toCharArray(), PasswordHashes.MIN_ITERATIONS));
	}

	@Test
	public void testRejectsMalformedHashes() {
		assertFalse(PasswordHashes.matches(secret.toCharArray(), null));
		assertFalse(PasswordHashes.matches(secret.toCharArray(), ""));
		assertFalse(PasswordHashes.matches(secret.toCharArray(), secret));
		assertFalse(PasswordHashes.matches(secret.toCharArray(), "pbkdf2-sha256$0$AAAA$AAAA"));
		assertFalse(PasswordHashes.matches(secret.toCharArray(), "pbkdf2-sha256$x$AAAA$AAAA"));
		assertFalse(PasswordHashes.matches(secret.toCharArray(), "md5$1000$AAAA$AAAA"));
		assertFalse(PasswordHashes.matches(null, PasswordHashes.hash(secret.toCharArray(), PasswordHashes.MIN_ITERATIONS)));
	}

	@Test
	public void testRejectsWeakParameters() {
		assertThrows(IllegalArgumentException.class, () -> PasswordHashes.hash(secret.toCharArray(), 1));
		String[] parts = PasswordHashes.hash(secret.toCharArray(), PasswordHashes.MIN_ITERATIONS).split("\\$");
		String lowIterations = parts[0] + "$1$" + parts[2] + "$" + parts[3];
		String truncatedHash = parts[0] + "$" + parts[1] + "$" + parts[2] + "$" + parts[3].substring(0, 8);
		String shortSalt = parts[0] + "$" + parts[1] + "$AAAA$" + parts[3];
		assertFalse(PasswordHashes.matches(secret.toCharArray(), lowIterations));
		assertFalse(PasswordHashes.matches(secret.toCharArray(), truncatedHash));
		assertFalse(PasswordHashes.matches(secret.toCharArray(), shortSalt));
	}
}
