## Manual Accessibility Verification

Use this checklist on critical flows before release.

### Keyboard-only checks

- Navigate from browser chrome to page content using `Tab` and `Shift+Tab`.
- Verify skip link appears and moves focus to `#main-content`.
- Verify all menus, dialogs, and action buttons are reachable and operable.
- Verify no keyboard trap in dialogs and that `Escape` closes modals.

### Reflow and zoom checks

- Test at 200% zoom on critical routes (`/`, `/registrer`, `/dashboard`, `/admin/brukere`).
- Test at 320 CSS px width and verify no required two-dimensional scrolling.

### Contrast checks

- Verify text/background contrast for body text and key actions.
- Verify focus indicator is visible on interactive elements.
- Verify status and error messaging is not color-only.

### Screen reader spot checks (NVDA/VoiceOver)

- Page title and language are announced correctly.
- Main navigation links have understandable names and current page indication.
- Form fields announce label, required/error states, and messages.
- Dialog opening announces title/message and keeps focus inside dialog.
- Table headers and action buttons are announced with meaningful names.

### Evidence to capture

- Route tested
- Date/tester
- Browser + assistive tech
- Pass/fail + notes
