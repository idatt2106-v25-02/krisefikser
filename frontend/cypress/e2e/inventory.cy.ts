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

    cy.intercept('GET', '/api/items/summary*', {
      statusCode: 200,
      body: {
        kcal: 2500,
        kcalGoal: 14000,
        waterLiters: 14,
        waterLitersGoal: 14,
        checkedItems: 3,
        totalItems: 10,
      },
    }).as('getInventorySummary')

    cy.intercept('GET', '/api/items/food*', {
      statusCode: 200,
      body: [],
    }).as('getFoodItems')

    cy.intercept('GET', '/api/items/checklist*', {
      statusCode: 200,
      body: [],
    }).as('getChecklistItems')
  })

  it('loads inventory page and performs search input interaction', () => {
    cy.visit('/husstand/beredskapslager')
    cy.wait('@getMe')
    cy.wait('@getActiveHousehold')
    cy.wait('@getInventorySummary')
    cy.wait('@getFoodItems')
    cy.wait('@getChecklistItems')
    cy.get('input[placeholder*="Søk"]').first().type('Hermetikk')
  })
})
