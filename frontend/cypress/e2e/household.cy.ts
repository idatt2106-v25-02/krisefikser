describe('Household flow', () => {
  beforeEach(() => {
    cy.mockAuthMe()
    cy.mockCoreHouseholdData({
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
        {
          id: 'member-2',
          role: 'MEMBER',
          user: {
            id: 'user-234',
            firstName: 'Kari',
            lastName: 'Nordmann',
            email: 'kari@example.com',
          },
        },
      ],
    })
  })

  function visitHousehold(): void {
    cy.visit('/husstand', {
      onBeforeLoad(win) {
        win.localStorage.setItem('accessToken', 'mock-test-access-token')
        win.localStorage.setItem('refreshToken', 'mock-test-refresh-token')
      },
    })
  }

  it('loads household page with active household and members', () => {
    visitHousehold()
    cy.wait('@getMe')
    cy.wait('@getAllHouseholds')
    cy.wait('@getActiveHousehold')

    cy.contains('Testhusstand').should('be.visible')
    cy.contains('Medlemmer og gjester').should('be.visible')
    cy.contains('Kari Nordmann').should('be.visible')
  })

  it('accepts a pending invite', () => {
    cy.intercept('GET', '/api/household-invites/pending', {
      statusCode: 200,
      body: [
        {
          id: 'invite-1',
          invitedEmail: 'test.user@example.com',
          status: 'PENDING',
          household: { name: 'Nabo-husstand' },
          createdBy: { firstName: 'Ola', lastName: 'Nordmann' },
        },
      ],
    }).as('getPendingInvitesForUser')
    cy.intercept('POST', '/api/household-invites/invite-1/accept', {
      statusCode: 200,
      body: {},
    }).as('acceptInvite')

    visitHousehold()
    cy.wait('@getPendingInvitesForUser')
    cy.contains('Ventende invitasjoner').should('be.visible')
    cy.contains('Nabo-husstand').should('be.visible')
    cy.contains('Godta').click()
    cy.wait('@acceptInvite')
  })

  it('declines a pending invite', () => {
    cy.intercept('GET', '/api/household-invites/pending', {
      statusCode: 200,
      body: [
        {
          id: 'invite-2',
          invitedEmail: 'test.user@example.com',
          status: 'PENDING',
          household: { name: 'Annen-husstand' },
          createdBy: { firstName: 'Anne', lastName: 'Hansen' },
        },
      ],
    }).as('getPendingInvitesForUser')
    cy.intercept('POST', '/api/household-invites/invite-2/decline', {
      statusCode: 200,
      body: {},
    }).as('declineInvite')

    visitHousehold()
    cy.wait('@getPendingInvitesForUser')
    cy.get('button[aria-label="Tilgjengelighetsalternativer"]')
      .should('exist')
      .invoke('hide')
    cy.contains('Avslå').click()
    cy.wait('@declineInvite')
  })

  it('redirects to join-or-create page when active household returns 404', () => {
    cy.intercept('GET', '/api/households/active', {
      statusCode: 404,
      body: {},
    }).as('activeHouseholdNotFound')
    cy.intercept('GET', '/api/households/all', {
      statusCode: 200,
      body: [],
    }).as('getAllHouseholds')

    visitHousehold()
    cy.wait('@activeHouseholdNotFound')
    cy.url().should('include', '/bli-med-eller-opprett-husstand')
  })
})
