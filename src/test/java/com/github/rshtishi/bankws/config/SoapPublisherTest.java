package com.github.rshtishi.bankws.config;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.net.ServerSocket;

import org.junit.jupiter.api.Test;

import com.github.rshtishi.bankws.service.BankAccountService;
import com.github.rshtishi.bankws.service.BankAccountServiceImpl;
import com.github.rshtishi.bankws.support.SoapTlsTestSupport;

class SoapPublisherTest {

	@Test
	void startupFailsWithoutKeyStore() {
		SoapProperties properties = properties(0);
		IllegalStateException e = assertThrows(IllegalStateException.class,
				() -> new SoapPublisher(mock(BankAccountService.class), properties));
		assertTrue(e.getMessage().contains("only served over HTTPS"), e.getMessage());
	}

	@Test
	void startupFailsWithWrongKeyStorePassword() {
		SoapProperties properties = properties(0);
		properties.getEndpoint().getSsl().setKeyStore(SoapTlsTestSupport.keyStore().toUri().toString());
		properties.getEndpoint().getSsl().setKeyStorePassword("not-the-password");
		assertThrows(IllegalStateException.class,
				() -> new SoapPublisher(mock(BankAccountService.class), properties));
	}

	@Test
	void startsAndStopsWithKeyStore() throws Exception {
		int port;
		try (ServerSocket socket = new ServerSocket(0)) {
			port = socket.getLocalPort();
		}
		SoapProperties properties = properties(port);
		properties.getEndpoint().getSsl().setKeyStore(SoapTlsTestSupport.keyStore().toUri().toString());
		properties.getEndpoint().getSsl().setKeyStorePassword(SoapTlsTestSupport.keyStorePassword());
		SoapPublisher publisher = new SoapPublisher(new BankAccountServiceImpl(), properties);
		publisher.stop();
	}

	private static SoapProperties properties(int port) {
		SoapProperties properties = new SoapProperties();
		properties.getEndpoint().setHost("localhost");
		properties.getEndpoint().setPort(port);
		properties.getEndpoint().setName("baws");
		return properties;
	}
}
