describe('Login form full-stack', () => {
  beforeEach(() => {
    cy.clearAuthSession()
  })

  it('shows error message for wrong password against real backend', () => {
    cy.visit('/logg-inn')
    cy.contains('h1', 'Innlogging').should('be.visible')

    cy.get('input[type="email"]').type('brotherman@testern.no')
    cy.get('input[name="password"]').type('wrong-password-not-real')
    cy.contains('button', 'Logg inn').click()

    cy.contains('Feil e-post eller passord', { timeout: 15000 }).should('be.visible')
    cy.url().should('include', '/logg-inn')
  })

  it('logs in via form and lands on dashboard', () => {
    cy.visit('/logg-inn?redirect=/dashboard')
    cy.get('input[type="email"]').type('brotherman@testern.no')
    cy.get('input[name="password"]').type('password')
    cy.contains('button', 'Logg inn').click()

    cy.url({ timeout: 20000 }).should('include', '/dashboard')
    cy.contains('Min profil').should('be.visible')
  })
})
