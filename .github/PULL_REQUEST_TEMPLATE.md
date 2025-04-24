---
name: "🔀 Pull Request"
description: "Opprett en PR for kodeendringer basert på en Task-issue"

body:
  - type: markdown
    attributes:
      value: |
        ## 📌 Sammendrag
        <!-- Kort oppsummering av endringen -->

  - type: input
    id: scope
    attributes:
      label: "Scope"
      description: "Følg `<type>/<scope>`, f.eks. `feat/backend/auth`"
      placeholder: "<type>/<scope>"
    validations:
      required: true

  - type: input
    id: issue
    attributes:
      label: "Issue #"
      description: "Referer den tilhørende Task-issue"
      placeholder: "#123"
    validations:
      required: true

  - type: textarea
    id: summary
    attributes:
      label: "Hva er gjort?"
      description: "Kort beskrivelse av implementasjonen"
      placeholder: |
        - Nye API-endepunkter for login
        - Oppdatering av UI-komponenter for dashboard
    validations:
      required: true

  - type: textarea
    id: acceptance
    attributes:
      label: "Akseptansekriterier"
      description: "Kopier sjekklistene fra Task-issue"
      placeholder: |
        - [ ] Kriterium 1
        - [ ] Kriterium 2
    validations:
      required: true

  - type: textarea
    id: testing
    attributes:
      label: "Hvordan teste"
      description: "Steg for å verifisere endringen lokalt"
      placeholder: |
        1. `npm install && npm test`
        2. Kjør `curl` mot /login-endpoint

  - type: markdown
    attributes:
      value: |
        ## 📸 Skjermbilder / Demo
        <!-- Legg til hvis relevant -->

---
