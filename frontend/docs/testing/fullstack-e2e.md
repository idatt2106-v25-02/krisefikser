# Full-stack E2E med Cypress

Denne testpakken kjører mot ekte backend + database. Domenekall stubbes ikke; enkelte mutasjonstester bruker `cy.intercept` kun for å vente på ekte `PUT`-svar og asserte statuskode (ingen kunstig responskropp).

## Forutsetninger

- Docker + Docker Compose installert.
- `pnpm` installert og tilgjengelig i PATH (samme versjon som `packageManager` i [`package.json`](../../package.json) anbefales; `corepack enable` er et alternativ). Når dette er på plass, er `pnpm run test:e2e:fullstack:smoke` og `pnpm run test:e2e:fullstack:regression` tilstrekkelig — du trenger ikke `npx pnpm@…` med mindre du bevisst kjører uten global pnpm.
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

## Utvidede full-stack-scenarioer

Følgende specs er lagt til for å avdekke logikk på tvers av frontend og ekte API (uten å stubbe svar med `cy.intercept`; enkelte mutasjonstester bruker intercept kun for å vente på `PUT` og asserte HTTP-status):

| Fil | Kort innhold |
|-----|----------------|
| [`cypress/e2e/inventory-mutations.fullstack.cy.ts`](../../cypress/e2e/inventory-mutations.fullstack.cy.ts) | Oppdater vann, endre matvarenavn, toggl sjekkliste mot ekte backend |
| [`cypress/e2e/dashboard-household.fullstack.cy.ts`](../../cypress/e2e/dashboard-household.fullstack.cy.ts) | Seedet bruker på dashboard, navigasjon til husstand og husstandens refleksjoner |
| [`cypress/e2e/auth-login.fullstack.cy.ts`](../../cypress/e2e/auth-login.fullstack.cy.ts) | Feil passord (401) og vellykket innlogging via skjema |
| [`cypress/e2e/my-reflections.fullstack.cy.ts`](../../cypress/e2e/my-reflections.fullstack.cy.ts) | Siden «Mine refleksjoner» laster uten API-feilbanner |

**Mutasjoner og seed-data:** Tester som endrer lager skriver til databasen som vanlige brukerhandlinger. Lokalt kan du nullstille med ny `docker compose` / tom DB ved behov. I CI er miljøet typisk ferskt per kjøring.

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

## Leveransefaser

- **Fase 1 (nåværende leveranse):** Full-stack E2E mot ekte backend — smoke- og regresjonsscripts, alle `*.fullstack.cy.ts` pluss `fullstack-smoke.cy.ts` som beskrevet over, og tilhørende CI-jobber (smoke på PR, regresjon på natt/manuell kjøring).
- **Senere (utenfor fase 1):** Utvidet arbeid rundt seed/invite-flyter, glemt-passord-stabilitet og generell stabilitetsherding av testmiljøet er bevisst lagt utenfor denne leveransen; det kan planlegges som egne faser når behovet melder seg.
