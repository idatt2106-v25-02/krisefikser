describe('Inventory mutations full-stack', () => {
  beforeEach(() => {
    cy.clearAuthSession()
    cy.loginAsSeededUser()
  })

  function openProductCategory(categoryId: 'water' | 'food' | 'misc'): void {
    cy.get(`[aria-controls="category-${categoryId}"]`).scrollIntoView()
    cy.get(`[aria-controls="category-${categoryId}"]`).click()
    cy.get(`#category-${categoryId}`).should('be.visible')
  }

  it('updates water amount against real API', () => {
    cy.intercept('PUT', '/api/items/water/*').as('updateWater')

    cy.visit('/husstand/beredskapslager')
    cy.location('pathname', { timeout: 20000 }).should('eq', '/husstand/beredskapslager')
    cy.contains('Beredskapslager').should('be.visible')

    openProductCategory('water')
    cy.get('#waterAddInput').should('be.visible')
    cy.get('#waterAddInput').clear()
    cy.get('#waterAddInput').type('0.5')
    cy.contains('Oppdater vannmengde').click()
    cy.wait('@updateWater', { timeout: 15000 })
      .its('response.statusCode')
      .should('satisfy', (code: number) => code === 200 || code === 204)
  })

  it('renames first food item when editor is available', () => {
    cy.intercept('PUT', '/api/items/food/*').as('updateFood')

    cy.visit('/husstand/beredskapslager')
    cy.location('pathname', { timeout: 20000 }).should('eq', '/husstand/beredskapslager')
    cy.contains('Beredskapslager').should('be.visible')

    openProductCategory('food')
    cy.get('body').then(($body) => {
      const editors = $body.find('[title="Rediger matvare"]')
      if (editors.length === 0) {
        cy.log('No food rows to edit; skipping')
        return
      }
      cy.wrap(editors.first()).click()
      const uniqueName = `E2E_FS_${Date.now()}`
      cy.get('input[placeholder="Navn"]').clear()
      cy.get('input[placeholder="Navn"]').type(uniqueName)
      cy.get('[title="Lagre endringer"]').click()
      cy.wait('@updateFood', { timeout: 15000 }).its('response.statusCode').should('eq', 200)
      cy.contains(uniqueName).should('be.visible')
    })
  })

  it('toggles first checklist item when misc section has checkboxes', () => {
    cy.intercept('PUT', '/api/items/checklist/*').as('toggleChecklist')

    cy.visit('/husstand/beredskapslager')
    cy.location('pathname', { timeout: 20000 }).should('eq', '/husstand/beredskapslager')
    cy.contains('Beredskapslager').should('be.visible')

    openProductCategory('misc')
    cy.get('body').then(($body) => {
      const boxes = $body.find('#category-misc input[type="checkbox"]')
      if (boxes.length === 0) {
        cy.log('No checklist checkboxes; skipping')
        return
      }
      cy.wrap(boxes.first()).click({ force: true })
      cy.wait('@toggleChecklist', { timeout: 15000 }).its('response.statusCode').should('eq', 200)
    })
  })
})
