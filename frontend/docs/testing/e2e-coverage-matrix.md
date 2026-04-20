# E2E Coverage Matrix (Full-stack fokus)

Denne matrisen dokumenterer hvilke kritiske brukerreiser som allerede er dekket, og hvilke som er lagt til for full-stack E2E (uten `cy.intercept`-mocking).

## Ruter og domener

| Domene | Kritisk flyt | Dagens dekning | Ny full-stack dekning |
| --- | --- | --- | --- |
| Auth | Login og redirect | Delvis (mock-basert) | `fullstack-smoke.cy.ts` |
| Dashboard/profil | Last profil + innstillinger | Delvis (render) | `profile-settings.fullstack.cy.ts` |
| Varsler | Les varsler + marker som lest | Mangler | `notifications.fullstack.cy.ts` |
| Kriser/refleksjoner | Kriseoversikt + detalj + refleksjonsinngang | Mangler | `events-reflections.fullstack.cy.ts` |
| Husstand | Last husstand + forlat/slett/404-redirect | Delvis (mock-basert) | `household-advanced.fullstack.cy.ts` |
| Admin authz | Admin/superadmin tilgangsmatrise | Delvis (negativ rute) | `admin-authz.fullstack.cy.ts` |
| Admin CRUD | Artikkel CRUD | Mangler | `admin-core-crud.fullstack.cy.ts` |
| Offentlig innhold | Scenario/nyheter/kart | Mangler | `public-content.fullstack.cy.ts` |

## Guard-matrise

Basert på `frontend/src/router/index.ts` og `frontend/src/router/guards.ts`:

- `requiresAuth`: verifiseres i `fullstack-smoke.cy.ts` og `household-advanced.fullstack.cy.ts`.
- `requiresGuest`: verifiseres via login/register avvisning i `admin-authz.fullstack.cy.ts` og eksisterende guard-spec.
- `requiresAdmin`: verifiseres i `admin-authz.fullstack.cy.ts`.
- `requiresSuperAdmin`: verifiseres i `admin-authz.fullstack.cy.ts`.

## Kriterier for «kritisk dekket»

En flyt regnes som kritisk dekket når testen:

1. Starter med ekte innlogging (`cy.request` mot backend),
2. Bruker ekte API-kall i UI (ingen `cy.intercept` for domenelogikk),
3. Verifiserer sluttstate i UI eller URL/routing etter handling.

## Neste trinn for videre dekningsløft

- Flytte gjenværende mock-baserte kjerneflyter til full-stack varianter.
- Innføre testdata-reset for lokal kjøring mellom spesifikasjoner ved behov.
- Knytte nye spesifikasjoner til smoke/regression-jobber i CI.
