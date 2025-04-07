# Clarity POC - Backend

This directory contains the backend service based on Spring MVC, providing RESTful APIs.

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