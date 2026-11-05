package com.github.rshtishi.bankws.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = "soap")
public class SoapProperties {
	
	private Endpoint endpoint = new Endpoint();
	
	@Data
	public class Endpoint {
		private String host;
		private int port;
		private String name;
		private Ssl ssl = new Ssl();
	}

	@Data
	public static class Ssl {
		private String keyStore;
		private String keyStorePassword;
		private String keyStoreType = "PKCS12";
		private String keyPassword;
		private List<String> protocols = new ArrayList<>(Arrays.asList("TLSv1.3", "TLSv1.2"));
	}

}
