describe('Full-stack smoke', () => {
  beforeEach(() => {
    cy.clearAuthSession()
  })

  it('logs in as seeded user and reaches dashboard', () => {
    cy.loginAsSeededUser()
    cy.visit('/dashboard')
    cy.contains('Min profil').should('be.visible')
  })

  it('loads household inventory with real backend data', () => {
    cy.loginAsSeededUser()
    cy.visit('/husstand/beredskapslager')
    cy.url().should('include', '/husstand/beredskapslager')
    cy.contains('Beredskapslager').should('be.visible')
  })

  it('loads crisis overview and opens first event', () => {
    cy.visit('/kriser')
    cy.contains('Kriser og hendelser').should('be.visible')
    cy.get('a[href^="/kriser/"]').first().click()
    cy.url().should('match', /\/kriser\/\d+$/)
    cy.get('h1').should('be.visible')
  })
})
