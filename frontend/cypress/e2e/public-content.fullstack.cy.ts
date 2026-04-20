describe('Public content full-stack', () => {
  it('navigates scenario list to detail', () => {
    cy.visit('/scenarioer')
    cy.contains('Alle krisescenarioer').should('be.visible')
    cy.get('div[class*="cursor-pointer"]').first().click()
    cy.url().should('match', /\/scenario\/\d+$/)
  })

  it('navigates news list to article', () => {
    cy.visit('/nyheter')
    cy.contains('Nyheter').should('be.visible')
    cy.get('div[class*="cursor-pointer"]').first().click()
    cy.url().should('match', /\/artikkel\/\d+$/)
  })

  it('loads map route shell', () => {
    cy.visit('/kart')
    cy.url().should('include', '/kart')
    cy.get('body').should('not.contain', 'Siden ble ikke funnet')
  })
})
