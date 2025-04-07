#!/bin/bash

# Start development environment PostgreSQL using docker-compose
echo "Starting development environment database..."
docker-compose -f docker-compose.dev.yml up -d

# Wait for the database to be ready
echo "Waiting for PostgreSQL to be ready..."
sleep 5

# Run the Spring Boot application with dev profile
echo "Starting backend application with dev profile..."
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev 