describe('Inventory flow', () => {
  beforeEach(() => {
    localStorage.setItem('accessToken', 'mock-test-access-token')
    localStorage.setItem('refreshToken', 'mock-test-refresh-token')

    cy.intercept('GET', '/api/auth/me', {
      statusCode: 200,
      body: {
        id: 'user-123',
        firstName: 'Test',
        lastName: 'User',
        email: 'test.user@example.com',
        roles: ['USER'],
        notifications: true,
        emailUpdates: false,
        locationSharing: true,
      },
    }).as('getMe')

    cy.intercept('GET', '/api/items/summary', {
      statusCode: 200,
      body: [],
    }).as('getInventorySummary')
  })

  it('loads inventory page and performs search input interaction', () => {
    cy.visit('/inventar')
    cy.wait('@getMe')
    cy.wait('@getInventorySummary')
    cy.get('input[placeholder*="Søk"]').first().type('Hermetikk')
  })
})
