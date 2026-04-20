describe('Public content full-stack', () => {
  it('navigates scenario list to detail', () => {
    cy.visit('/scenarioer')
    cy.contains('Alle krisescenarioer').should('be.visible')
    cy.contains('div', 'Les mer').first().should('be.visible').click()
    cy.url().should('include', '/scenario/')
    cy.url().should('not.include', '/scenario/undefined')
  })

  it('navigates news list to article', () => {
    cy.visit('/nyheter')
    cy.contains('Nyheter').should('be.visible')
    cy.contains('div', 'Les mer').first().should('be.visible').click()
    cy.url().should('include', '/artikkel/')
    cy.url().should('not.include', '/artikkel/undefined')
  })

  it('loads map route shell', () => {
    cy.visit('/kart')
    cy.url().should('include', '/kart')
    cy.get('body').should('not.contain', 'Siden ble ikke funnet')
  })
})
