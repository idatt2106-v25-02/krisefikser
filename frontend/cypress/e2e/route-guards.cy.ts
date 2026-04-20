describe('Route guard behaviour', () => {
  beforeEach(() => {
    cy.clearLocalStorage()
  })

  it('redirects unauthenticated users from dashboard to login with redirect query', () => {
    cy.visit('/dashboard')

    cy.url().should('include', '/logg-inn')
    cy.url().should('include', 'redirect=/dashboard')
    cy.contains('Innlogging').should('be.visible')
  })

  it('redirects unauthenticated users from inventory page to login with redirect query', () => {
    cy.visit('/husstand/beredskapslager')

    cy.url().should('include', '/logg-inn')
    cy.url().should('include', 'redirect=/husstand/beredskapslager')
    cy.contains('Innlogging').should('be.visible')
  })

  it('redirects authenticated users away from login page to dashboard', () => {
    cy.mockAuthMe()
    cy.mockCoreHouseholdData()

    cy.visit('/logg-inn', {
      onBeforeLoad(win) {
        win.localStorage.setItem('accessToken', 'mock-test-access-token')
        win.localStorage.setItem('refreshToken', 'mock-test-refresh-token')
      },
    })

    cy.wait('@getMe')
    cy.url().should('include', '/dashboard')
    cy.contains('Personlig informasjon').should('be.visible')
  })

  it('redirects authenticated users away from register page to dashboard', () => {
    cy.mockAuthMe()
    cy.mockCoreHouseholdData()
    cy.intercept('POST', '/api/auth/check-email', {
      statusCode: 200,
      body: { exists: false },
    })

    cy.visit('/registrer', {
      onBeforeLoad(win) {
        win.localStorage.setItem('accessToken', 'mock-test-access-token')
        win.localStorage.setItem('refreshToken', 'mock-test-refresh-token')
      },
    })

    cy.wait('@getMe')
    cy.url().should('include', '/dashboard')
    cy.contains('Innstillinger').should('be.visible')
  })

  it('blocks non-admin users from admin dashboard', () => {
    cy.mockAuthMe({
      roles: ['USER'],
    })

    cy.visit('/admin', {
      onBeforeLoad(win) {
        win.localStorage.setItem('accessToken', 'mock-test-access-token')
        win.localStorage.setItem('refreshToken', 'mock-test-refresh-token')
      },
    })

    cy.wait('@getMe')
    cy.url().should('include', '/admin')
    cy.contains('Siden ble ikke funnet').should('be.visible')
  })
})
