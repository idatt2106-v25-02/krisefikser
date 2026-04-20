describe('Auth recovery and verification flows', () => {
  beforeEach(() => {
    cy.clearLocalStorage()
  })

  it('requests password reset with valid email', () => {
    cy.intercept('POST', '/api/auth/request-password-reset', {
      statusCode: 200,
      body: {
        message: 'Password reset link sent',
      },
    }).as('requestPasswordReset')

    cy.visit('/glemt-passord')
    cy.get('input[type="email"]').type('test.user@example.com')
    cy.contains('button', 'Tilbakestill Passord').click()

    cy.wait('@requestPasswordReset')
      .its('request.body')
      .should('include', { email: 'test.user@example.com' })

    cy.contains('Vi har sendt en lenke for å tilbakestille passordet til').should('be.visible')
  })

  it('shows validation error for invalid email on forgot-password page', () => {
    cy.intercept('POST', '/api/auth/request-password-reset').as('requestPasswordReset')

    cy.visit('/glemt-passord')
    cy.get('input[type="email"]').type('ikke-en-epost')
    cy.contains('button', 'Tilbakestill Passord').click()

    cy.contains('Vennligst skriv inn en gyldig e-postadresse').should('be.visible')
    cy.get('@requestPasswordReset.all').should('have.length', 0)
  })

  it('redirects to forgot-password when reset-password token is missing', () => {
    cy.visit('/reset-passord')
    cy.url().should('include', '/glemt-passord')
  })

  it('completes password reset when token is present', () => {
    cy.intercept('POST', '/api/auth/complete-password-reset', {
      statusCode: 200,
      body: {},
    }).as('completePasswordReset')

    cy.visit('/reset-passord?token=valid-reset-token')
    cy.get('input[name="password"]').type('NyttPassord123!')
    cy.get('input[name="confirmPassword"]').type('NyttPassord123!')
    cy.contains('button', 'Tilbakestill Passord').click()

    cy.wait('@completePasswordReset')
      .its('request.body')
      .should('deep.include', {
        token: 'valid-reset-token',
        newPassword: 'NyttPassord123!',
      })

    cy.contains('Passordet ditt har blitt tilbakestilt.').should('be.visible')
  })

  it('verifies email token and allows navigation to login', () => {
    cy.intercept('POST', '/api/auth/verify-email*', {
      statusCode: 200,
      body: 'Verified',
    }).as('verifyEmail')

    cy.visit('/verify?token=valid-email-token')
    cy.wait('@verifyEmail')
    cy.contains('E-post verifisert!').should('be.visible')

    cy.contains('button', 'Fortsett til innlogging').click()
    cy.url().should('include', '/logg-inn')
    cy.url().should('include', 'token=valid-email-token')
  })

  it('shows verification failure state when email token is invalid', () => {
    cy.intercept('POST', '/api/auth/verify-email*', {
      statusCode: 400,
      body: 'Invalid token',
    }).as('verifyEmailFailure')

    cy.visit('/verify?token=invalid-email-token')
    cy.wait('@verifyEmailFailure')
    cy.contains('Verifisering feilet').should('be.visible')
    cy.contains('Token er ugyldig, utløpt, eller noe gikk galt').should('be.visible')
  })
})
