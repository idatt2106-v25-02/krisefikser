describe('Profile settings full-stack', () => {
  beforeEach(() => {
    cy.clearAuthSession()
    cy.loginAsSeededUser()
    cy.visit('/dashboard')
  })

  it('opens profile editing and shows validation feedback', () => {
    cy.contains('Personlig informasjon').should('be.visible')
    cy.contains('button', 'Rediger').click()
    cy.get('#firstName').clear()
    cy.contains('button', 'Lagre endringer').click()
    cy.contains('Fornavn er påkrevd').should('be.visible')
    cy.contains('button', 'Avbryt').click()
  })

  it('navigates to change password view', () => {
    cy.contains('Sikkerhet').should('be.visible')
    cy.contains('button', 'Endre passord').click()
    cy.url().should('include', '/endre-passord')
    cy.contains('Oppdater Passord').should('be.visible')
  })
})
