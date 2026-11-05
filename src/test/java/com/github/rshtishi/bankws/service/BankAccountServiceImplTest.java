package com.github.rshtishi.bankws.service;

import static org.junit.jupiter.api.Assertions.assertThrows;

import javax.xml.ws.soap.SOAPFaultException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class BankAccountServiceImplTest {

	@Autowired
	private BankAccountService bankAccountService;

	@Test
	public void testGetTrasactionsWithoutAuthenticatedCallerIsDenied() {
		assertThrows(SOAPFaultException.class, () -> bankAccountService.getTrasactions());
	}

	@Test
	public void testGetTrasactionsForClientWithoutAuthenticatedCallerIsDenied() {
		assertThrows(SOAPFaultException.class, () -> bankAccountService.getTrasactionsForClient("rshtishi"));
	}

}
