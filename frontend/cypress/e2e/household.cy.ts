describe('Household flow', () => {
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

    cy.intercept('GET', '/api/households/active', {
      statusCode: 200,
      body: {
        id: 'household-1',
        name: 'Testhusstand',
        address: 'Eksempelvei 1',
        members: [],
        guests: [],
      },
    }).as('getActiveHousehold')
  })

  it('loads household page with mocked active household', () => {
    cy.visit('/husholdning')
    cy.wait('@getMe')
    cy.wait('@getActiveHousehold')
    cy.contains(/hushold|husstand/i).should('exist')
  })
})
