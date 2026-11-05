package com.github.rshtishi.bankws.security;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * PBKDF2-HMAC-SHA256 password hashes in the form {@code pbkdf2-sha256$<iterations>$<salt>$<hash>}.
 * Run {@code main} to generate a value for {@code soap.security.users.<name>.password-hash}.
 */
public final class PasswordHashes {

	public static final int DEFAULT_ITERATIONS = 310_000;

	private static final String PREFIX = "pbkdf2-sha256";
	private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
	private static final int SALT_BYTES = 16;
	private static final int HASH_BITS = 256;
	private static final SecureRandom RANDOM = new SecureRandom();

	private PasswordHashes() {
	}

	public static String hash(char[] password) {
		return hash(password, DEFAULT_ITERATIONS);
	}

	public static String hash(char[] password, int iterations) {
		byte[] salt = new byte[SALT_BYTES];
		RANDOM.nextBytes(salt);
		byte[] hash = pbkdf2(password, salt, iterations, HASH_BITS);
		Base64.Encoder encoder = Base64.getEncoder().withoutPadding();
		return PREFIX + "$" + iterations + "$" + encoder.encodeToString(salt) + "$" + encoder.encodeToString(hash);
	}

	public static boolean matches(char[] password, String encoded) {
		if (password == null || encoded == null) {
			return false;
		}
		String[] parts = encoded.split("\\$");
		if (parts.length != 4 || !PREFIX.equals(parts[0])) {
			return false;
		}
		try {
			int iterations = Integer.parseInt(parts[1]);
			byte[] salt = Base64.getDecoder().decode(parts[2]);
			byte[] expected = Base64.getDecoder().decode(parts[3]);
			if (iterations < 1 || salt.length == 0 || expected.length == 0) {
				return false;
			}
			byte[] actual = pbkdf2(password, salt, iterations, expected.length * 8);
			return MessageDigest.isEqual(expected, actual);
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int bits) {
		PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, bits);
		try {
			return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).getEncoded();
		} catch (GeneralSecurityException e) {
			throw new IllegalStateException("PBKDF2 is not available", e);
		} finally {
			spec.clearPassword();
		}
	}

	public static void main(String[] args) throws IOException {
		char[] password;
		Console console = System.console();
		if (console != null) {
			password = console.readPassword("Password: ");
		} else {
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
			String line = reader.readLine();
			password = line == null ? new char[0] : line.toCharArray();
		}
		if (password == null || password.length == 0) {
			System.err.println("Empty password");
			System.exit(1);
		}
		System.out.println(hash(password));
		Arrays.fill(password, '\0');
	}
}
