## Accessibility Definition of Done

Any frontend change is done only when all items below are satisfied:

- New/updated UI is keyboard accessible and has visible focus states.
- Inputs have programmatic labels and errors are provided as text.
- Dialogs and overlays expose name/role/value and manage focus correctly.
- Dynamic status/error messages are announced using suitable live region patterns.
- No critical violations in automated accessibility checks (`pnpm run test:a11y`).
- Lint and tests pass in CI for the modified area.
- If user flow is critical, a manual keyboard and screen reader spot-check is recorded.
