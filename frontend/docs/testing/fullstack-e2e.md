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
# API må peke på Spring i Docker (samme som Cypress baseUrl-host)
export VITE_API_URL=http://127.0.0.1:8080
pnpm run build
pnpm run test:e2e:fullstack:smoke
```

For full regresjon:

```bash
export VITE_API_URL=http://127.0.0.1:8080
pnpm run test:e2e:fullstack:regression
```

### Auth, Turnstile og fangst av e-post (profil `e2e`)

CI og anbefalt lokalt oppsett for full-stack bruker **Spring-profilen `e2e`** sammen med `dev`. Da lagres utgående HTML-e-post i minne (ingen Mailtrap), og Cypress kan hente bekreftelseslenke via et internt API.

**Viktig:** Aktiver aldri profilen `e2e` i produksjon.

| Variabel | Formål |
|----------|--------|
| `SPRING_PROFILES_ACTIVE=dev,e2e` | Laster [`application-e2e.properties`](../../../backend/src/main/resources/application-e2e.properties) og registrerer e-postfangst + hook. |
| `TURNSTILE_SECRET_KEY=1x0000000000000000000000000000000AA` | Cloudflare **test**-hemmelighet; sammen med dummy-tokenet `XXXX.DUMMY.TOKEN.XXXX` (satt av Cypress `stubTurnstile`) passer server-side validering. |
| `FRONTEND_URL=http://127.0.0.1:5173` | Må samsvare med Cypress [`cypress.config.ts`](../../cypress.config.ts) `baseUrl` slik at CORS og lenker i e-post (`/verify?token=…`) er gyldige. |
| `E2E_MAIL_HOOK_SECRET` | Hemmelighet for header `X-E2E-Mail-Hook` på `GET /api/e2e/mail/latest`. |

Cypress får samme hemmelighet via `CYPRESS_e2eMailHookSecret` (tilgjengelig som `e2eMailHookSecret` i `Cypress.env()`). Uten denne variabelen hoppes [`auth-registration-email.fullstack.cy.ts`](../../cypress/e2e/auth-registration-email.fullstack.cy.ts) over.

Eksempel lokalt (PowerShell):

```powershell
$env:E2E_MAIL_HOOK_SECRET = "dev-shared-secret"
$env:SPRING_PROFILES_ACTIVE = "dev,e2e"
$env:TURNSTILE_SECRET_KEY = "1x0000000000000000000000000000000AA"
$env:FRONTEND_URL = "http://127.0.0.1:5173"
docker compose up -d db backend
cd frontend
$env:VITE_API_URL = "http://127.0.0.1:8080"
$env:CYPRESS_e2eMailHookSecret = "dev-shared-secret"
pnpm run build
pnpm run test:e2e:fullstack:regression
```

## Utvidede full-stack-scenarioer

Følgende specs er lagt til for å avdekke logikk på tvers av frontend og ekte API (uten å stubbe svar med `cy.intercept`; enkelte mutasjonstester bruker intercept kun for å vente på `PUT` og asserte HTTP-status):

| Fil | Kort innhold |
|-----|----------------|
| [`cypress/e2e/inventory-mutations.fullstack.cy.ts`](../../cypress/e2e/inventory-mutations.fullstack.cy.ts) | Oppdater vann, endre matvarenavn, toggl sjekkliste mot ekte backend |
| [`cypress/e2e/dashboard-household.fullstack.cy.ts`](../../cypress/e2e/dashboard-household.fullstack.cy.ts) | Seedet bruker på dashboard, navigasjon til husstand og husstandens refleksjoner |
| [`cypress/e2e/auth-login.fullstack.cy.ts`](../../cypress/e2e/auth-login.fullstack.cy.ts) | Feil passord (401) og vellykket innlogging via skjema |
| [`cypress/e2e/auth-registration-email.fullstack.cy.ts`](../../cypress/e2e/auth-registration-email.fullstack.cy.ts) | Registrering → fanget bekreftelses-e-post → `/verify` → innlogging (krever `e2e`-profil + hook-hemmelighet) |
| [`cypress/e2e/my-reflections.fullstack.cy.ts`](../../cypress/e2e/my-reflections.fullstack.cy.ts) | Siden «Mine refleksjoner» laster uten API-feilbanner |

**Mutasjoner og seed-data:** Tester som endrer lager skriver til databasen som vanlige brukerhandlinger. Lokalt kan du nullstille med ny `docker compose` / tom DB ved behov. I CI er miljøet typisk ferskt per kjøring.

## Seedede brukere (dev-seeding)

Disse kommer fra backend `DataSeeder`:

- Standardbruker: `brotherman@testern.no` / `password`
- Admin: `admin@example.com` / `admin123`
- Superadmin: `admin@krisefikser.app` / `admin123`

Verdiene kan overstyres via Cypress-env i `cypress.config.ts`.

## Smoke vs regression

- `smoke`: raske kritiske flyter for PR-gate (GitHub Actions-jobb `frontend-e2e-fullstack-smoke` på push/PR mot `main`/`develop`). Listen i `package.json` er eksplisitt; **registrering med fanget e-post** (`auth-registration-email.fullstack.cy.ts`) kjører kun i regresjon for å holde PR-gaten kort.
- `regression`: hele full-stack-pakken (alle `*.fullstack.cy.ts` pluss `fullstack-smoke.cy.ts`).

### CI (GitHub Actions)

- **Push / pull request:** Kjører mockede Cypress-tester og full-stack **smoke** mot Docker (`db` + `backend`) med profiler `dev,e2e`, Turnstile test-hemmelighet, `FRONTEND_URL`/`VITE_API_URL` for `127.0.0.1`, og tilfeldig `E2E_MAIL_HOOK_SECRET` delt med Cypress — ikke full regresjon.
- **Nattlig `schedule` og manuell `workflow_dispatch`:** Jobben `frontend-e2e-fullstack-regression` bygger frontend med Istanbul-instrumentering (`pnpm run build:coverage`) og `VITE_API_URL=http://127.0.0.1:8080`, starter backend med samme e2e-oppsett som smoke, kjører `pnpm run test:e2e:fullstack:regression`, genererer rapport med `pnpm run e2e:coverage:report`, og laster opp artefakten `frontend-e2e-coverage-report` (HTML + lcov under `coverage-e2e/`).

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

- **Fase 1 (nåværende leveranse):** Full-stack E2E mot ekte backend — smoke- og regresjonsscripts, alle `*.fullstack.cy.ts` pluss `fullstack-smoke.cy.ts` som beskrevet over, og tilhørende CI-jobber (smoke på PR, regresjon på natt/manuell kjøring), inkludert registrering med fanget bekreftelses-e-post.
- **Senere:** Utvidet arbeid rundt seed/invite-flyter, fullstack glemt-passord med samme e-postfangst, og generell stabilitetsherding av testmiljøet kan planlegges når behovet melder seg.
