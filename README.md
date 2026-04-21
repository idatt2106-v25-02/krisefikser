# Krisefikser.no

Crisis preparedness and response in one place: maps, shelters, news, households, and resources. Built for **before, during, and after** a crisis—in line with the product owners’ vision to raise household readiness and bring scattered services together.

Student project in **IDATT2106** (NTNU): **three weeks**, two implementation sprints and one documentation sprint. Lower-priority user stories may be partial or omitted.

- **Vision (PDF):** [Visjonsdokument 2025 (final)](frontend/public/docs/visjonsdokument-2025-final.pdf)
- **Traceability in the app:** open **Emne, krav og teknologi** at `/emne-og-teknologi` when the frontend is running (e.g. `http://localhost:5173/emne-og-teknologi`).

Feedback: [kontakt@krisefikser.app](mailto:kontakt@krisefikser.app)

## Tech stack

**Frontend:** Vue 3.5, TypeScript, Vite 6, Pinia, TanStack Vue Query, Tailwind 4, Reka UI, Lucide, Leaflet / Vue Leaflet, Axios, Orval, VeeValidate, Zod, WebSocket/STOMP where enabled. Package manager: **pnpm 10**.

**Backend:** Java 21, Spring Boot 3.4 (Web, JPA, Security, Validation, WebSocket), REST + JPA, JWT, SpringDoc OpenAPI. **MySQL** in dev/prod-style setups, **H2** in tests. JaCoCo line coverage gate **≥80%** on `mvn verify`.

**Quality & CI:** Vitest, Cypress (incl. fullstack specs where configured), a11y checks in pipeline, GitHub Actions — see [.github/workflows/ci.yml](.github/workflows/ci.yml) (runs on `main` / `develop`, PRs, nightly cron, and manual dispatch).

## Prerequisites

Node **18+**, **pnpm 10+**, **JDK 21**, **Docker** (Compose), **Maven** (or use the Maven wrapper in `backend/`).

## Quick start

1. **Clone**

   ```bash
   git clone https://github.com/idatt2106-v25-02/krisefikser.git
   cd krisefikser
   ```

2. **Database**

   ```bash
   docker-compose up -d
   ```

3. **Backend** (from `backend/`, default port **8080**)

   ```bash
   cd backend
   ./mvnw spring-boot:run
   ```

   On Windows CMD, use `mvnw.cmd spring-boot:run` if `./mvnw` is not available.

4. **Frontend** (from `frontend/`, default **5173**)

   ```bash
   cd frontend
   pnpm install
   pnpm dev
   ```

5. Open [http://localhost:5173](http://localhost:5173). API docs: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) when the backend is up.

## Environment variables

Copy examples and fill in values: [backend/.env.example](backend/.env.example), [frontend/.env.example](frontend/.env.example) (Turnstile, Mailtrap, URLs, etc.).

## Tests

- Backend: `cd backend && ./mvnw test` (verify with JaCoCo: `./mvnw verify`)
- Frontend: `cd frontend && pnpm test:unit` · `pnpm test:e2e` (Cypress against preview; see `package.json` for fullstack variants)

## Credits

IDATT2106, NTNU:

- [Henrik Halvorsen Kvamme](https://gitlab.stud.idi.ntnu.no/henrihkv)
- [Conrad Tinius Osvik](https://gitlab.stud.idi.ntnu.no/conrado)
- [Embret Olav Rasmussen Roås](https://gitlab.stud.idi.ntnu.no/eoroaas)
- [Jakob Huuse](https://gitlab.stud.idi.ntnu.no/jakobhu)
- [Kaamya Shinde](https://gitlab.stud.idi.ntnu.no/kamyaas)
- [Kevin Dennis Mazali](https://gitlab.stud.idi.ntnu.no/kevindm)
- [Shiza Ahmad](https://gitlab.stud.idi.ntnu.no/shizaa)
