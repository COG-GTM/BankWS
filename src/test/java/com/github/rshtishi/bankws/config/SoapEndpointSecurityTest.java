package com.github.rshtishi.bankws.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.github.rshtishi.bankws.security.PasswordHashes;

@SpringBootTest(properties = "soap.endpoint.port=0")
class SoapEndpointSecurityTest {

	private static final String PARTNER = "partner-kbalaj";
	private static final String ADMIN = "bank-admin";
	private static final String PARTNER_SECRET = UUID.randomUUID().toString();
	private static final String ADMIN_SECRET = UUID.randomUUID().toString();

	@DynamicPropertySource
	static void soapUsers(DynamicPropertyRegistry registry) {
		registry.add("soap.security.users." + PARTNER + ".password-hash",
				() -> PasswordHashes.hash(PARTNER_SECRET.toCharArray(), PasswordHashes.MIN_ITERATIONS));
		registry.add("soap.security.users." + PARTNER + ".clients", () -> "kbalaj");
		registry.add("soap.security.users." + ADMIN + ".password-hash",
				() -> PasswordHashes.hash(ADMIN_SECRET.toCharArray(), PasswordHashes.MIN_ITERATIONS));
		registry.add("soap.security.users." + ADMIN + ".admin", () -> "true");
	}

	@Autowired
	private SoapPublisher soapPublisher;

	@Test
	public void testUnauthenticatedRequestsAreRejected() throws IOException {
		assertEquals(401, call(getTransactions(), null).status);
		assertEquals(401, call(getTransactionsForClient("kbalaj"), null).status);
		assertEquals(401, get("?wsdl", null).status);
	}

	@Test
	public void testWrongPasswordOrUnknownUserIsRejected() throws IOException {
		assertEquals(401, call(getTransactions(), basic(ADMIN, PARTNER_SECRET)).status);
		assertEquals(401, call(getTransactions(), basic("mallory", ADMIN_SECRET)).status);
	}

	@Test
	public void testPartnerCanReadItsOwnClient() throws IOException {
		Response response = call(getTransactionsForClient("kbalaj"), basic(PARTNER, PARTNER_SECRET));

		assertEquals(200, response.status);
		assertTrue(response.body.contains("<client>kbalaj</client>"));
		assertFalse(response.body.contains("<client>rshtishi</client>"));
	}

	@Test
	public void testPartnerCannotReadAnotherClient() throws IOException {
		Response response = call(getTransactionsForClient("rshtishi"), basic(PARTNER, PARTNER_SECRET));

		assertEquals(500, response.status);
		assertTrue(response.body.contains("Access denied"));
		assertFalse(response.body.contains("<client>"));
		assertFalse(response.body.contains("at com."));
	}

	@Test
	public void testPartnerCannotReadAllClients() throws IOException {
		Response response = call(getTransactions(), basic(PARTNER, PARTNER_SECRET));

		assertEquals(500, response.status);
		assertTrue(response.body.contains("Access denied"));
		assertFalse(response.body.contains("<client>"));
	}

	@Test
	public void testAdminCanReadAllClients() throws IOException {
		Response response = call(getTransactions(), basic(ADMIN, ADMIN_SECRET));

		assertEquals(200, response.status);
		assertEquals(5, response.body.split("<client>", -1).length - 1);
		assertTrue(response.body.contains("<client>rshtishi</client>"));
		assertTrue(response.body.contains("<client>vtafaj</client>"));
	}

	@Test
	public void testAuthenticatedCallerCanReadWsdl() throws IOException {
		Response response = get("?wsdl", basic(PARTNER, PARTNER_SECRET));

		assertEquals(200, response.status);
		assertTrue(response.body.contains("getTrasactionsForClient"));
	}

	private static String getTransactions() {
		return envelope("<ser:getTrasactions/>");
	}

	private static String getTransactionsForClient(String client) {
		return envelope("<ser:getTrasactionsForClient><arg0>" + client + "</arg0></ser:getTrasactionsForClient>");
	}

	private static String envelope(String body) {
		return "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" "
				+ "xmlns:ser=\"http://service.bankws.rshtishi.github.com/\"><soapenv:Header/><soapenv:Body>"
				+ body + "</soapenv:Body></soapenv:Envelope>";
	}

	private static String basic(String user, String secret) {
		return "Basic " + Base64.getEncoder().encodeToString((user + ":" + secret).getBytes(StandardCharsets.UTF_8));
	}

	private Response call(String soapEnvelope, String authorization) throws IOException {
		HttpURLConnection connection = open("", authorization);
		connection.setRequestMethod("POST");
		connection.setDoOutput(true);
		connection.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
		connection.setRequestProperty("SOAPAction", "\"\"");
		try (OutputStream out = connection.getOutputStream()) {
			out.write(soapEnvelope.getBytes(StandardCharsets.UTF_8));
		}
		return read(connection);
	}

	private Response get(String query, String authorization) throws IOException {
		return read(open(query, authorization));
	}

	private HttpURLConnection open(String query, String authorization) throws IOException {
		HttpURLConnection connection = (HttpURLConnection) new URL(soapPublisher.getUrl() + query).openConnection();
		if (authorization != null) {
			connection.setRequestProperty("Authorization", authorization);
		}
		return connection;
	}

	private static Response read(HttpURLConnection connection) throws IOException {
		int status = connection.getResponseCode();
		InputStream stream = status >= 400 ? connection.getErrorStream() : connection.getInputStream();
		ByteArrayOutputStream body = new ByteArrayOutputStream();
		if (stream != null) {
			try (InputStream in = stream) {
				in.transferTo(body);
			}
		}
		return new Response(status, body.toString(StandardCharsets.UTF_8));
	}

	private static final class Response {
		private final int status;
		private final String body;

		private Response(int status, String body) {
			this.status = status;
			this.body = body;
		}
	}
}
