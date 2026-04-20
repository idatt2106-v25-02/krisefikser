describe('Inventory flow', () => {
  beforeEach(() => {
    cy.mockAuthMe()
    cy.mockCoreHouseholdData()
    cy.mockInventoryData()
  })

  function visitInventory(): void {
    cy.visit('/husstand/beredskapslager', {
      onBeforeLoad(win) {
        win.localStorage.setItem('accessToken', 'mock-test-access-token')
        win.localStorage.setItem('refreshToken', 'mock-test-refresh-token')
      },
    })
  }

  it('loads inventory page and main cards', () => {
    visitInventory()
    cy.wait('@getMe')
    cy.wait('@getActiveHousehold')
    cy.wait('@getInventorySummary')
    cy.wait('@getFoodItems')
    cy.wait('@getChecklistItems')

    cy.contains('Beredskapslager').should('be.visible')
    cy.contains('Oversikt').should('be.visible')
    cy.contains('Produkter').should('be.visible')
  })

  it('updates water amount through inventory controls', () => {
    cy.intercept('PUT', '/api/items/water/*', {
      statusCode: 200,
      body: {},
    }).as('updateWater')

    visitInventory()
    cy.contains('Vann').click()
    cy.get('#waterAddInput').clear().type('2.0')
    cy.contains('Oppdater vannmengde').click()
    cy.wait('@updateWater')
  })

  it('edits an existing food item', () => {
    cy.intercept('PUT', '/api/items/food/*', {
      statusCode: 200,
      body: {},
    }).as('updateFoodItem')

    visitInventory()
    cy.contains('Mat').click()
    cy.get('[title="Rediger matvare"]').first().click()
    cy.get('input[placeholder="Navn"]').clear().type('Tørket ris')
    cy.get('[title="Lagre endringer"]').click()
    cy.wait('@updateFoodItem').its('request.body').should('include', { name: 'Tørket ris' })
  })

  it('deletes an existing food item after confirmation', () => {
    cy.intercept('DELETE', '/api/items/food/*', {
      statusCode: 204,
      body: {},
    }).as('deleteFoodItem')

    visitInventory()
    cy.contains('Mat').click()
    cy.get('[title="Slett matvare"]').first().click()
    cy.contains('Slett matvare').should('be.visible')
    cy.contains('button', 'Slett').click()
    cy.wait('@deleteFoodItem')
  })

  it('toggles a checklist item', () => {
    cy.intercept('PUT', '/api/items/checklist/*', {
      statusCode: 200,
      body: {},
    }).as('toggleChecklist')

    visitInventory()
    cy.contains('Diverse').click()
    cy.get('input[type="checkbox"]').first().check({ force: true })
    cy.wait('@toggleChecklist')
  })
})
