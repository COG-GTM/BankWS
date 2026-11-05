package com.github.rshtishi.bankws.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.github.rshtishi.bankws.entity.Transaction;
import com.github.rshtishi.bankws.support.SoapTlsTestSupport;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class BankAccountServiceImplTest extends SoapTlsTestSupport {

	@Autowired
	private BankAccountService bankAccountService;

	@Test
	public void testGetTrasactions() {
		// setup
		int sizeExpected = 5;
		// execute
		List<Transaction> transactions = bankAccountService.getTrasactions();
		// verify
		assertEquals(sizeExpected, transactions.size());
	}

	@Test
	public void testGetTrasactionsForClient() {
		// setup
		int idExpected = 1;
		String clientExpected = "rshtishi";
		double amountExpected = 500.00;
		// execute
		List<Transaction> transactions = bankAccountService.getTrasactionsForClient(clientExpected);
		// verify
		assertEquals(idExpected, transactions.get(0).getId());
		assertEquals(clientExpected, transactions.get(0).getClient());
		assertEquals(amountExpected, transactions.get(0).getAmount());
	}

}
