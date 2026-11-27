package com.github.rshtishi.bankws.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jakarta.annotation.PreDestroy;
import jakarta.xml.ws.Endpoint;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import com.github.rshtishi.bankws.service.BankAccountService;

@Configuration
public class SoapPublisher {

	private BankAccountService bankAccountService;
	private SoapProperties soapProperties;
	private final ExecutorService requestExecutor;
	private final Endpoint endpoint;

	@Autowired
	public SoapPublisher(BankAccountService bankAccountService, SoapProperties soapProperties) {
		this.bankAccountService = bankAccountService;
		this.soapProperties = soapProperties;
		final String url = this.createUrl();
		this.requestExecutor = Executors.newCachedThreadPool(this::createRequestThread);
		this.endpoint = Endpoint.create(this.bankAccountService);
		this.endpoint.setExecutor(this.requestExecutor);
		this.endpoint.publish(url);
	}

	@PreDestroy
	public void stop() {
		this.endpoint.stop();
		this.requestExecutor.shutdown();
	}

	// Request threads must see the application class loader so JAX-WS/JAXB can
	// resolve their implementation classes when running from the Spring Boot jar.
	private Thread createRequestThread(Runnable runnable) {
		Thread thread = new Thread(runnable);
		thread.setContextClassLoader(SoapPublisher.class.getClassLoader());
		return thread;
	}

	private String createUrl() {
		StringBuilder builder = new StringBuilder("http://");
		builder.append(this.soapProperties.getEndpoint().getHost());
		builder.append(":");
		builder.append(this.soapProperties.getEndpoint().getPort());
		builder.append("/");
		builder.append(this.soapProperties.getEndpoint().getName());
		return builder.toString();
	}
}
