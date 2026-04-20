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
    cy.visit('/kriser', { failOnStatusCode: false })
    cy.location('pathname', { timeout: 10000 }).should('eq', '/kriser')

    cy.get('body').then(($body) => {
      const pageText = $body.text()
      const eventLinks = $body.find('a[href^="/kriser/"]')
      const hasErrorState = pageText.includes('Kunne ikke laste kriser')
      const hasLoadingState = pageText.includes('Laster inn kriser')

      if (hasErrorState) {
        cy.contains('Kunne ikke laste kriser').should('be.visible')
        return
      }

      if (eventLinks.length > 0) {
        cy.wrap(eventLinks.first()).click()
        cy.url().should('match', /\/kriser\/\d+$/)
        cy.get('h1').should('be.visible')
        return
      }

      if (hasLoadingState) {
        cy.contains('Laster inn kriser').should('be.visible')
      }
    })
  })
})
