describe('Emne og teknologi (IDATT2106)', () => {
  it('viser hovedoverskrift', () => {
    cy.visit('/emne-og-teknologi', {
      onBeforeLoad(win) {
        win.localStorage.clear()
      },
    })
    cy.contains('button', 'Kun nødvendige').click()
    cy.get('h1').should('contain.text', 'Emne, krav og teknologi')
  })
})
