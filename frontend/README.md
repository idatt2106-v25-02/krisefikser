# .

This template should help get you started developing with Vue 3 in Vite.

## Recommended IDE Setup

[VSCode](https://code.visualstudio.com/) + [Volar](https://marketplace.visualstudio.com/items?itemName=Vue.volar) (and disable Vetur).

## Type Support for `.vue` Imports in TS

TypeScript cannot handle type information for `.vue` imports by default, so we replace the `tsc` CLI with `vue-tsc` for type checking. In editors, we need [Volar](https://marketplace.visualstudio.com/items?itemName=Vue.volar) to make the TypeScript language service aware of `.vue` types.

## Customize configuration

See [Vite Configuration Reference](https://vite.dev/config/).

## Project Setup

```sh
pnpm install
```

### Compile and Hot-Reload for Development

```sh
pnpm dev
```

### Type-Check, Compile and Minify for Production

```sh
pnpm build
```

### Run Unit Tests with [Vitest](https://vitest.dev/)

```sh
pnpm test:unit
```

### Run End-to-End Tests with [Playwright](https://playwright.dev)

```sh
# Install browsers for the first run
npx playwright install

# When testing on CI, must build the project first
pnpm build

# Runs the end-to-end tests
pnpm test:e2e
# Runs the tests only on Chromium
pnpm test:e2e --project=chromium
# Runs the tests of a specific file
pnpm test:e2e tests/example.spec.ts
# Runs the tests in debug mode
pnpm test:e2e --debug
```

### Lint with [ESLint](https://eslint.org/)

```sh
pnpm lint
```

## Project Structure
```
🗂️ Project Structure
src/
├── assets/
├── components/
│   ├── common/               # Footer, CTA buttons, etc.
│   ├── household/            # Shared between roles
│   ├── inventory/            # Item and category management
│   ├── map/                  # Map logic (used across user roles)
│   ├── modals/               # Invite, ReCaptcha, Confirm dialogs
│   ├── notifications/        # Event notification display
│   └── privacy/              # Consent & policy components
├── views/
│   ├── nonRegistered/
│   │   ├── HomeView.vue
│   │   ├── RegisterView.vue
│   │   ├── MapView.vue
│   │   └── JoinOrCreateHouseholdView.vue
│   ├── registered/
│   │   ├── DashboardView.vue
│   │   ├── InventoryView.vue
│   │   ├── AddItemView.vue
│   │   ├── HouseholdView.vue
│   │   ├── InviteView.vue
│   │   └── SearchView.vue
│   ├── admin/
│   │   ├── AdminLoginView.vue
│   │   ├── AdminDashboardView.vue
│   │   ├── AdminMapView.vue
│   │   ├── AdminEventsView.vue
│   │   └── AdminSearchView.vue
│   ├── superAdmin/
│   │   ├── SuperAdminDashboardView.vue
│   │   └── ManageAdminsView.vue
│   ├── auth/
│   │   ├── LoginView.vue
│   │   ├── ForgotPasswordView.vue
│   │   └── ResetPasswordView.vue
│   └── errors/
│       └── NotFoundView.vue
├── router/
│   └── index.js              # Route guards, roles, redirects
├── store/
│   └── index.js              # Auth, user role, inventory, etc.
├── App.vue
└── main.js
```
