describe('Authentication Tests', () => {
  /**
   * Test the user registration process.
   * It intercepts the registration API call, visits the registration page,
   * fills out the form, submits it, and verifies the API call was made.
   */
  it('registrering', function() {
    // Intercept the registration API call
    cy.intercept('POST', '/api/auth/register', {
      statusCode: 200,
      body: {
        accessToken: 'mock-access-token',
        refreshToken: 'mock-refresh-token'
      }
    }).as('registerRequest');

    cy.fixture('user').then((user) => {
      cy.visit('/registrer', {
        onBeforeLoad(win) {
          // Stub the turnstile render function
          // @ts-expect-error - Overwriting window property for test purposes
          win.turnstile = {
            render: (selector: string, options: { callback: (token: string) => void }) => {
              // Immediately call the callback with a mock token
              options.callback('mock-captcha-token');
              // Return a dummy widget ID
              return 'dummy-widget-id';
            },
            // Add other methods if your app uses them (e.g., remove, reset)
            remove: () => {},
            reset: () => {},
          };
        },
      });
      cy.get('input[name="firstName"]').clear();
      cy.get('input[name="firstName"]').type(user.registration.firstName);
      cy.get('input[name="lastName"]').clear();
      cy.get('input[name="lastName"]').type(user.registration.lastName);
      cy.get('input[name="email"]').clear();
      cy.get('input[name="email"]').type(user.registration.email);
      cy.get('input[name="password"]').clear();
      cy.get('input[name="password"]').type(user.registration.password);
      cy.get('input[name="confirmPassword"]').clear();
      cy.get('input[name="confirmPassword"]').type(user.registration.password); // Use the same password for confirmation
      cy.get('input[type="checkbox"]').click();
      // Click the submit button
      cy.get('button[type="submit"]').click();

      // Wait for the intercepted request to ensure it was called
      cy.wait('@registerRequest');
    });
  });

  /**
   * Test the user login process.
   * It intercepts the login API call, visits the login page,
   * fills out the email and password, submits the form,
   * and verifies the API call was made.
   */
  it('logg-inn', function() {
    // Intercept the login API call
    cy.intercept('POST', '/api/auth/login', {
      statusCode: 200,
      body: {
        accessToken: 'mock-access-token',
        refreshToken: 'mock-refresh-token'
      }
    }).as('loginRequest');

    cy.fixture('user').then((user) => {
      cy.visit('/logg-inn');

      cy.get('input[type="email"]').clear();
      cy.get('input[type="email"]').type(user.login.email);
      cy.get('input[type="password"]').clear();
      cy.get('input[type="password"]').type(user.login.password);

      // Click the submit button
      cy.get('button[type="submit"]').click();

      // Wait for the intercepted request to ensure it was called
      cy.wait('@loginRequest');
    });
  });
})
