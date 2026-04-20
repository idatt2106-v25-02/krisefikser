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

- `smoke`: raske kritiske flyter for PR-gate.
- `regression`: hele full-stack pakken (fase 2-4 specs + smoke).
