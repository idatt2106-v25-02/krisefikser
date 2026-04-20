describe('Public navigation and static pages', () => {
  beforeEach(() => {
    cy.clearLocalStorage()
  })

  it('renders privacy policy content and contact email', () => {
    cy.visit('/personvern')

    cy.contains('h1', 'Personvern').should('be.visible')
    cy.contains('Hvordan vi behandler dine data').should('be.visible')
    cy.contains('Dine rettigheter').should('be.visible')
    cy.contains('a', 'kontakt@krisefikser.app')
      .should('have.attr', 'href', 'mailto:kontakt@krisefikser.app')
  })

  it('renders about page sections', () => {
    cy.visit('/om-oss')

    cy.contains('h1', 'Om oss').should('be.visible')
    cy.contains('Hvem er vi?').should('be.visible')
    cy.contains('Vår misjon').should('be.visible')
    cy.contains('Hvem står bak?').should('be.visible')
  })

  it('shows 404 page and navigates back to home', () => {
    cy.visit('/finnes-ikke')

    cy.contains('Siden ble ikke funnet').should('be.visible')
    cy.contains('button', 'Gå tilbake til hjem').click()
    cy.url().should('eq', `${Cypress.config().baseUrl}/`)
  })

  it('allows unauthenticated users to start household creation flow', () => {
    cy.visit('/bli-med-eller-opprett-husstand')

    cy.contains('Kom i gang med husstand').should('be.visible')
    cy.contains('button', 'Registrer deg for å opprette').click()
    cy.url().should('include', '/registrer')
  })

  it('allows unauthenticated users to navigate to login from household join page', () => {
    cy.visit('/bli-med-eller-opprett-husstand')

    cy.contains('button', 'Logg inn').first().click()
    cy.url().should('include', '/logg-inn')
  })
})
