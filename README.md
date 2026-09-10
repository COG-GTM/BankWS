# BankWS

*BankWS* is spring-boot application that exposes a soap web service endpoint so third parties on different platforms can invoke
them via SOAP.

## Bussiness Case

A bank has many third-party application that need to access the user transaction history. The user transaction history is stored 
in the core system of the bank. The third-party applications are written with different technologies stack. We need to provide a way 
to expose the information from the core system of the bank in safe manner to all third-party application who need this information.

## Technology

BankWS is using the following technologies:
- Java [version: 21] (the language used to write the application)
- Maven [version:3.6] (the tool for managing dependencies and building the project) 
- Lombok [version:1.18.46, Spring Boot-managed] (the java library for removing boiler plate code from pojos)
- Spring-Boot [version:3.5.16] (the framework for creating spring application that just run)
- Spring-Data [version:3.5.x] (the framework for interacting with database)
- H2 Database [version:2.x] (the database we use for storing the information in development enviroment)
- Liquibase [version:4.x] (the tool for keeping the version control for relational databases)
- JAX-WS [version:4.0.5, Jakarta] (the library for exposing soap web service endpoints)

## Java 11 → 21 Migration

- Upgraded Spring Boot from 2.3.0 to 3.5.16 and Java from 11 to 21.
- Updated JPA and JAX-WS imports from `javax` to `jakarta`.
- Replaced the JAX-WS `jaxws-ri` 2.3 dependency with the `jaxws-rt` 4.0 runtime.
- Migrated to the Hibernate 6 and Liquibase 4 versions managed by Spring Boot 3.5.
- Added a JPA converter so H2 enum labels map correctly without changing the existing schema.

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

## Setting up the project
 
 - Clone the repository in your computer by executing: ```git clone https://github.com/rshtishi/BankWS.git```
 - build the project by executing:  ```mvn clean install```
 - run the application by executing:  ```mvn spring-boot:run```
 - Access the wsdl in url: ```http://localhost:8888/baws?wsdl``` to check if application started correctly.
