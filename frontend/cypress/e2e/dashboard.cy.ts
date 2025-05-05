/**
 * Tests for the main Dashboard view.
 * It ensures that the dashboard loads correctly and displays essential user information.
 */
describe('Dashboard View', () => {
  /**
   * Set up tasks before each test in this suite.
   * This includes setting mock authentication tokens in localStorage
   * and intercepting the /api/auth/me endpoint to simulate a logged-in user.
   */
  beforeEach(() => {
    // Set mock auth tokens in localStorage to bypass login for these tests
    localStorage.setItem('accessToken', 'mock-test-access-token');
    localStorage.setItem('refreshToken', 'mock-test-refresh-token');

    // Mock the /api/auth/me endpoint
    cy.intercept('GET', '/api/auth/me', {
      statusCode: 200,
      body: {
        id: 'user-123',
        firstName: 'Test',
        lastName: 'User',
        email: 'test.user@example.com',
        roles: ['USER'], // Add roles if your app uses them
        // Add any other user properties your dashboard might display
        notifications: true,
        emailUpdates: false,
        locationSharing: true,
      }
    }).as('getMe');

    // Mock potential household data endpoint if needed (example)
    // cy.intercept('GET', '/api/households*', { statusCode: 200, body: [] }).as('getHouseholds');
  });

  it('should load the dashboard and display user information', () => {
    // Visit the dashboard page
    cy.visit('/dashboard');

    // Wait for the /api/auth/me call to complete
    cy.wait('@getMe');

    // Add assertions here
    // For example, check if the user's name is displayed (adjust selector based on your component)
    cy.contains('Test User').should('be.visible');

    // Example: Check if the PersonalInfo component rendered something
    cy.contains('Personlig informasjon').should('be.visible');

    // Example: Check if Settings component rendered something
    cy.contains('Innstillinger').should('be.visible');

    // Example: Check if Households component rendered something
    cy.contains('Mine husstander').should('be.visible');

    // Example: Check if Security component rendered something
    cy.contains('Sikkerhet').should('be.visible');
  });

  // Add more tests for specific dashboard functionality as needed
  // For example, testing editing profile info, changing settings, etc.
  // You might need to mock PUT/POST requests for those interactions.
});
