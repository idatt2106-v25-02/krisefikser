describe('Dashboard flow', () => {
  beforeEach(() => {
    cy.mockAuthMe()
    cy.mockCoreHouseholdData()
  })

  function visitDashboard(): void {
    cy.visit('/dashboard', {
      onBeforeLoad(win) {
        win.localStorage.setItem('accessToken', 'mock-test-access-token')
        win.localStorage.setItem('refreshToken', 'mock-test-refresh-token')
      },
    })
  }

  it('loads dashboard and renders user profile details', () => {
    visitDashboard()
    cy.wait('@getMe')
    cy.wait('@getAllHouseholds')

    cy.contains('Personlig informasjon').should('be.visible')
    cy.get('#firstName').should('have.value', 'Test')
    cy.get('#lastName').should('have.value', 'User')
    cy.get('#email').should('have.value', 'test.user@example.com')
    cy.contains('Innstillinger').should('be.visible')
    cy.contains('Mine husstander').should('be.visible')
    cy.contains('Sikkerhet').should('be.visible')
  })
})
