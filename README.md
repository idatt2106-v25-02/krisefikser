# Krisefikser.no

## About

Krisefikser is a comprehensive crisis management platform designed to help users prepare for, navigate through, and recover from emergency situations. The application provides real-time information about crisis events, locations of emergency shelters, and resources for crisis preparedness.

## Features

- Interactive crisis map showing emergency shelters and active crisis zones
- Real-time location tracking to determine if users are in crisis areas
- Emergency preparedness information and checklists
- User account management with household coordination
- News and updates about crisis situations
- Resource sharing and community support

## Tech Stack

### Frontend

- Vue 3 with Composition API
- TypeScript
- Tailwind CSS
- Leaflet for interactive maps
- Vue Query for data fetching
- Vite build system

### Backend

- Spring Boot 3.4
- Java 21
- Spring Security
- Spring Data JPA
- MySQL database
- JWT authentication

## Getting Started

### Prerequisites

- Node.js (v18+)
- PNPM (v10+)
- JDK 21
- Docker and Docker Compose
- Maven

### Setting Up Development Environment

1. **Clone the repository**

   ```bash
   git clone https://github.com/idatt2106-v25-02/krisefikser.git
   cd krisefikser
   ```

2. **Start the database**

   ```bash
   docker-compose up -d
   ```

   This will start a MySQL database with the necessary configuration.

3. **Set up the backend**

   ```bash
   cd backend
   ./mvnw clean install
   ./mvnw spring-boot:run
   ```

   The backend will start on port 8080 by default.

4. **Set up the frontend**

   ```bash
   cd frontend
   pnpm install
   pnpm dev
   ```

   The frontend development server will start on port 5173 by default.

5. **Access the application**

   Open your browser and navigate to `http://localhost:5173`

## Development Workflow

### Gitflow Workflow

We follow the Gitflow workflow with the following branches:

- **`refactor/*`**: Code refactoring
- **`doc/*`**: Documentation
- **`feat/*`**: New features
- **`hotfix/*`**: Critical production fixes
- **`main`**: Stable production code
- **`develop`**: Main development branch

We use merge (not rebase) as our merge strategy.

### CI/CD Automation per Branch

| Branch       | CI (Testing)                      | CD (Deployment)                        |
| ------------ | --------------------------------- | -------------------------------------- |
| `refactor/*` | Unit tests, linting               | No deployment (work in progress)       |
| `doc/*`      | Unit tests, linting               | No deployment (work in progress)       |
| `feat/*`     | Unit tests, linting               | No deployment (work in progress)       |
| `hotfix/*`   | Hotfix testing                    | Quick production deploy after approval |
| `develop`    | Unit, integration, security tests | Automatic to staging environment       |
| `main`       | Final test run                    | Automatic to production                |

### Commit Messages

Use standardized prefixes:

- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation
- `style`: Formatting (no code change)
- `refactor`: Code restructuring
- `test`: Test-related changes
- `chore`: Build/tool updates

### Backend Development

- API documentation is available at `http://localhost:8080/swagger-ui.html` when the backend is running
- Run tests with `./mvnw test`
- The backend follows a standard Spring Boot architecture with controllers, services, and repositories

### Frontend Development

- Use `pnpm lint` to check code quality
- Use `pnpm test:unit` to run unit tests
- Use `pnpm test:e2e` to run end-to-end tests with Playwright
- The API client is auto-generated using Orval

### Testing

#### Backend

- Unit tests
- Controller tests
- Integration tests

#### Frontend

- Unit tests (Vitest)
- E2E tests (Cypress)

### Documentation Standards

#### Structure and Formatting

- First sentence is a brief summary
- Use HTML tags like `<p>`, `<code>`, `<ul>` for clarity
- Maximum 80 characters per line
- Third-person style ("It should...", "The method handles...")

#### Grammar and Tags

- **Third person** preferred ("it", "they")
- **JavaDoc tags**:
  - `@author`, `@version` (classes/interfaces)
  - `@param`, `@return` (methods)
  - `@see`, `@deprecated` (optional)

#### Documentation Example

```java
/**
 * Calculates the total price based on quantity.
 * <p>
 * Uses <code>calculateTax</code> to include VAT.
 * @param quantity Number of units (must be > 0).
 * @return Total price including VAT.
 */
public double calculateTotal(int quantity) { ... }
```

## Deployment

The application is deployed using a CI/CD pipeline.

## Contributing

If you'd like to contribute to the project, please first read the [Gitflow Workflow](#gitflow-workflow) and [commit messages](#commit-messages).

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'feat: Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Merge Request

## Credits

Developed as part of the IDATT2106 course at NTNU by:

- [Henrik Halvorsen Kvamme](https://gitlab.stud.idi.ntnu.no/henrihkv)
- [Conrad Tinius Osvik](https://gitlab.stud.idi.ntnu.no/conrado)
- [Embret Olav Rasmussen Roås](https://gitlab.stud.idi.ntnu.no/eoroaas)
- [Jakob Huuse](https://gitlab.stud.idi.ntnu.no/jakobhu)
- [Kaamya Shinde](https://gitlab.stud.idi.ntnu.no/kamyaas)
- [Kevin Dennis Mazali](https://gitlab.stud.idi.ntnu.no/kevindm)
- [Shiza Ahmad](https://gitlab.stud.idi.ntnu.no/shizaa)
