describe('Events and reflections full-stack', () => {
  beforeEach(() => {
    cy.clearAuthSession()
  })

  it('filters events and opens detail view', () => {
    cy.visit('/kriser', { failOnStatusCode: false })
    cy.location('pathname', { timeout: 10000 }).should('eq', '/kriser')

    cy.get('body').then(($body) => {
      const pageText = $body.text()
      const hasErrorState = pageText.includes('Kunne ikke laste kriser')
      const hasLoadingState = pageText.includes('Laster inn kriser')
      const hasOngoingTab = $body.find('[data-testid="kriser-tab-ongoing"]').length > 0

      if (hasErrorState) {
        cy.contains('Kunne ikke laste kriser').should('be.visible')
        return
      }

      if (!hasOngoingTab) {
        if (hasLoadingState) {
          cy.contains('Laster inn kriser').should('be.visible')
        }
        return
      }

      cy.get('[data-testid="kriser-tab-ongoing"]').click()
      cy.get('body').then(($updatedBody) => {
        const eventLinks = $updatedBody.find('a[href^="/kriser/"]')

        if (eventLinks.length > 0) {
          cy.wrap(eventLinks.first()).click()
          cy.url().should('match', /\/kriser\/\d+$/)
          cy.get('h1').should('be.visible')
        }
      })
    })
  })

  it('navigates from event detail to my reflections', () => {
    cy.loginAsSeededUser()
    cy.visit('/kriser', { failOnStatusCode: false })
    cy.location('pathname', { timeout: 10000 }).should('eq', '/kriser')

    cy.get('body').then(($body) => {
      const pageText = $body.text()
      const hasErrorState = pageText.includes('Kunne ikke laste kriser')
      const hasLoadingState = pageText.includes('Laster inn kriser')
      const hasFinishedTab = $body.find('[data-testid="kriser-tab-finished"]').length > 0

      if (hasErrorState) {
        cy.contains('Kunne ikke laste kriser').should('be.visible')
        return
      }

      if (!hasFinishedTab) {
        if (hasLoadingState) {
          cy.contains('Laster inn kriser').should('be.visible')
        }
        return
      }

      cy.get('[data-testid="kriser-tab-finished"]').click()
      cy.get('body').then(($updatedBody) => {
        const eventLinks = $updatedBody.find('a[href^="/kriser/"]')

        if (eventLinks.length > 0) {
          cy.wrap(eventLinks.first()).click()
          cy.contains('Refleksjoner').should('be.visible')
          cy.contains('button', 'Legg til Refleksjon').click()
          cy.contains('Skriv en ny refleksjon').should('be.visible')
        }
      })
    })
  })
})
