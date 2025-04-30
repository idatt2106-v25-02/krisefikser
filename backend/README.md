# Krisefikser Backend

## Overview

This repository contains the backend service for Krisefikser, built with Spring Boot 3.4. It provides RESTful APIs for the Krisefikser crisis management platform, handling user authentication, emergency shelter tracking, crisis information, and more.

## Tech Stack

- Java 21
- Spring Boot 3.4
- Spring Security with JWT authentication
- Spring Data JPA
- MySQL/H2 databases
- JUnit & Spring Test for testing
- Swagger/OpenAPI for API documentation
- Maven for dependency management and build

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── stud/
│   │       └── ntnu/
│   │           └── krisefikser/
│   │               ├── auth/        # Authentication and security
│   │               ├── user/        # User management
│   │               ├── household/   # Household functionality
│   │               ├── map/         # Map and location features
│   │               ├── article/     # News and article functionality
│   │               ├── item/        # Emergency items tracking
│   │               ├── config/      # Application configuration
│   │               └── BackendApplication.java  # Main application class
│   └── resources/
│       ├── application.properties          # Default configuration
│       ├── application-dev.properties      # Development configuration
│       └── application-test.properties     # Testing configuration
└── test/  # Test classes mirroring the main structure
```

## Getting Started

### Prerequisites

- JDK 21
- Maven 3.8+
- Docker (for running MySQL)
- Git

### Development Setup

1. **Setup the database**

   The application is configured to use MySQL. You can start a MySQL instance using Docker:

   ```bash
   cd ..  # Go to root directory
   docker-compose up -d
   cd backend  # Return to backend directory
   ```

3. **Run the application**

   ```bash
   # Run with Maven
   ./mvnw spring-boot:run

   # Or with dev profile for development settings
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
   ```

4. **Verify the application is running**

   - The application should start on port 8080
   - Access Swagger UI: http://localhost:8080/swagger-ui.html

### Running Tests

```bash
# Run all tests
./mvnw test

# Run with coverage report
./mvnw test jacoco:report
```

Coverage reports will be generated in `target/site/jacoco`.

## Code Conventions

### Package Structure

We follow a domain-based package structure where each domain concept has its own package containing all related components:

```
domain/
├── controller/   # REST controllers
├── service/      # Business logic
├── repository/   # Data access
├── model/        # Domain models and entities
└── dto/          # Data Transfer Objects
```

### Naming Conventions

- **Classes**: PascalCase (e.g., `UserService`, `AuthController`)
- **Methods & Variables**: camelCase (e.g., `getUserById`, `findAllActiveUsers`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `MAX_LOGIN_ATTEMPTS`)
- **Packages**: lowercase (e.g., `auth`, `user`)
- **Database Tables**: snake_case (e.g., `user_profile`, `emergency_item`)

### Documentation Standards

Follow JavaDoc standards for all public methods and classes:

```java
/**
 * Retrieves a user by their unique identifier.
 *
 * <p>This method fetches the user entity from the database
 * and transforms it into a DTO for client consumption.</p>
 *
 * @param id The unique identifier of the user to retrieve
 * @return The user DTO if found
 * @throws ResourceNotFoundException if the user doesn't exist
 */
public UserDto getUserById(Long id) {
    // Implementation
}
```

### API Design Principles

1. Use RESTful conventions for endpoint naming

   - Collection: `/users` (GET, POST)
   - Specific resource: `/users/{id}` (GET, PUT, DELETE)

2. Use appropriate HTTP methods

   - GET: Retrieve data
   - POST: Create new resources
   - PUT: Update existing resources
   - DELETE: Remove resources

3. Return consistent response structures

```json
{
  "status": "success",
  "data": {
    // Resource data
  },
  "message": "Optional message"
}
```

4. Include meaningful error responses

```json
{
  "status": "error",
  "message": "User not found",
  "details": "No user exists with ID 123",
  "timestamp": "2023-05-15T14:30:00Z",
  "path": "/api/users/123"
}
```

### Testing Standards

1. **Unit tests**: Test individual components in isolation
2. **Integration tests**: Test interactions between components
3. **API tests**: Test API endpoints

Name test methods descriptively:

```java
@Test
void shouldReturnUserWhenValidIdProvided() {
    // Test implementation
}

@Test
void shouldThrowExceptionWhenUserNotFound() {
    // Test implementation
}
```

## Git Workflow

We follow the Gitflow workflow as described in the main README:

- **`refactor/*`**: Code refactoring
- **`doc/*`**: Documentation
- **`feat/*`**: New features
- **`hotfix/*`**: Critical production fixes
- **`main`**: Stable production code
- **`develop`**: Main development branch

### Commit Messages

Use standardized prefixes:

- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation
- `style`: Formatting (no code change)
- `refactor`: Code restructuring
- `test`: Test-related changes
- `chore`: Build/tool updates

Example: `feat: implement user authentication endpoint`

## Deployment

The backend is deployed through our CI/CD pipeline based on the branch:

- `develop` branch deploys to staging
- `main` branch deploys to production

See the main README.md for detailed CI/CD configuration.

## API Documentation

When the application is running, the full API documentation is available at:

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

## Troubleshooting

### Common Issues

1. **Database connection problems**

   - Verify Docker container is running: `docker ps`
   - Check application-dev.properties for correct credentials

2. **Build failures**

   - Ensure you have JDK 21 installed: `java -version`
   - Run `./mvnw clean` and try again

3. **Test failures**
   - Check that H2 is properly configured in application-test.properties
   - Ensure no tests depend on external services without mocks

### Getting Help

If you encounter issues not covered here, please:

1. Check existing GitHub issues
2. Contact the development team on Discord
3. Create a new GitHub issue with detailed steps to reproduce
