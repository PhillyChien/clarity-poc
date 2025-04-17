# Clarity POC - Backend

This directory contain the backend service based on Spring MVC, providing RESTful APIs.

## Tech Stack

- Framework: Spring MVC (Java)
- Security: Spring Security
- Authentication: Basic username/password login/registration
- Authorization: JWT (JSON Web Tokens)
- Database: PostgreSQL
- Database Migration: Flyway
- Dependencies: Spring Data JPA, Lombok, Validation

## Local Development

1. Ensure Java 17 and Maven are installed
2. Configure `application.properties` or `application.yml` file
3. Run `mvn spring-boot:run` to start the service

## Run via Docker

Run `docker-compose up --build -d` in the root directory

## CI/CD Pipeline

The backend application is configured to use GitHub Actions for CI/CD. The workflow is defined in `.github/workflows/backend-ci.yml`.


