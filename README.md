# ShiftLog

[![CI](https://github.com/beamerboi/employee-shift-coordinator/actions/workflows/ci.yml/badge.svg)](https://github.com/beamerboi/employee-shift-coordinator/actions/workflows/ci.yml)
[![Coverage Status](https://coveralls.io/repos/github/beamerboi/employee-shift-coordinator/badge.svg?branch=main)](https://coveralls.io/github/beamerboi/employee-shift-coordinator?branch=main)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=beamerboi_employee-shift-coordinator&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=beamerboi_employee-shift-coordinator)

ShiftLog is a small Java 17 application for managing employees, shifts, and employee assignments. It is an automated-testing exam project with a Java Swing GUI and a retained REST API.

## Architecture

Both interfaces call the same service layer, which contains the use cases and domain rules. Services depend on repository abstractions. Spring profiles select either the MongoDB or PostgreSQL repository implementation; business logic is not duplicated.

## Requirements and verification

Install Java 17, Maven, and Docker. Docker must be running; PostgreSQL and MongoDB do not need to be installed or started because Testcontainers manages both during the build.

```shell
mvn clean verify
```

This runs unit, Swing GUI, service-backed GUI E2E, REST slice, and real-database integration tests; enforces JaCoCo line/branch coverage; and runs PIT mutation analysis. Reports are written below `target/site/jacoco` and `target/pit-reports`. To run PIT independently, use `mvn pitest:mutationCoverage`.

## Running the application

Start the selected database, then choose a Spring profile:

```shell
# MongoDB (default profile)
mvn spring-boot:run -Dspring-boot.run.profiles=mongo

# PostgreSQL
mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```

MongoDB uses `MONGODB_URI` (default `mongodb://localhost:27017/shiftlog`). PostgreSQL uses `POSTGRES_URL`, `POSTGRES_USER`, and `POSTGRES_PASSWORD`; the password intentionally has no checked-in default. Set `SHIFTLOG_GUI_ENABLED=false` for REST-only/headless operation. The REST documentation is available at `/swagger-ui.html`.

For Eclipse, import the repository with **File → Import → Existing Maven Projects**, select Java 17, and update the Maven project if prompted. Committed Eclipse metadata provides consistent source folders and compiler settings.

Coveralls requires the `COVERALLS_REPO_TOKEN` GitHub secret. SonarCloud requires `SONAR_TOKEN`; its project key and organization are non-secret Maven properties.
