# Testing BankWS REST API

## Prerequisites

- JDK 17 (the project targets Java 11 source but builds fine on JDK 17 with Lombok >= 1.18.30)
- Maven wrapper is included (`./mvnw`); ensure it is executable (`chmod +x ./mvnw`)

## Build & Run

```bash
chmod +x ./mvnw
./mvnw clean install       # Build + run all tests
./mvnw spring-boot:run     # Start the app on port 8080
```

## REST Endpoints

| Method | URL | Description |
|--------|-----|-------------|
| GET | `/api/transactions` | Returns all transactions as JSON |
| GET | `/api/transactions?client={name}` | Returns transactions filtered by client name |

## Seed Data (Liquibase)

The H2 in-memory database is seeded with 5 transactions via Liquibase changelogs in `src/main/resources/db/changelog/`:

| id | client | amount | actionType |
|----|--------|--------|------------|
| 1 | rshtishi | 500.0 | DEPOSIT |
| 2 | rshtishi | 900.0 | DEPOSIT |
| 3 | rshtishi | 200.0 | WITHDRAW |
| 4 | kbalaj | 700.0 | DEPOSIT |
| 5 | vtafaj | 700.0 | DEPOSIT |

## Test Verification Steps

1. **All transactions**: `curl http://localhost:8080/api/transactions` — expect JSON array with 5 elements
2. **Filtered by client**: `curl http://localhost:8080/api/transactions?client=rshtishi` — expect 3 results, all with `client=rshtishi`
3. **Nonexistent client**: `curl http://localhost:8080/api/transactions?client=nonexistent` — expect empty array `[]`
4. **H2 Console** (dev only): Available at `http://localhost:8080/h2-console` with JDBC URL `jdbc:h2:mem:bankdb`, user `sa`, password `password`

## Known Environment Notes

- Lombok 1.18.12 (original) is incompatible with JDK 17. If the build fails with `IllegalAccessError` related to `LombokProcessor`, upgrade Lombok to >= 1.18.30 in `pom.xml`.
- The `mvnw` script may lack execute permissions after cloning. Run `chmod +x ./mvnw` before building.
- No CI is configured on this repository. Tests must be verified locally via `./mvnw test`.

## Devin Secrets Needed

None — the app uses an in-memory H2 database with hardcoded credentials (`sa`/`password`).
