describe('Public content full-stack', () => {
  it('navigates scenario list to detail', () => {
    cy.visit('/scenarioer')
    cy.location('pathname', { timeout: 20000 }).should('eq', '/scenarioer')
    cy.contains('Alle krisescenarioer', { timeout: 20000 }).should('be.visible')
    cy.get('[data-cy=scenario-card]', { timeout: 20000 }).should(($cards) => {
      expect($cards.length).to.be.greaterThan(0)
    })
    cy.get('[data-cy=scenario-card]').first().find('[data-cy=read-more]').click()
    cy.location('pathname', { timeout: 20000 }).should('match', /^\/scenario\/.+/)
    cy.location('pathname').should('not.include', '/scenario/undefined')
  })

  it('navigates news list to article', () => {
    cy.visit('/nyheter')
    cy.location('pathname', { timeout: 20000 }).should('eq', '/nyheter')
    cy.contains('Nyheter', { timeout: 20000 }).should('be.visible')
    cy.get('[data-cy=news-card]', { timeout: 20000 }).should(($cards) => {
      expect($cards.length).to.be.greaterThan(0)
    })
    cy.get('[data-cy=news-card]').first().find('[data-cy=read-more]').click()
    cy.location('pathname', { timeout: 20000 }).should('match', /^\/artikkel\/.+/)
    cy.location('pathname').should('not.include', '/artikkel/undefined')
  })

  it('loads map route shell', () => {
    cy.visit('/kart')
    cy.url().should('include', '/kart')
    cy.get('body').should('not.contain', 'Siden ble ikke funnet')
  })
})
