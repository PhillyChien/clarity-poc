#!/bin/bash

# Go to the project's root directory
cd ..

# Start development environment PostgreSQL using docker-compose
echo "Starting development environment database..."
docker-compose -f docker-compose.dev.yml up -d

# Wait for the database to be ready
echo "Waiting for PostgreSQL to be ready..."
sleep 5

# Ask if the user wants to manually execute Flyway migrations
read -p "Do you want to manually execute Flyway migrations? (y/n): " execute_migrations

if [ "$execute_migrations" = "y" ] || [ "$execute_migrations" = "Y" ]; then
  echo "Executing Flyway migrations in development environment..."
  cd backend
  ./mvnw flyway:migrate -Dspring.profiles.active=dev
  cd ..
  echo "Migrations completed."
fi

# Run the Spring Boot application with dev profile
echo "Starting backend application with dev profile..."
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev 