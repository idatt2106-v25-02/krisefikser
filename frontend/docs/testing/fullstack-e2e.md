# Full-stack E2E med Cypress

Denne testpakken kjører mot ekte backend + database (ingen API-mocking med `cy.intercept` for domeneflyter).

## Forutsetninger

- Docker + Docker Compose installert.
- `pnpm` installert.
- Ledige porter `3306`, `8080` og `5173`.

## Start testmiljø lokalt

Fra repo-roten:

```bash
docker compose up -d db backend
```

Fra `frontend`:

```bash
pnpm install
pnpm run build
pnpm run test:e2e:fullstack:smoke
```

For full regresjon:

```bash
pnpm run test:e2e:fullstack:regression
```

## Seedede brukere (dev-seeding)

Disse kommer fra backend `DataSeeder`:

- Standardbruker: `brotherman@testern.no` / `password`
- Admin: `admin@example.com` / `admin123`
- Superadmin: `admin@krisefikser.app` / `admin123`

Verdiene kan overstyres via Cypress-env i `cypress.config.ts`.

## Smoke vs regression

- `smoke`: raske kritiske flyter for PR-gate (GitHub Actions-jobb `frontend-e2e-fullstack-smoke` på push/PR mot `main`/`develop`).
- `regression`: hele full-stack-pakken (alle `*.fullstack.cy.ts` pluss `fullstack-smoke.cy.ts`).

### CI (GitHub Actions)

- **Push / pull request:** Kjører mockede Cypress-tester og full-stack **smoke** mot Docker (`db` + `backend`), ikke full regresjon.
- **Nattlig `schedule` og manuell `workflow_dispatch`:** Jobben `frontend-e2e-fullstack-regression` bygger frontend med Istanbul-instrumentering (`pnpm run build:coverage`), kjører `pnpm run test:e2e:fullstack:regression`, genererer rapport med `pnpm run e2e:coverage:report`, og laster opp artefakten `frontend-e2e-coverage-report` (HTML + lcov under `coverage-e2e/`).

Manuell kjøring: i GitHub-repoet velg workflow **CI Pipeline** → **Run workflow**.

### E2E-dekning vs Vitest-dekning

- **Vitest** (`pnpm run test:coverage`): enhetstester; rapport i `frontend/coverage/` med terskler i CI.
- **Cypress E2E** (instrumentert bygg): måler hvilken **frontend**-kode (Vue/TS) som faktisk kjører i nettleseren under testene; rapport i `frontend/coverage-e2e/`. Dette er **ikke** Java/JaCoCo for Spring Boot.

Lokalt etter instrumentert bygg og full-stack-regresjon:

```bash
pnpm run build:coverage
# start docker + kjør f.eks. test:e2e:fullstack:regression
pnpm run e2e:coverage:report
```
