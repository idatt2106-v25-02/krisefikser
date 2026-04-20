describe('Events and reflections full-stack', () => {
  beforeEach(() => {
    cy.clearAuthSession()
  })

  it('filters events and opens detail view', () => {
    cy.visit('/kriser')
    cy.contains('Kriser og hendelser', { timeout: 10000 }).should('be.visible')
    cy.get('[data-testid="kriser-tab-ongoing"]').click()

    cy.get('body').then(($body) => {
      const eventLinks = $body.find('a[href^="/kriser/"]')

      if (eventLinks.length > 0) {
        cy.wrap(eventLinks.first()).click()
        cy.url().should('match', /\/kriser\/\d+$/)
        cy.get('h1').should('be.visible')
      } else {
        cy.contains('Ingen hendelser funnet i denne kategorien.').should('be.visible')
      }
    })
  })

  it('navigates from event detail to my reflections', () => {
    cy.loginAsSeededUser()
    cy.visit('/kriser')
    cy.contains('Kriser og hendelser', { timeout: 10000 }).should('be.visible')
    cy.get('[data-testid="kriser-tab-finished"]').click()

    cy.get('body').then(($body) => {
      const eventLinks = $body.find('a[href^="/kriser/"]')

      if (eventLinks.length > 0) {
        cy.wrap(eventLinks.first()).click()
        cy.contains('Refleksjoner').should('be.visible')
        cy.contains('button', 'Legg til Refleksjon').click()
        cy.contains('Skriv en ny refleksjon').should('be.visible')
      } else {
        cy.contains('Ingen hendelser funnet i denne kategorien.').should('be.visible')
      }
    })
  })
})
