describe('Emne og teknologi (IDATT2106)', () => {
  it('viser hovedoverskrift og hovedspor-kort', () => {
    cy.visit('/emne-og-teknologi', {
      onBeforeLoad(win) {
        win.localStorage.clear()
      },
    })
    cy.contains('button', 'Kun nødvendige').click()
    cy.get('h1').should('contain.text', 'Emne, krav og teknologi')
    cy.contains('h2', 'Hovedspor i løsningen').scrollIntoView()
    cy.contains('h2', 'Hovedspor i løsningen').should('be.visible')
    cy.contains('a', 'Åpne kart').should('be.visible')
  })
})
