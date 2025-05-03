/// <reference types="cypress" />
// ***********************************************
// This example commands.ts shows you how to
// create various custom commands and overwrite
// existing commands.
//
// For more comprehensive examples of custom
// commands please read more here:
// https://on.cypress.io/custom-commands
// ***********************************************
//
//
// -- This is a parent command --
// Cypress.Commands.add('login', (email, password) => { ... })
//
//
// -- This is a child command --
// Cypress.Commands.add('drag', { prevSubject: 'element'}, (subject, options) => { ... })
//
//
// -- This is a dual command --
// Cypress.Commands.add('dismiss', { prevSubject: 'optional'}, (subject, options) => { ... })
//
//
// -- This will overwrite an existing command --
// Cypress.Commands.overwrite('visit', (originalFn, url, options) => { ... })
//
// declare global {
//   namespace Cypress {
//     interface Chainable {
//       login(email: string, password: string): Chainable<void>
//       drag(subject: string, options?: Partial<TypeOptions>): Chainable<Element>
//       dismiss(subject: string, options?: Partial<TypeOptions>): Chainable<Element>
//       visit(originalFn: CommandOriginalFn, url: string, options: Partial<VisitOptions>): Chainable<Element>
//     }
//   }
// }

declare global {
  namespace Cypress {
    interface Chainable {
      registerUser(email: string, password: string, name: string): Chainable<void>
      loginUser(email: string, password: string): Chainable<void>
      deleteTestUser(email: string): Chainable<void>
    }
  }
}

// Custom command for user registration
Cypress.Commands.add('registerUser', (email: string, password: string, name: string) => {
  cy.request({
    method: 'POST',
    url: '/api/auth/register',
    body: {
      email,
      password,
      name
    },
    failOnStatusCode: false // Don't fail if user already exists
  })
})

// Custom command for user login
Cypress.Commands.add('loginUser', (email: string, password: string) => {
  cy.request({
    method: 'POST',
    url: '/api/auth/login',
    body: {
      email,
      password
    }
  }).then((response) => {
    // Store the token in localStorage or cookies as needed
    if (response.body.token) {
      window.localStorage.setItem('token', response.body.token)
    }
  })
})

// Custom command to delete test user (cleanup)
Cypress.Commands.add('deleteTestUser', (email: string) => {
  cy.request({
    method: 'DELETE',
    url: `/api/auth/users/${email}`,
    headers: {
      Authorization: `Bearer ${window.localStorage.getItem('token')}`
    },
    failOnStatusCode: false
  })
})

export {}
