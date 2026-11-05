package com.github.rshtishi.bankws.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.HttpsURLConnection;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.github.rshtishi.bankws.support.SoapTlsTestSupport;

@SpringBootTest
class SoapPublisherHttpsTest extends SoapTlsTestSupport {

	private static final String HTTPS_URL = "https://localhost:8888/baws";
	private static final String HTTP_URL = "http://localhost:8888/baws";

	@Test
	void wsdlIsServedOverHttps() throws Exception {
		HttpsURLConnection connection = openHttps(HTTPS_URL + "?wsdl");
		assertEquals(200, connection.getResponseCode());
		String wsdl = read(connection.getInputStream());
		assertTrue(wsdl.contains("BankAccountService"), wsdl);
		assertTrue(wsdl.contains("location=\"https://localhost:8888/baws\""), wsdl);
	}

	@Test
	void soapCallIsServedOverHttps() throws Exception {
		String envelope = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" "
				+ "xmlns:ser=\"http://service.bankws.rshtishi.github.com/\"><soapenv:Body>"
				+ "<ser:getTrasactionsForClient><arg0>rshtishi</arg0></ser:getTrasactionsForClient>"
				+ "</soapenv:Body></soapenv:Envelope>";
		HttpsURLConnection connection = openHttps(HTTPS_URL);
		connection.setRequestMethod("POST");
		connection.setDoOutput(true);
		connection.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
		try (OutputStream out = connection.getOutputStream()) {
			out.write(envelope.getBytes(StandardCharsets.UTF_8));
		}
		assertEquals(200, connection.getResponseCode());
		String response = read(connection.getInputStream());
		assertTrue(response.contains("getTrasactionsForClientResponse"), response);
		assertTrue(response.contains("rshtishi"), response);
	}

	@Test
	void plainHttpIsRejected() throws Exception {
		HttpURLConnection connection = (HttpURLConnection) new URL(HTTP_URL + "?wsdl").openConnection();
		connection.setConnectTimeout(5000);
		connection.setReadTimeout(5000);
		assertThrows(IOException.class, connection::getResponseCode);
	}

	private static HttpsURLConnection openHttps(String url) throws Exception {
		HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();
		connection.setSSLSocketFactory(clientSslContext().getSocketFactory());
		connection.setConnectTimeout(5000);
		connection.setReadTimeout(5000);
		return connection;
	}

	private static String read(InputStream in) throws IOException {
		try (InputStream input = in) {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte[] buffer = new byte[4096];
			int n;
			while ((n = input.read(buffer)) != -1) {
				out.write(buffer, 0, n);
			}
			return out.toString(StandardCharsets.UTF_8.name());
		}
	}
}
