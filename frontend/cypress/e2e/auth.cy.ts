describe('Authentication Tests', () => {
  it('registers user and redirects to email verification page', () => {
    cy.stubTurnstile()
    cy.intercept('POST', '/api/auth/register', {
      statusCode: 200,
      body: {
        accessToken: 'mock-access-token',
        refreshToken: 'mock-refresh-token',
      },
    }).as('registerRequest')

    cy.fixture('user').then((user) => {
      cy.visit('/registrer')
      cy.get('input[name="firstName"]').type(user.registration.firstName)
      cy.get('input[name="lastName"]').type(user.registration.lastName)
      cy.get('input[name="email"]').type(user.registration.email)
      cy.get('input[name="password"]').type(user.registration.password)
      cy.get('input[name="confirmPassword"]').type(user.registration.password)
      cy.get('input#acceptedPrivacyPolicy').check({ force: true })
      cy.get('button[type="submit"]').click()

      cy.wait('@registerRequest').then((interception) => {
        expect(interception.request.body).to.include({
          firstName: user.registration.firstName,
          lastName: user.registration.lastName,
          email: user.registration.email,
        })
      })
      cy.url().should('include', '/bekreft-e-post')
    })
  })

  it('shows validation feedback for invalid registration email', () => {
    cy.stubTurnstile()
    cy.visit('/registrer')

    cy.get('input[name="firstName"]').type('Test')
    cy.get('input[name="lastName"]').type('User')
    cy.get('input[name="email"]').type('not-an-email')
    cy.get('input[name="password"]').type('Password123!')
    cy.get('input[name="confirmPassword"]').type('Password123!')
    cy.get('input#acceptedPrivacyPolicy').check({ force: true })
    cy.get('button[type="submit"]').click()

    cy.contains('Ugyldig e-post').should('be.visible')
  })

  it('shows duplicate-email error from backend', () => {
    cy.stubTurnstile()
    cy.intercept('POST', '/api/auth/register', {
      statusCode: 409,
      body: {
        message: 'E-post finnes allerede',
      },
    }).as('registerConflict')

    cy.visit('/registrer')
    cy.get('input[name="firstName"]').type('Test')
    cy.get('input[name="lastName"]').type('User')
    cy.get('input[name="email"]').type('already.used@example.com')
    cy.get('input[name="password"]').type('Password123!')
    cy.get('input[name="confirmPassword"]').type('Password123!')
    cy.get('input#acceptedPrivacyPolicy').check({ force: true })
    cy.get('button[type="submit"]').click()

    cy.wait('@registerConflict')
    cy.contains('E-postadressen er allerede registrert').should('be.visible')
  })

  it('logs in and respects redirect query parameter', () => {
    cy.intercept('POST', '/api/auth/login', {
      statusCode: 200,
      body: {
        accessToken: 'mock-access-token',
        refreshToken: 'mock-refresh-token',
      },
    }).as('loginRequest')
    cy.mockAuthMe()
    cy.mockCoreHouseholdData()
    cy.mockInventoryData()

    cy.visit('/logg-inn?redirect=/husstand/beredskapslager')
    cy.get('input[type="email"]').type('test.user@example.com')
    cy.get('input[name="password"]').type('Password123!')
    cy.get('button[type="submit"]').click()

    cy.wait('@loginRequest')
    cy.url().should('include', '/husstand/beredskapslager')
    cy.contains('Beredskapslager').should('be.visible')
  })

  it('stays on login view when credentials are rejected', () => {
    cy.intercept('POST', '/api/auth/login', {
      statusCode: 401,
      body: {
        message: 'Invalid credentials',
      },
    }).as('loginRequest')

    cy.visit('/logg-inn')
    cy.get('input[type="email"]').type('wrong.user@example.com')
    cy.get('input[name="password"]').type('WrongPass123!')
    cy.get('button[type="submit"]').click()

    cy.wait('@loginRequest')
    cy.url().should('include', '/logg-inn')
    cy.contains('Innlogging').should('be.visible')
  })
})
