package com.github.rshtishi.bankws.service;

import java.security.Principal;
import java.util.List;

import javax.annotation.Resource;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.SOAPFaultException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.rshtishi.bankws.entity.Transaction;
import com.github.rshtishi.bankws.repository.TransactionRepository;
import com.github.rshtishi.bankws.security.TransactionAccessPolicy;

@Service
@WebService(serviceName = "bank-account", endpointInterface = "com.github.rshtishi.bankws.service.BankAccountService")
public class BankAccountServiceImpl implements BankAccountService {
	
	@Autowired
	private TransactionRepository transactionRepository;

	@Autowired
	private TransactionAccessPolicy accessPolicy;

	@Resource
	private WebServiceContext webServiceContext;
	
	@WebMethod(operationName="getTransactions")
	@Override
	public List<Transaction> getTrasactions() {
		if (!accessPolicy.canReadAllClients(currentCaller())) {
			throw accessDenied();
		}
		List<Transaction> transactions = transactionRepository.findAll();
		return transactions;
	}

	@WebMethod(operationName="getTransactionsForClient")
	@Override
	public List<Transaction> getTrasactionsForClient(@WebParam(name="client") String client) {
		if (!accessPolicy.canReadClient(currentCaller(), client)) {
			throw accessDenied();
		}
		List<Transaction> transactions = transactionRepository.findByClient(client);
		return transactions;
	}

	private Principal currentCaller() {
		if (webServiceContext == null) {
			return null;
		}
		try {
			return webServiceContext.getUserPrincipal();
		} catch (IllegalStateException e) {
			// Not inside a SOAP request.
			return null;
		}
	}

	private static SOAPFaultException accessDenied() {
		try {
			SOAPFault fault = SOAPFactory.newInstance()
					.createFault("Access denied", new QName(SOAPConstants.URI_NS_SOAP_ENVELOPE, "Client"));
			return new SOAPFaultException(fault);
		} catch (SOAPException e) {
			throw new WebServiceException("Access denied");
		}
	}

}
