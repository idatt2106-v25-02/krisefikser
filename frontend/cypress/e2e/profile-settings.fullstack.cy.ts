describe('Profile settings full-stack', () => {
  beforeEach(() => {
    cy.clearAuthSession()
    cy.loginAsSeededUser()
    cy.visit('/dashboard')
  })

  it('opens profile editing and shows validation feedback', () => {
    cy.contains('h2', 'Personlig informasjon')
      .should('be.visible')
      .closest('div.bg-white')
      .as('personalInfoCard')

    cy.get('@personalInfoCard').within(() => {
      cy.contains('button', 'Rediger').click()
      cy.get('#firstName').should('be.enabled').clear()
      cy.contains('button', 'Lagre endringer').click()
      cy.contains('Fornavn er påkrevd').should('be.visible')
      cy.contains('button', 'Avbryt').click()
    })
  })

  it('navigates to change password view', () => {
    cy.contains('Sikkerhet').should('be.visible')
    cy.contains('button', 'Endre passord').click()
    cy.url().should('include', '/endre-passord')
    cy.contains('Oppdater Passord').should('be.visible')
  })
})
