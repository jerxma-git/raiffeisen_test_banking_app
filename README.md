# Raiffeisen Accounting App MVP (Internship test task)
## Usage
_\*Assuming there is a `.env` file in the root of this project with postgres credentials (`POSTGRES_DB, POSTGRES_USER, POSTGRES_PASSWORD`):_

```
docker build -t <container-name> .
docker run -d -p 5432:<host_machine_port> --env-file .env <container-name>:latest


mvn clean install
mvn spring-boot:run
```