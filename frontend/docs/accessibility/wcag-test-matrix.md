## WCAG 2.1 Test Matrix (Private Webapp)

This matrix tracks representative user flows and the WCAG criteria that must pass before release.

### Scope

- Standard: WCAG 2.1 A/AA
- Product type: private webapp
- Primary routes source: `frontend/src/router/index.ts`

### Flow Coverage

| Flow | Route examples | Critical checks | Status |
| --- | --- | --- | --- |
| Public navigation | `/`, `/kart`, `/kriser`, `/om-oss` | Keyboard access, visible focus, skip link, heading structure, link purpose | In progress |
| Authentication | `/logg-inn`, `/registrer`, `/glemt-passord` | Labels/instructions, error messages, focus management, non-color error indicators | In progress |
| Forms with validation | `AddressForm`, registration inputs | `aria-invalid`, `aria-describedby`, error text announced, logical tab order | In progress |
| Data tables | Admin users table | Proper table headers, caption, empty state announcement, action buttons with names | In progress |
| Dialog/modal interactions | Notification modal, shared dialog content | Dialog semantics (`role="dialog"` + `aria-modal`), Esc close, focus trap, focus return | In progress |

### Component Targets

- `frontend/src/App.vue`
- `frontend/src/components/layout/Navbar.vue`
- `frontend/src/components/layout/AddressForm.vue`
- `frontend/src/views/auth/register/RegisterView.vue`
- `frontend/src/components/notification/NotificationModal.vue`
- `frontend/src/components/ui/dialog/DialogContent.vue`
- `frontend/src/components/ui/data-table/DataTable.vue`
- `frontend/src/components/admin/users/UserTable.vue`
- `frontend/src/components/ui/form/FormControl.vue`
- `frontend/src/components/ui/form/useFormField.ts`

### Result Logging Format

For each finding:

- WCAG SC (example: `2.4.7`)
- Location (route/component)
- Severity (High/Medium/Low)
- Issue summary
- Remediation link/PR
