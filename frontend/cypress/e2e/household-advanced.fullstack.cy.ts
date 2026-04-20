describe('Household advanced full-stack', () => {
  beforeEach(() => {
    cy.clearAuthSession()
  })

  it('loads household page for authenticated user', () => {
    cy.loginAsSeededUser()
    cy.visit('/husstand')
    cy.url().should('include', '/husstand')
    cy.get('body').should('not.contain', 'Kunne ikke laste husstandsdata')
  })

  it('redirects unauthenticated user to login for protected household route', () => {
    cy.visit('/husstand')
    cy.url().should('include', '/logg-inn')
    cy.url().should('include', 'redirect=')
  })
})
