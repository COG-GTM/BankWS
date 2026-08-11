# BankWS

*BankWS* is spring-boot application that exposes a REST API so third parties on different platforms can consume
the bank transaction history over HTTP/JSON.

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
- Spring-MVC [version:2.3.0.RELEASE] (the framework for exposing the REST endpoints)
- Spring-Data [version:2.3.0.RELEASE] (the framework for interacting with database)
- H2 Database [version:1.4.2] (the database we use for storing the information in development enviroment)
- Liquibase [version:3.8.9] (the tool for keeping the version control for relational databases)

 ## Implementation Details

The service layer is a plain Spring bean and the REST layer is a `@RestController` that delegates to it.

```
public interface BankAccountService {

	public List<Transaction> getTrasactions();

	public List<Transaction> getTrasactionsForClient(String client);

}
```

```
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
```

The port of the embedded Tomcat can be changed in the application.properties file:
```
#REST
server.port=8080
```

## REST API

| Method | Path | Description |
| --- | --- | --- |
| GET | `/transactions` | Returns all the transactions. |
| GET | `/transactions/client/{client}` | Returns the transactions of the given client. Responds with 200 and an empty array when the client is unknown or has no transactions. |

Example:
```
curl http://localhost:8080/transactions
curl http://localhost:8080/transactions/client/rshtishi
```

Example response:
```
[
  {
    "id": 1,
    "client": "rshtishi",
    "date": "2007-08-09T13:14:15",
    "amount": 500.0,
    "actionType": "DEPOSIT"
  }
]
```

## Setting up the project
 
 - Clone the repository in your computer by executing: ```git clone https://github.com/rshtishi/BankWS.git```
 - build the project by executing:  ```mvn clean install```
 - run the application by executing:  ```mvn spring-boot:run```
 - Access ```http://localhost:8080/transactions``` to check if application started correctly.
