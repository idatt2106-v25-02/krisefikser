describe('Notifications full-stack', () => {
  beforeEach(() => {
    cy.clearAuthSession()
    cy.loginAsSeededUser()
  })

  it('loads notifications and opens detail link when available', () => {
    cy.visit('/varsler')
    cy.contains('Varsler').should('be.visible')
    cy.get('body', { timeout: 15000 }).should(($body) => {
      const text = $body.text()
      if (text.includes('Kunne ikke laste varsler')) {
        throw new Error('Notifications request failed (error state visible)')
      }
      const settled =
        text.includes('Ingen varsler å vise') || text.includes('Vis detaljer')
      if (!settled) {
        throw new Error('Expected empty list or notification cards with detail link after loading')
      }
    })
    cy.get('body').then(($body) => {
      if ($body.text().includes('Ingen varsler å vise')) {
        cy.contains('Ingen varsler å vise').should('be.visible')
        return
      }
      cy.contains('Vis detaljer').first().click()
      cy.url().should('match', /\/varsler\/[^/]+$/)
    })
  })

  it('switches filters and marks notifications as read when present', () => {
    cy.visit('/varsler')
    cy.contains('Varsler').should('be.visible')
    cy.get('body', { timeout: 15000 }).should(($body) => {
      const text = $body.text()
      if (text.includes('Kunne ikke laste varsler')) {
        throw new Error('Notifications request failed (error state visible)')
      }
      const settled =
        text.includes('Ingen varsler å vise') || text.includes('Vis detaljer')
      if (!settled) {
        throw new Error('Expected empty list or notification cards before filter interactions')
      }
    })
    cy.get('[data-testid="notifications-filter-unread"]').click()
    cy.get('[data-testid="notifications-filter-crisis"]').click()
    cy.get('[data-testid="notifications-filter-expiry"]').click()
    cy.get('[data-testid="notifications-filter-update"]').click()
    cy.get('[data-testid="notifications-filter-all"]').click()

    cy.get('body').then(($body) => {
      if ($body.text().includes('Marker alle som lest')) {
        cy.get('[data-testid="notifications-mark-all-read"]').click()
      }
    })
  })
})
