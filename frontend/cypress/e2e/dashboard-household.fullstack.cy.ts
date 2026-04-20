describe('Dashboard and household navigation full-stack', () => {
  beforeEach(() => {
    cy.clearAuthSession()
    cy.loginAsSeededUser()
  })

  it('shows seeded profile data on dashboard from real /me', () => {
    cy.visit('/dashboard')
    cy.contains('Min profil').should('be.visible')
    cy.get('#firstName', { timeout: 20000 }).should('have.value', 'Brotherman')
    cy.get('#lastName').should('have.value', 'Testern')
    cy.get('#email').should('have.value', 'brotherman@testern.no')
    cy.contains('Personlig informasjon').should('be.visible')
    cy.contains('Kunne ikke laste brukerdata').should('not.exist')
  })

  it('navigates from dashboard to household via Mine husstander', () => {
    cy.visit('/dashboard')
    cy.contains('Min profil').should('be.visible')

    cy.contains('a', 'Mine husstander').click()
    cy.url().should('include', '/husstand')
    cy.get('body').should('not.contain', 'Kunne ikke laste husstandsdata')
  })

  it('loads household reflections page without error banner', () => {
    cy.visit('/husstand/refleksjoner')
    cy.contains('Husstandens Refleksjoner').should('be.visible')
    cy.contains('Kunne ikke laste refleksjoner:').should('not.exist')
  })
})
