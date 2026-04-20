describe('Critical smoke flow', () => {
  it('logs in and lands on dashboard', () => {
    cy.intercept('POST', '/api/auth/login', {
      statusCode: 200,
      body: {
        accessToken: 'mock-access-token',
        refreshToken: 'mock-refresh-token',
      },
    }).as('loginRequest')

    cy.mockAuthMe()
    cy.mockCoreHouseholdData()

    cy.visit('/logg-inn')
    cy.get('input[type="email"]').type('test.user@example.com')
    cy.get('input[name="password"]').type('Password123!')
    cy.get('button[type="submit"]').click()

    cy.wait('@loginRequest')
    cy.wait('@getMe')
    cy.url().should('include', '/dashboard')
    cy.contains('Personlig informasjon').should('be.visible')
  })

  it('loads household page with active household data', () => {
    cy.mockAuthMe()
    cy.mockCoreHouseholdData()

    cy.visit('/husstand', {
      onBeforeLoad(win) {
        win.localStorage.setItem('accessToken', 'mock-test-access-token')
        win.localStorage.setItem('refreshToken', 'mock-test-refresh-token')
      },
    })

    cy.wait('@getMe')
    cy.wait('@getActiveHousehold')
    cy.contains('Testhusstand').should('be.visible')
    cy.contains('Husstandsmedlemmer').should('be.visible')
  })

  it('loads inventory page and displays key widgets', () => {
    cy.mockAuthMe()
    cy.mockCoreHouseholdData()
    cy.mockInventoryData()

    cy.visit('/husstand/beredskapslager', {
      onBeforeLoad(win) {
        win.localStorage.setItem('accessToken', 'mock-test-access-token')
        win.localStorage.setItem('refreshToken', 'mock-test-refresh-token')
      },
    })

    cy.wait('@getMe')
    cy.wait('@getActiveHousehold')
    cy.wait('@getInventorySummary')
    cy.contains('Beredskapslager').should('be.visible')
    cy.contains('Oversikt').should('be.visible')
    cy.contains('Produkter').should('be.visible')
  })

  it('updates water amount from inventory page', () => {
    cy.mockAuthMe()
    cy.mockCoreHouseholdData()
    cy.mockInventoryData()
    cy.intercept('PUT', '/api/items/water/*', {
      statusCode: 200,
      body: {},
    }).as('updateWater')

    cy.visit('/husstand/beredskapslager', {
      onBeforeLoad(win) {
        win.localStorage.setItem('accessToken', 'mock-test-access-token')
        win.localStorage.setItem('refreshToken', 'mock-test-refresh-token')
      },
    })

    cy.wait('@getInventorySummary')
    cy.contains('Vann').click()
    cy.get('#waterAddInput').clear()
    cy.get('#waterAddInput').type('1.5')
    cy.contains('Oppdater vannmengde').click()
    cy.wait('@updateWater')
  })

  it('updates a food item and persists via API', () => {
    cy.mockAuthMe()
    cy.mockCoreHouseholdData()
    cy.mockInventoryData()
    cy.intercept('PUT', '/api/items/food/*', {
      statusCode: 200,
      body: {},
    }).as('updateFoodItem')

    cy.visit('/husstand/beredskapslager', {
      onBeforeLoad(win) {
        win.localStorage.setItem('accessToken', 'mock-test-access-token')
        win.localStorage.setItem('refreshToken', 'mock-test-refresh-token')
      },
    })

    cy.wait('@getFoodItems')
    cy.contains('Mat').click()
    cy.get('[title="Rediger matvare"]').first().click()
    cy.get('input[placeholder="Navn"]').clear()
    cy.get('input[placeholder="Navn"]').type('Bygggryn')
    cy.get('[title="Lagre endringer"]').click()

    cy.wait('@updateFoodItem').its('request.body').should('include', { name: 'Bygggryn' })
  })

  it('redirects to join-or-create page when household is missing', () => {
    cy.mockAuthMe()
    cy.intercept('GET', '/api/households/active', {
      statusCode: 404,
      body: {},
    }).as('activeHouseholdNotFound')
    cy.intercept('GET', '/api/households/all', { statusCode: 200, body: [] }).as('getAllHouseholds')
    cy.intercept('GET', '/api/household-invites/pending', { statusCode: 200, body: [] })

    cy.visit('/husstand', {
      onBeforeLoad(win) {
        win.localStorage.setItem('accessToken', 'mock-test-access-token')
        win.localStorage.setItem('refreshToken', 'mock-test-refresh-token')
      },
    })

    cy.wait('@activeHouseholdNotFound')
    cy.url().should('include', '/bli-med-eller-opprett-husstand')
  })
})
