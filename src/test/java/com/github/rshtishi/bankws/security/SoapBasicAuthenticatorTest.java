package com.github.rshtishi.bankws.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.github.rshtishi.bankws.config.SoapProperties;
import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

class SoapBasicAuthenticatorTest {

	private static final String USER = "partner";
	private static final String SECRET = UUID.randomUUID().toString();

	private final SoapBasicAuthenticator authenticator = new SoapBasicAuthenticator(security(), 3, Duration.ofMinutes(1));

	private static SoapProperties.Security security() {
		SoapProperties.User user = new SoapProperties.User();
		user.setPasswordHash(PasswordHashes.hash(SECRET.toCharArray(), PasswordHashes.MIN_ITERATIONS));
		SoapProperties.Security security = new SoapProperties.Security();
		security.getUsers().put(USER, user);
		return security;
	}

	private static HttpExchange request(String ip, String user, String password) {
		HttpExchange exchange = mock(HttpExchange.class);
		Headers headers = new Headers();
		if (user != null) {
			String token = Base64.getEncoder().encodeToString((user + ":" + password).getBytes(StandardCharsets.UTF_8));
			headers.add("Authorization", "Basic " + token);
		}
		when(exchange.getRequestHeaders()).thenReturn(headers);
		when(exchange.getResponseHeaders()).thenReturn(new Headers());
		when(exchange.getRemoteAddress()).thenReturn(new InetSocketAddress(ip, 40000));
		return exchange;
	}

	@Test
	public void testValidCredentialsSucceedRepeatedly() {
		for (int i = 0; i < 5; i++) {
			assertTrue(authenticator.authenticate(request("10.0.0.1", USER, SECRET)) instanceof Authenticator.Success);
		}
	}

	@Test
	public void testThrottlesAddressAfterRepeatedFailures() {
		for (int i = 0; i < 3; i++) {
			Authenticator.Result result = authenticator.authenticate(request("10.0.0.2", "nobody", "wrong"));
			assertEquals(401, ((Authenticator.Failure) result).getResponseCode());
		}
		Authenticator.Result throttled = authenticator.authenticate(request("10.0.0.2", USER, SECRET));
		assertEquals(429, ((Authenticator.Failure) throttled).getResponseCode());

		assertTrue(authenticator.authenticate(request("10.0.0.3", USER, SECRET)) instanceof Authenticator.Success);
	}

	@Test
	public void testChallengeWithoutCredentialsIsNotCountedAsFailure() {
		for (int i = 0; i < 5; i++) {
			assertFalse(authenticator.authenticate(request("10.0.0.4", null, null)) instanceof Authenticator.Failure);
		}
		assertTrue(authenticator.authenticate(request("10.0.0.4", USER, SECRET)) instanceof Authenticator.Success);
	}

	@Test
	public void testCachedVerificationDoesNotAcceptOtherPasswords() {
		assertTrue(authenticator.checkCredentials(USER, SECRET));
		assertFalse(authenticator.checkCredentials(USER, SECRET + "x"));
		assertFalse(authenticator.checkCredentials(USER, null));
	}
}
