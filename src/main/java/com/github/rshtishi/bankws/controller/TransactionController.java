package com.github.rshtishi.bankws.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.rshtishi.bankws.entity.Transaction;
import com.github.rshtishi.bankws.service.BankAccountService;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

	@Autowired
	private BankAccountService bankAccountService;

	@GetMapping
	public List<Transaction> getTransactions(@RequestParam(name = "client", required = false) String client) {
		if (client != null) {
			return bankAccountService.getTrasactionsForClient(client);
		}
		return bankAccountService.getTrasactions();
	}

}
