package com.github.rshtishi.bankws.security;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.github.rshtishi.bankws.config.SoapProperties;
import com.sun.net.httpserver.BasicAuthenticator;
import com.sun.net.httpserver.HttpExchange;

/**
 * HTTP Basic authentication for the SOAP endpoint against {@code soap.security.users}.
 * Remote addresses with too many recent failures get 429 without running PBKDF2, and
 * recently verified credentials are remembered (as a keyed HMAC) so valid callers skip PBKDF2.
 */
public class SoapBasicAuthenticator extends BasicAuthenticator {

	private static final String UNKNOWN_USER_HASH = PasswordHashes.hash("unknown-user".toCharArray());
	private static final int DEFAULT_MAX_FAILURES = 10;
	private static final Duration DEFAULT_FAILURE_WINDOW = Duration.ofMinutes(1);
	private static final Duration VERIFIED_TTL = Duration.ofMinutes(5);
	private static final int MAX_TRACKED_ADDRESSES = 10_000;

	private final Map<String, SoapProperties.User> users;
	private final int maxFailures;
	private final long failureWindowNanos;
	private final Map<InetAddress, Failures> failures = new ConcurrentHashMap<>();
	private final Map<String, Verified> verified = new ConcurrentHashMap<>();
	private final byte[] cacheKey = new byte[32];

	public SoapBasicAuthenticator(SoapProperties.Security security) {
		this(security, DEFAULT_MAX_FAILURES, DEFAULT_FAILURE_WINDOW);
	}

	SoapBasicAuthenticator(SoapProperties.Security security, int maxFailures, Duration failureWindow) {
		super(security.getRealm());
		this.users = security.getUsers();
		this.maxFailures = maxFailures;
		this.failureWindowNanos = failureWindow.toNanos();
		new SecureRandom().nextBytes(this.cacheKey);
	}

	@Override
	public Result authenticate(HttpExchange exchange) {
		InetAddress remote = remoteAddress(exchange);
		long now = System.nanoTime();
		if (remote != null && isThrottled(remote, now)) {
			return new Failure(429);
		}
		Result result = super.authenticate(exchange);
		boolean attempted = exchange.getRequestHeaders().getFirst("Authorization") != null;
		// Successes do not reset the counter, so valid logins cannot mask guesses against other users.
		if (remote != null && attempted && !(result instanceof Success)) {
			recordFailure(remote, now);
		}
		return result;
	}

	@Override
	public boolean checkCredentials(String username, String password) {
		SoapProperties.User user = username == null ? null : users.get(username);
		if (user == null || user.getPasswordHash() == null || password == null) {
			// Same work as a real check so response time does not reveal valid usernames.
			PasswordHashes.matches(String.valueOf(password).toCharArray(), UNKNOWN_USER_HASH);
			return false;
		}
		byte[] fingerprint = fingerprint(user.getPasswordHash(), password);
		long now = System.nanoTime();
		Verified cached = verified.get(username);
		if (cached != null && now - cached.at < VERIFIED_TTL.toNanos()
				&& MessageDigest.isEqual(cached.fingerprint, fingerprint)) {
			return true;
		}
		if (PasswordHashes.matches(password.toCharArray(), user.getPasswordHash())) {
			verified.put(username, new Verified(fingerprint, now));
			return true;
		}
		return false;
	}

	private boolean isThrottled(InetAddress remote, long now) {
		Failures f = failures.get(remote);
		return f != null && now - f.windowStart < failureWindowNanos && f.count >= maxFailures;
	}

	private void recordFailure(InetAddress remote, long now) {
		if (failures.size() >= MAX_TRACKED_ADDRESSES) {
			failures.values().removeIf(f -> now - f.windowStart >= failureWindowNanos);
		}
		failures.compute(remote, (k, f) -> f == null || now - f.windowStart >= failureWindowNanos
				? new Failures(now, 1)
				: new Failures(f.windowStart, f.count + 1));
	}

	private static InetAddress remoteAddress(HttpExchange exchange) {
		InetSocketAddress address = exchange.getRemoteAddress();
		return address == null ? null : address.getAddress();
	}

	private byte[] fingerprint(String passwordHash, String password) {
		try {
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(new SecretKeySpec(cacheKey, "HmacSHA256"));
			mac.update(passwordHash.getBytes(StandardCharsets.UTF_8));
			mac.update((byte) 0);
			return mac.doFinal(password.getBytes(StandardCharsets.UTF_8));
		} catch (GeneralSecurityException e) {
			throw new IllegalStateException("HmacSHA256 is not available", e);
		}
	}

	private static final class Failures {
		private final long windowStart;
		private final int count;

		private Failures(long windowStart, int count) {
			this.windowStart = windowStart;
			this.count = count;
		}
	}

	private static final class Verified {
		private final byte[] fingerprint;
		private final long at;

		private Verified(byte[] fingerprint, long at) {
			this.fingerprint = fingerprint;
			this.at = at;
		}
	}
}
