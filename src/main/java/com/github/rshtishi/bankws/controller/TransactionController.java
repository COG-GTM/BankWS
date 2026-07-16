package com.github.rshtishi.bankws.controller;

import java.util.List;

import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.rshtishi.bankws.entity.Transaction;
import com.github.rshtishi.bankws.repository.TransactionRepository;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

	private final TransactionRepository transactionRepository;

	public TransactionController(TransactionRepository transactionRepository) {
		this.transactionRepository = transactionRepository;
	}

	@GetMapping
	public List<Transaction> getTransactions(@RequestParam(required = false) String client) {
		if (!StringUtils.hasText(client)) {
			return transactionRepository.findAll();
		}
		return transactionRepository.findByClient(client);
	}

}
