package com.github.rshtishi.bankws.config;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SoapPublisherTest {

	@Autowired
	private SoapProperties soapProperties;

	@Test
	void testWsdlIsPublished() throws Exception {
		String url = "http://" + soapProperties.getEndpoint().getHost() + ":" + soapProperties.getEndpoint().getPort()
				+ "/" + soapProperties.getEndpoint().getName() + "?wsdl";
		HttpResponse<String> response = HttpClient.newHttpClient()
				.send(HttpRequest.newBuilder(URI.create(url)).GET().build(), HttpResponse.BodyHandlers.ofString());

		assertTrue(response.statusCode() == 200);
		assertTrue(response.body().contains("getTrasactionsForClient"));
	}

}
