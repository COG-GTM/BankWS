package com.github.rshtishi.bankws.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.rshtishi.bankws.entity.Transaction;
import com.github.rshtishi.bankws.repository.TransactionRepository;

@Service
public class BankAccountServiceImpl implements BankAccountService {
	
	@Autowired
	private TransactionRepository transactionRepository;
	
	@Override
	public List<Transaction> getTrasactions() {
		List<Transaction> transactions = transactionRepository.findAll();
		return transactions;
	}

	@Override
	public List<Transaction> getTrasactionsForClient(String client) {
		List<Transaction> transactions = transactionRepository.findByClient(client);
		return transactions;
	}

}
