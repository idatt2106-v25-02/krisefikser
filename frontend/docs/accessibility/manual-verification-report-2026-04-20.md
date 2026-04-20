## Manual Verification Report (2026-04-20)

### Executed checks

- `corepack pnpm exec eslint ...` on modified accessibility-related files: pass
- `corepack pnpm run test:a11y`: pass (2 axe checks)
- Code walkthrough against keyboard/focus/dialog/table/form requirements in:
  - `src/App.vue`
  - `src/components/layout/Navbar.vue`
  - `src/components/notification/NotificationModal.vue`
  - `src/components/layout/AddressForm.vue`
  - `src/views/auth/register/RegisterView.vue`
  - `src/components/ui/data-table/DataTable.vue`
  - `src/components/admin/users/UserTable.vue`

### Checklist outcome

- Keyboard navigation and focus visibility: pass (skip link, menu button semantics, focusable controls)
- Dialog behavior: pass (dialog semantics, focus trap, Escape close, focus return)
- Form labeling and error exposure: pass (programmatic labels + alert text for geocoding errors)
- Table semantics and empty state: pass (caption support, header scope, row count empty state)
- Document language metadata: pass (`<html lang="no">`)

### Requires local assistive-tech session

The following checks need a real desktop assistive-tech setup and were prepared but not executable inside CLI-only validation:

- NVDA/VoiceOver announcement quality on full user flows
- Visual contrast measurement on rendered pages
- 200% zoom and 320 CSS px reflow across all representative routes

Run these using `docs/accessibility/manual-verification.md` as the execution checklist.
