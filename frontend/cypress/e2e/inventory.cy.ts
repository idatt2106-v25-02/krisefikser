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

  function openProductCategory(categoryId: 'water' | 'food' | 'misc'): void {
    cy.get(`[aria-controls="category-${categoryId}"]`).scrollIntoView().click()
    cy.get(`#category-${categoryId}`).should('be.visible')
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
    openProductCategory('water')
    cy.get('#waterAddInput').clear()
    cy.get('#waterAddInput').type('2.0')
    cy.contains('Oppdater vannmengde').click()
    cy.wait('@updateWater')
  })

  it('edits an existing food item', () => {
    cy.intercept('PUT', '/api/items/food/*', {
      statusCode: 200,
      body: {},
    }).as('updateFoodItem')

    visitInventory()
    openProductCategory('food')
    cy.get('[title="Rediger matvare"]').first().click()
    cy.get('input[placeholder="Navn"]').clear()
    cy.get('input[placeholder="Navn"]').type('Tørket ris')
    cy.get('[title="Lagre endringer"]').click()
    cy.wait('@updateFoodItem').its('request.body').should('include', { name: 'Tørketris' })
  })

  it('deletes an existing food item after confirmation', () => {
    cy.intercept('DELETE', '/api/items/food/*', {
      statusCode: 204,
      body: {},
    }).as('deleteFoodItem')

    visitInventory()
    openProductCategory('food')
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
    openProductCategory('misc')
    cy.get('input[type="checkbox"]').first().check({ force: true })
    cy.wait('@toggleChecklist')
  })
})
