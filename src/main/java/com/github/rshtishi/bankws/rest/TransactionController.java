package com.github.rshtishi.bankws.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.rshtishi.bankws.entity.Transaction;
import com.github.rshtishi.bankws.repository.TransactionRepository;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

	@Autowired
	private TransactionRepository transactionRepository;

	@GetMapping
	public List<Transaction> getTransactions(@RequestParam(value = "client", required = false) String client) {
		if (client == null || client.trim().isEmpty()) {
			return transactionRepository.findAll();
		}
		return transactionRepository.findByClient(client);
	}

}
