describe('My reflections page full-stack', () => {
  beforeEach(() => {
    cy.clearAuthSession()
    cy.loginAsSeededUser()
  })

  it('loads mine reflections without API error banner', () => {
    cy.visit('/mine-refleksjoner')
    cy.contains('h1', 'Mine Refleksjoner').should('be.visible')
    cy.contains('Her finner du alle refleksjonene du har skrevet').should('be.visible')
    cy.contains('button', 'Ny refleksjon', { timeout: 20000 }).should('be.visible')
    cy.contains('Kunne ikke laste refleksjoner:').should('not.exist')
  })
})
