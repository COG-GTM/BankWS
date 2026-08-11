package com.github.rshtishi.bankws.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.rshtishi.bankws.entity.Transaction;
import com.github.rshtishi.bankws.service.BankAccountService;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

	@Autowired
	private BankAccountService bankAccountService;

	@GetMapping
	public List<Transaction> getTrasactions() {
		return bankAccountService.getTrasactions();
	}

	@GetMapping("/client/{client}")
	public List<Transaction> getTrasactionsForClient(@PathVariable String client) {
		return bankAccountService.getTrasactionsForClient(client);
	}

}
