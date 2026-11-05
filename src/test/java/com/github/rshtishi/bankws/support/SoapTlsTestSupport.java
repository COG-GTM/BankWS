package com.github.rshtishi.bankws.support;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Generates a throwaway self-signed key store for the HTTPS-only SOAP endpoint.
 * Test classes that start the Spring context extend this class so they share a
 * single cached context (and a single bound port).
 */
public abstract class SoapTlsTestSupport {

	private static Path keyStore;
	private static String keyStorePassword;

	@DynamicPropertySource
	static void soapSslProperties(DynamicPropertyRegistry registry) {
		ensureKeyStore();
		registry.add("soap.endpoint.ssl.key-store", () -> keyStore.toUri().toString());
		registry.add("soap.endpoint.ssl.key-store-password", () -> keyStorePassword);
	}

	public static synchronized void ensureKeyStore() {
		if (keyStore != null) {
			return;
		}
		try {
			Path dir = Files.createTempDirectory("bankws-tls");
			Path file = dir.resolve("soap-test.p12");
			String password = UUID.randomUUID().toString();
			String keytool = Paths.get(System.getProperty("java.home"), "bin", "keytool").toString();
			Process process = new ProcessBuilder(keytool, "-genkeypair", "-alias", "bankws", "-keyalg", "RSA",
					"-keysize", "2048", "-validity", "1", "-dname", "CN=localhost", "-ext",
					"SAN=dns:localhost,ip:127.0.0.1", "-storetype", "PKCS12", "-keystore", file.toString(),
					"-storepass", password, "-keypass", password).redirectErrorStream(true)
							.redirectOutput(new File(dir.toFile(), "keytool.log")).start();
			if (!process.waitFor(60, TimeUnit.SECONDS) || process.exitValue() != 0) {
				throw new IllegalStateException("keytool failed, see " + dir.resolve("keytool.log"));
			}
			file.toFile().deleteOnExit();
			keyStorePassword = password;
			keyStore = file;
		} catch (IOException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}
	}

	public static Path keyStore() {
		ensureKeyStore();
		return keyStore;
	}

	public static String keyStorePassword() {
		ensureKeyStore();
		return keyStorePassword;
	}

	/** Client SSL context that trusts only the generated test certificate. */
	public static SSLContext clientSslContext() throws IOException, GeneralSecurityException {
		KeyStore trustStore = KeyStore.getInstance("PKCS12");
		try (InputStream in = Files.newInputStream(keyStore())) {
			trustStore.load(in, keyStorePassword().toCharArray());
		}
		TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		tmf.init(trustStore);
		SSLContext sslContext = SSLContext.getInstance("TLS");
		sslContext.init(null, tmf.getTrustManagers(), null);
		return sslContext;
	}
}
