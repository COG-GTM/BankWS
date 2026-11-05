package com.github.rshtishi.bankws.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.github.rshtishi.bankws.support.SoapTlsTestSupport;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class SoapPropertiesTest extends SoapTlsTestSupport {
	
	@Autowired
	private  SoapProperties soapProperties;

	@Test
	public void testGetEndpointHost() {
		//setup
		String hostExpected = "localhost";
		String nameExpected = "baws";
		//execute
		String hostActual = soapProperties.getEndpoint().getHost();
		//verify
		assertEquals(hostExpected, hostActual);
	}
	
	@Test
	public void testGetEndpointPort() {
		//setup
		int portExpected = 8888;
		//execute
		int portActual = soapProperties.getEndpoint().getPort();
		//verify
		assertEquals(portExpected, portActual);
	}
	
	@Test
	public void testGetEndpointName() {
		//setup
		String nameExpected = "baws";
		//execute
		String nameActual = soapProperties.getEndpoint().getName();
		//verify
		assertEquals(nameExpected, nameActual);
	}
	

}
