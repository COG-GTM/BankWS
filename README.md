# BankWS

*BankWS* is spring-boot application that exposes a soap web service endpoint so third parties on different platforms can invoke
them via SOAP.

## Bussiness Case

A bank has many third-party application that need to access the user transaction history. The user transaction history is stored 
in the core system of the bank. The third-party applications are written with different technologies stack. We need to provide a way 
to expose the information from the core system of the bank in safe manner to all third-party application who need this information.

## Technology

BankWS is using the following technologies:
- Java [version: 11] (the language used to write the application)
- Maven [version:3.6] (the tool for managing dependencies and building the project) 
- Lombok [version:1.18.12] (the java library for removing boiler plate code from pojos)
- Spring-Boot [version:2.3.0.RELEASE] (the framework for creating spring application that just run)
- Spring-Data [version:2.3.0.RELEASE] (the framework for interacting with database)
- H2 Database [version:1.4.2] (the database we use for storing the information in development enviroment)
- Liquibase [version:3.8.9] (the tool for keeping the version control for relational databases)
- JAX-WS [version:2.3.0] (the library for exposing soap web service endpoints)

 ## Implementation Details
 
JAX-WS is an API for producing and consuming REST-style and SOAP-style web services. The @WebService annotation marks the POJO class 
as a web service, and the @WebMethod annotation specifies which of the encapsulated methods is a service operation. In a class annotated as a @WebService, a public instance
method is thereby a service operation even if the method is not annotated.

Below are the SEI (Service Endpoint Interface) and SIB (Service Implementation Bean):
```
@WebService
public interface BankAccountService {
	
	@WebMethod
	public List<Transaction> getTrasactions();
	
	@WebMethod
	public List<Transaction> getTrasactionsForClient(@WebParam String client);

}
```
The *BankAccountService* is the SEI and specifies the service operations.

```
@Service
@WebService(serviceName = "bank-account", endpointInterface = "com.github.rshtishi.bankws.service.BankAccountService")
public class BankAccountServiceImpl implements BankAccountService {
	
	@Autowired
	private TransactionRepository transactionRepository;
	
	@WebMethod(operationName="getTransactions")
	@Override
	public List<Transaction> getTrasactions() {
		List<Transaction> transactions = transactionRepository.findAll();
		return transactions;
	}

	@WebMethod(operationName="getTransactionsForClient")
	@Override
	public List<Transaction> getTrasactionsForClient(@WebParam(name="client") String client) {
		List<Transaction> transactions = transactionRepository.findByClient(client);
		return transactions;
	}

}
```
*BankAccountServiceImpl* is SIB that provides the implementation of the operations specified in SEI.

Below we publish the endopoint for the *BankAccountService* SOAP-based web service:
```
@Configuration
public class SoapPublisher {

	private BankAccountService bankAccountService;
	private SoapProperties soapProperties;

	@Autowired
	public SoapPublisher(BankAccountService bankAccountService, SoapProperties soapProperties) {
		this.bankAccountService = bankAccountService;
		this.soapProperties = soapProperties;
		final String url = this.createUrl();
		Endpoint.publish(url, this.bankAccountService);
	}
	
	private String createUrl() {
		StringBuilder builder = new StringBuilder("http://");
		builder.append(this.soapProperties.getEndpoint().getHost());
		builder.append(":");
		builder.append(this.soapProperties.getEndpoint().getPort());
		builder.append("/");
		builder.append(this.soapProperties.getEndpoint().getName());
		return builder.toString();
	}
}
```

In the application.properties file you can modify the url when you want to endpoint to be published:
```
#SOAP
soap.endpoint.host=localhost
soap.endpoint.port=8888
soap.endpoint.name=baws
```

## Security

The SOAP endpoint requires HTTP Basic authentication and authorizes every call:

- `getTrasactionsForClient` only returns data for the clients listed for the caller (`soap.security.users.<user>.clients`).
- `getTrasactions` (all clients) is restricted to users with `soap.security.users.<user>.admin=true`.
- Unauthenticated requests get `401`; authenticated callers asking for data they may not see get an `Access denied` SOAP fault.
- A client address with 10 failed logins within a minute gets `429` (without running PBKDF2) until the window expires (successful logins do not reset the count); recently verified credentials are cached in memory for 5 minutes so valid callers do not pay the PBKDF2 cost on every request. Large distributed floods still need network-level rate limiting.
- With no users configured every request is rejected.

Passwords are stored as PBKDF2 hashes (at least 100,000 iterations, 16-byte salt and 32-byte hash; weaker hashes are rejected). Generate one with:
```
java -cp target/classes com.github.rshtishi.bankws.security.PasswordHashes
```
and configure the caller, preferably through environment variables rather than the properties file:
```
soap.security.users.partnera.password-hash=pbkdf2-sha256$310000$...
soap.security.users.partnera.clients=kbalaj
```

The endpoint refuses to start on a non-loopback `soap.endpoint.host` over plain HTTP. To expose it to third parties configure TLS:
```
soap.endpoint.ssl.key-store=/path/to/keystore.p12
soap.endpoint.ssl.key-store-password=${SOAP_KEYSTORE_PASSWORD}
```
(`soap.endpoint.allow-insecure-http=true` is only meant for deployments behind a TLS-terminating proxy.)

## Setting up the project
 
 - Clone the repository in your computer by executing: ```git clone https://github.com/rshtishi/BankWS.git```
 - build the project by executing:  ```mvn clean install```
 - run the application by executing:  ```mvn spring-boot:run```
 - Access the wsdl in url: ```http://localhost:8888/baws?wsdl``` (with the credentials of a configured SOAP user) to check if application started correctly.

