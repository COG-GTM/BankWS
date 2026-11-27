package com.github.rshtishi.bankws.config;

import java.util.concurrent.Executors;

import jakarta.xml.ws.Endpoint;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import com.github.rshtishi.bankws.service.BankAccountService;

@Configuration
public class SoapPublisher {

	private BankAccountService bankAccountService;
	private SoapProperties soapProperties;

	@Autowired
	public SoapPublisher(BankAccountService bankAccountService, SoapProperties soapProperties) {
		this.bankAccountService = bankAccountService;
		this.soapProperties = soapProperties;
		final String url = this.createUrl();
		Endpoint endpoint = Endpoint.create(this.bankAccountService);
		endpoint.setExecutor(Executors.newCachedThreadPool(this::createRequestThread));
		endpoint.publish(url);
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
