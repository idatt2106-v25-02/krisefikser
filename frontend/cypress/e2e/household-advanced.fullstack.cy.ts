describe('Household advanced full-stack', () => {
  beforeEach(() => {
    cy.clearAuthSession()
  })

  it('loads household page for authenticated user', () => {
    cy.loginAsSeededUser()
    cy.visit('/husstand')

    cy.location('pathname', { timeout: 20000 }).should((pathname) => {
      expect(pathname === '/husstand' || pathname === '/bli-med-eller-opprett-husstand').to.eq(true)
    })

    cy.location('pathname').then((pathname) => {
      if (pathname === '/bli-med-eller-opprett-husstand') {
        cy.contains('Kom i gang med husstand').should('be.visible')
      } else {
        cy.get('body').then(($body) => {
          if ($body.text().includes('Medlemmer og gjester')) {
            cy.contains('Medlemmer og gjester').should('be.visible')
            return
          }
          cy.contains('Medlemmer og gjester', { timeout: 15000 }).should('be.visible')
        })
      }
      cy.get('body').should('not.contain', 'Kunne ikke laste husstandsdata')
    })
  })

  it('redirects unauthenticated user to login for protected household route', () => {
    cy.visit('/husstand')
    cy.url().should('include', '/logg-inn')
    cy.url().should('include', 'redirect=')
  })
})
