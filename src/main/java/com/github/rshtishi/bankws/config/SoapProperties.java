package com.github.rshtishi.bankws.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = "soap")
public class SoapProperties {
	
	private Endpoint endpoint = new Endpoint();
	private Security security = new Security();
	
	@Data
	public class Endpoint {
		private String host;
		private int port;
		private String name;
		private boolean allowInsecureHttp;
		private Ssl ssl = new Ssl();
	}

	@Data
	public static class Ssl {
		private String keyStore;
		private String keyStorePassword;
		private String keyStoreType = "PKCS12";
	}

	@Data
	public static class Security {
		private String realm = "BankWS";
		private Map<String, User> users = new LinkedHashMap<>();
	}

	@Data
	public static class User {
		private String passwordHash;
		private List<String> clients = new ArrayList<>();
		private boolean admin;
	}

}
