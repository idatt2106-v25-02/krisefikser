/// <reference types="cypress" />

interface MockUser {
  id: string
  firstName: string
  lastName: string
  email: string
  roles: string[]
  notifications: boolean
  emailUpdates: boolean
  locationSharing: boolean
}

interface MockHousehold {
  id: string
  name: string
  address: string
  latitude: number
  longitude: number
  postalCode: string
  city: string
  owner: {
    id: string
    firstName: string
    lastName: string
    email: string
  }
  createdAt: string
  members: Array<{
    id: string
    role: string
    user: {
      id: string
      firstName: string
      lastName: string
      email: string
    }
  }>
  guests: Array<{ id: string; name: string; email: string }>
  meetingPlaces: Array<{
    id: string
    name: string
    address: string
    latitude: number
    longitude: number
    description: string
    type: 'primary' | 'secondary'
    targetDays: number
  }>
  inventoryItems: Array<{
    id: string
    name: string
    amount: number
    expiryDate: string
    productType: {
      name: string
      unit: string
    }
  }>
}

const defaultUser: MockUser = {
  id: 'user-123',
  firstName: 'Test',
  lastName: 'User',
  email: 'test.user@example.com',
  roles: ['USER'],
  notifications: true,
  emailUpdates: false,
  locationSharing: true,
}

const defaultHousehold: MockHousehold = {
  id: 'household-1',
  name: 'Testhusstand',
  address: 'Eksempelvei 1',
  latitude: 59.91,
  longitude: 10.75,
  postalCode: '0150',
  city: 'Oslo',
  owner: {
    id: 'user-123',
    firstName: 'Test',
    lastName: 'User',
    email: 'test.user@example.com',
  },
  createdAt: '2026-01-10T10:00:00.000Z',
  members: [
    {
      id: 'member-1',
      role: 'OWNER',
      user: {
        id: 'user-123',
        firstName: 'Test',
        lastName: 'User',
        email: 'test.user@example.com',
      },
    },
  ],
  guests: [],
  meetingPlaces: [],
  inventoryItems: [],
}

declare global {
  // eslint-disable-next-line @typescript-eslint/no-namespace
  namespace Cypress {
    interface Chainable {
      stubTurnstile(): Chainable<void>
      seedAuthenticatedSession(): Chainable<void>
      mockAuthMe(user?: Partial<MockUser>): Chainable<void>
      mockCoreHouseholdData(household?: Partial<MockHousehold>): Chainable<void>
      mockInventoryData(): Chainable<void>
    }
  }
}

Cypress.Commands.add('stubTurnstile', () => {
  cy.on('window:before:load', (win) => {
    // @ts-expect-error - test double for global turnstile in registration flow
    win.turnstile = {
      render: (_selector: string, options: { callback?: (token: string) => void }) => {
        options.callback?.('mock-captcha-token')
        return 'stubbed-widget-id'
      },
      remove: () => {},
      reset: () => {},
      getResponse: () => 'mock-captcha-token',
    }
  })
})

Cypress.Commands.add('seedAuthenticatedSession', () => {
  cy.window().then((win) => {
    win.localStorage.setItem('accessToken', 'mock-test-access-token')
    win.localStorage.setItem('refreshToken', 'mock-test-refresh-token')
  })
})

Cypress.Commands.add('mockAuthMe', (user: Partial<MockUser> = {}) => {
  cy.intercept('GET', '/api/auth/me', {
    statusCode: 200,
    body: {
      ...defaultUser,
      ...user,
    },
  }).as('getMe')
})

Cypress.Commands.add('mockCoreHouseholdData', (household: Partial<MockHousehold> = {}) => {
  const mergedHousehold = {
    ...defaultHousehold,
    ...household,
  }

  cy.intercept('GET', '/api/households/active', {
    statusCode: 200,
    body: mergedHousehold,
  }).as('getActiveHousehold')

  cy.intercept('GET', '/api/households/all', {
    statusCode: 200,
    body: [mergedHousehold],
  }).as('getAllHouseholds')

  cy.intercept('GET', '/api/household-invites/pending', {
    statusCode: 200,
    body: [],
  }).as('getPendingInvitesForUser')

  cy.intercept('GET', `/api/household-invites/household/${mergedHousehold.id}/pending`, {
    statusCode: 200,
    body: [],
  }).as('getPendingInvitesForHousehold')
})

Cypress.Commands.add('mockInventoryData', () => {
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
    body: [
      {
        id: 'food-1',
        name: 'Havregryn',
        amount: 1200,
        expiryDate: '2027-03-01',
        productType: { name: 'Mat', unit: 'kcal' },
      },
    ],
  }).as('getFoodItems')

  cy.intercept('GET', '/api/items/checklist*', {
    statusCode: 200,
    body: [
      {
        id: 'check-1',
        name: 'Fyrstikker',
        checked: false,
        category: 'misc',
      },
    ],
  }).as('getChecklistItems')
})
