describe('Events and reflections full-stack', () => {
  beforeEach(() => {
    cy.clearAuthSession()
  })

  it('filters events and opens detail view', () => {
    cy.visit('/kriser')
    cy.contains('Kriser og hendelser').should('be.visible')
    cy.get('[data-testid="kriser-tab-ongoing"]').click()
    cy.get('a[href^="/kriser/"]').should('have.length.greaterThan', 0)
    cy.get('a[href^="/kriser/"]').first().click()
    cy.url().should('match', /\/kriser\/\d+$/)
    cy.get('h1').should('be.visible')
  })

  it('navigates from event detail to my reflections', () => {
    cy.loginAsSeededUser()
    cy.visit('/kriser')
    cy.get('[data-testid="kriser-tab-finished"]').click()
    cy.get('a[href^="/kriser/"]').first().click()
    cy.contains('Refleksjoner').should('be.visible')
    cy.contains('button', 'Legg til Refleksjon').click()
    cy.contains('Skriv en ny refleksjon').should('be.visible')
  })
})
