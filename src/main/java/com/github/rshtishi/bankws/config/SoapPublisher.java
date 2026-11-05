package com.github.rshtishi.bankws.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PreDestroy;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.xml.ws.Endpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import com.github.rshtishi.bankws.security.SoapBasicAuthenticator;
import com.github.rshtishi.bankws.service.BankAccountService;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;

@Configuration
public class SoapPublisher {

	private static final Logger log = LoggerFactory.getLogger(SoapPublisher.class);
	private static final int WORKER_THREADS = 16;

	private BankAccountService bankAccountService;
	private SoapProperties soapProperties;
	private final HttpServer server;
	private final ExecutorService executor;
	private final Endpoint endpoint;

	@Autowired
	public SoapPublisher(BankAccountService bankAccountService, SoapProperties soapProperties) {
		this.bankAccountService = bankAccountService;
		this.soapProperties = soapProperties;
		SoapProperties.Endpoint config = this.soapProperties.getEndpoint();
		boolean tls = StringUtils.hasText(config.getSsl().getKeyStore());
		this.verifyTransportIsSafe(config, tls);
		if (this.soapProperties.getSecurity().getUsers().isEmpty()) {
			log.warn("No soap.security.users configured: every SOAP request will be rejected");
		}

		HttpServer httpServer = this.createServer(config, tls);
		ExecutorService workers = null;
		Endpoint published = null;
		try {
			workers = Executors.newFixedThreadPool(WORKER_THREADS);
			httpServer.setExecutor(workers);
			HttpContext context = httpServer.createContext("/" + config.getName());
			context.setAuthenticator(new SoapBasicAuthenticator(this.soapProperties.getSecurity()));
			published = Endpoint.create(this.bankAccountService);
			published.publish(context);
			httpServer.start();
		} catch (RuntimeException | Error e) {
			if (published != null) {
				published.stop();
			}
			httpServer.stop(0);
			if (workers != null) {
				workers.shutdownNow();
			}
			throw e;
		}
		this.server = httpServer;
		this.executor = workers;
		this.endpoint = published;
		log.info("SOAP endpoint published at {}", this.getUrl());
	}

	public InetSocketAddress getAddress() {
		return this.server.getAddress();
	}

	public String getUrl() {
		StringBuilder builder = new StringBuilder(this.server instanceof HttpsServer ? "https://" : "http://");
		String host = this.soapProperties.getEndpoint().getHost();
		boolean ipv6Literal = host.indexOf(':') >= 0 && !host.startsWith("[");
		builder.append(ipv6Literal ? "[" + host + "]" : host);
		builder.append(":");
		builder.append(this.server.getAddress().getPort());
		builder.append("/");
		builder.append(this.soapProperties.getEndpoint().getName());
		return builder.toString();
	}

	@PreDestroy
	public void stop() {
		this.endpoint.stop();
		this.server.stop(0);
		this.executor.shutdownNow();
	}

	private void verifyTransportIsSafe(SoapProperties.Endpoint config, boolean tls) {
		if (tls || config.isAllowInsecureHttp() || isLoopback(config.getHost())) {
			return;
		}
		throw new IllegalStateException("Refusing to publish the SOAP endpoint on non-loopback host '"
				+ config.getHost() + "' over plain HTTP: configure soap.endpoint.ssl.key-store "
				+ "(or set soap.endpoint.allow-insecure-http=true behind a TLS-terminating proxy)");
	}

	private static boolean isLoopback(String host) {
		try {
			return InetAddress.getByName(host).isLoopbackAddress();
		} catch (IOException e) {
			return false;
		}
	}

	private HttpServer createServer(SoapProperties.Endpoint config, boolean tls) {
		InetSocketAddress address = new InetSocketAddress(config.getHost(), config.getPort());
		try {
			if (!tls) {
				return HttpServer.create(address, 0);
			}
			HttpsServer httpsServer = HttpsServer.create(address, 0);
			httpsServer.setHttpsConfigurator(new HttpsConfigurator(createSslContext(config.getSsl())));
			return httpsServer;
		} catch (IOException e) {
			throw new UncheckedIOException("Cannot bind SOAP endpoint to " + address, e);
		}
	}

	private static SSLContext createSslContext(SoapProperties.Ssl ssl) throws IOException {
		char[] password = ssl.getKeyStorePassword() == null ? new char[0] : ssl.getKeyStorePassword().toCharArray();
		try (InputStream in = Files.newInputStream(Paths.get(ssl.getKeyStore()))) {
			KeyStore keyStore = KeyStore.getInstance(ssl.getKeyStoreType());
			keyStore.load(in, password);
			KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmf.init(keyStore, password);
			SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(kmf.getKeyManagers(), null, null);
			return sslContext;
		} catch (GeneralSecurityException e) {
			throw new IllegalStateException("Cannot load SOAP TLS key store " + ssl.getKeyStore(), e);
		}
	}
}
