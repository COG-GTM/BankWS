package com.github.rshtishi.bankws.service;

import java.util.List;

import com.github.rshtishi.bankws.entity.Transaction;

public interface BankAccountService {

	public List<Transaction> getTrasactions();

	public List<Transaction> getTrasactionsForClient(String client);

}
