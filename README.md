# Raiffeisen Accounting App MVP (Internship test task)
## Usage
_\*Assuming there is a `.env` file in the root of this project with postgres credentials (`POSTGRES_DB, POSTGRES_USER, POSTGRES_PASSWORD`):_

```
docker build -t <container-name> .
docker run -d -p 5432:<host_machine_port> --env-file .env <container-name>:latest


mvn clean install
mvn spring-boot:run
```

## TODOs
1. cover code with unit tests
2. implement request data validation and exception handling
3. return proper response codes
4. define and maintain invariants for Account
5. rework request/response entities into DTOs
6. ? add new meaningful API for account querying
7. ? implement transaction history
8. ? implement client auth + limit account exposure to owners only