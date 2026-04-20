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
      loginByApi(email: string, password: string): Chainable<void>
      loginAsSeededUser(): Chainable<void>
      loginAsSeededAdmin(): Chainable<void>
      loginAsSeededSuperAdmin(): Chainable<void>
      clearAuthSession(): Chainable<void>
    }
  }
}

Cypress.Commands.add('stubTurnstile', () => {
  cy.on('window:before:load', (win) => {
    // @ts-expect-error - test double for global turnstile in registration flow
    win.turnstile = {
      render: (_selector: string, options: { callback?: (token: string) => void }) => {
        // Cloudflare Turnstile dummy token; pairs with test secret 1x0000000000000000000000000000000AA on backend.
        options.callback?.('XXXX.DUMMY.TOKEN.XXXX')
        return 'stubbed-widget-id'
      },
      remove: () => {},
      reset: () => {},
      getResponse: () => 'XXXX.DUMMY.TOKEN.XXXX',
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

interface LoginResponse {
  accessToken: string
  refreshToken: string
}

const apiBaseUrl = () => Cypress.env('apiUrl') || 'http://localhost:8080'

function persistTokens(tokens: LoginResponse): void {
  window.localStorage.setItem('accessToken', tokens.accessToken)
  window.localStorage.setItem('refreshToken', tokens.refreshToken)
}

Cypress.Commands.add('clearAuthSession', () => {
  cy.window().then((win) => {
    win.localStorage.removeItem('accessToken')
    win.localStorage.removeItem('refreshToken')
  })
})

Cypress.Commands.add('loginByApi', (email: string, password: string) => {
  cy.request<LoginResponse>({
    method: 'POST',
    url: `${apiBaseUrl()}/api/auth/login`,
    body: {
      email,
      password,
    },
  }).then((response) => {
    expect(response.status).to.eq(200)
    persistTokens(response.body)
  })
})

Cypress.Commands.add('loginAsSeededUser', () => {
  const userEmail = Cypress.env('e2eUserEmail') || 'brotherman@testern.no'
  const userPassword = Cypress.env('e2eUserPassword') || 'password'
  cy.loginByApi(userEmail, userPassword)
})

Cypress.Commands.add('loginAsSeededAdmin', () => {
  const adminEmail = Cypress.env('e2eAdminEmail') || 'admin@example.com'
  const adminPassword = Cypress.env('e2eAdminPassword') || 'admin123'
  cy.loginByApi(adminEmail, adminPassword)
})

Cypress.Commands.add('loginAsSeededSuperAdmin', () => {
  const superAdminEmail = Cypress.env('e2eSuperAdminEmail') || 'admin@krisefikser.app'
  const superAdminPassword = Cypress.env('e2eSuperAdminPassword') || 'admin123'
  cy.loginByApi(superAdminEmail, superAdminPassword)
})
