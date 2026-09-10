package com.github.rshtishi.bankws.service;

import java.util.List;

import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebService;

import com.github.rshtishi.bankws.entity.Transaction;

@WebService
public interface BankAccountService {
	
	@WebMethod
	public List<Transaction> getTrasactions();
	
	@WebMethod
	public List<Transaction> getTrasactionsForClient(@WebParam String client);

}
