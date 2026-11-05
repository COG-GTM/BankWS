package com.github.rshtishi.bankws.config;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

import javax.annotation.PreDestroy;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.xml.ws.Endpoint;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

import com.github.rshtishi.bankws.service.BankAccountService;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;

/**
 * Publishes the SOAP endpoint over HTTPS only. Startup fails when no key store
 * is configured instead of falling back to cleartext HTTP.
 */
@Configuration
public class SoapPublisher {

	private BankAccountService bankAccountService;
	private SoapProperties soapProperties;
	private HttpsServer server;
	private Endpoint endpoint;

	@Autowired
	public SoapPublisher(BankAccountService bankAccountService, SoapProperties soapProperties) {
		this.bankAccountService = bankAccountService;
		this.soapProperties = soapProperties;
		this.server = this.createHttpsServer();
		HttpContext context = this.server.createContext(this.createPath());
		this.endpoint = Endpoint.create(this.bankAccountService);
		this.endpoint.publish(context);
		this.server.start();
	}

	@PreDestroy
	public void stop() {
		if (this.endpoint != null) {
			this.endpoint.stop();
		}
		if (this.server != null) {
			this.server.stop(0);
		}
	}

	private String createPath() {
		return "/" + this.soapProperties.getEndpoint().getName();
	}

	private HttpsServer createHttpsServer() {
		SoapProperties.Endpoint endpointProperties = this.soapProperties.getEndpoint();
		SSLContext sslContext = this.createSslContext(endpointProperties.getSsl());
		String[] protocols = endpointProperties.getSsl().getProtocols().toArray(new String[0]);
		try {
			HttpsServer httpsServer = HttpsServer.create(
					new InetSocketAddress(endpointProperties.getHost(), endpointProperties.getPort()), 0);
			httpsServer.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
				@Override
				public void configure(HttpsParameters params) {
					SSLParameters sslParameters = getSSLContext().getDefaultSSLParameters();
					sslParameters.setProtocols(protocols);
					params.setSSLParameters(sslParameters);
				}
			});
			return httpsServer;
		} catch (IOException e) {
			throw new IllegalStateException("Unable to start HTTPS server for SOAP endpoint on "
					+ endpointProperties.getHost() + ":" + endpointProperties.getPort(), e);
		}
	}

	private SSLContext createSslContext(SoapProperties.Ssl ssl) {
		if (!StringUtils.hasText(ssl.getKeyStore()) || !StringUtils.hasText(ssl.getKeyStorePassword())) {
			throw new IllegalStateException("soap.endpoint.ssl.key-store and soap.endpoint.ssl.key-store-password "
					+ "must be set: the SOAP endpoint is only served over HTTPS");
		}
		if (ssl.getProtocols() == null || ssl.getProtocols().isEmpty()) {
			throw new IllegalStateException("soap.endpoint.ssl.protocols must not be empty");
		}
		char[] storePassword = ssl.getKeyStorePassword().toCharArray();
		char[] keyPassword = StringUtils.hasText(ssl.getKeyPassword()) ? ssl.getKeyPassword().toCharArray()
				: storePassword;
		try (InputStream in = ResourceUtils.getURL(ssl.getKeyStore()).openStream()) {
			KeyStore keyStore = KeyStore.getInstance(ssl.getKeyStoreType());
			keyStore.load(in, storePassword);
			KeyManagerFactory keyManagerFactory = KeyManagerFactory
					.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			keyManagerFactory.init(keyStore, keyPassword);
			SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(keyManagerFactory.getKeyManagers(), null, null);
			return sslContext;
		} catch (IOException | GeneralSecurityException e) {
			throw new IllegalStateException("Unable to load SOAP endpoint key store " + ssl.getKeyStore(), e);
		}
	}
}
