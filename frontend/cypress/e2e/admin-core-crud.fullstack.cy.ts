describe('Admin article CRUD full-stack', () => {
  beforeEach(() => {
    cy.clearAuthSession()
    cy.loginAsSeededAdmin()
    cy.visitWhenAuthenticated('/admin/artikler')
    cy.location('pathname', { timeout: 20000 }).should('eq', '/admin/artikler')
    cy.get('[data-cy=admin-articles-title]', { timeout: 20000 }).should('be.visible')
  })

  it('creates and deletes an article', () => {
    const title = `E2E artikkel ${Date.now()}`
    const imageUrl =
      'https://res.cloudinary.com/dmoe4eqt4/image/upload/v1776713518/krisefikser/articles/seed-article-hero-a.jpg'

    cy.intercept('POST', '**/api/images/upload', {
      statusCode: 200,
      body: { url: imageUrl, publicId: 'e2e-article-image' },
    }).as('uploadArticleImage')

    cy.contains('button', 'Ny artikkel').click()
    cy.get('#title').type(title)
    cy.get('input[type="file"]').selectFile(
      {
        contents: Cypress.Buffer.from('fake-image'),
        fileName: 'e2e-article.jpg',
        mimeType: 'image/jpeg',
      },
      { force: true },
    )
    cy.wait('@uploadArticleImage')
    cy.get('#text').type('Automatisk opprettet av full-stack Cypress-test.')
    cy.contains('button', 'Opprett').click()

    cy.contains('tr', title).scrollIntoView()
    cy.contains('tr', title).should('be.visible')

    cy.contains('tr', title).within(() => {
      cy.get('button').last().click()
    })
    cy.contains('button', 'Slett').click()
    cy.contains(title).should('not.exist')
  })
})
