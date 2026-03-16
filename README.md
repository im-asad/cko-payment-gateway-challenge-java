# Payment Gateway Challenge — Java

## Requirements

- Java 17+
- Docker

## Setup

### 1. Start the bank simulator

```bash
docker-compose up -d
```

This starts the mock bank simulator on `http://localhost:8080`.

### 2. Run the application

```bash
./gradlew bootRun
```

The gateway starts on `http://localhost:8090`.

### 3. Run the tests

```bash
./gradlew test
```

## API Documentation

## Project structure

```
src/                        - Spring Boot application
  main/java/.../
    controller/             - REST endpoints
    service/                - Business logic and bank client
    model/
      api/                  - HTTP request/response models
      domain/               - Internal domain model
      bank/                 - Bank communication models
    repository/             - In-memory payment store
    exception/              - Exception types and global handler
    validation/             - Custom validation annotations
imposters/                  - Bank simulator configuration (do not modify)
docker-compose.yml          - Starts the bank simulator
```

## Notes

- The Gradle wrapper is pre-configured. No separate Gradle installation is needed.
- If your default `java` is not 17+, set `JAVA_HOME` explicitly: `JAVA_HOME=$(/usr/libexec/java_home) ./gradlew bootRun`
