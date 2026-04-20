describe('Admin article CRUD full-stack', () => {
  beforeEach(() => {
    cy.clearAuthSession()
    cy.loginAsSeededAdmin()
    cy.visit('/admin/artikler')
    cy.contains('Administrer artikler').should('be.visible')
  })

  it('creates and deletes an article', () => {
    const title = `E2E artikkel ${Date.now()}`

    cy.contains('button', 'Ny artikkel').click()
    cy.get('#title').type(title)
    cy.get('#imageUrl').type('https://picsum.photos/seed/e2e/800/600')
    cy.get('#text').type('Automatisk opprettet av full-stack Cypress-test.')
    cy.contains('button', 'Opprett').click()

    cy.contains(title).should('be.visible')

    cy.contains('tr', title).within(() => {
      cy.get('button').last().click()
    })
    cy.contains('button', 'Slett').click()
    cy.contains(title).should('not.exist')
  })
})
