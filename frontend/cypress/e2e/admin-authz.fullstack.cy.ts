describe('Admin authorization matrix full-stack', () => {
  beforeEach(() => {
    cy.clearAuthSession()
  })

  it('allows admin access to admin dashboard', () => {
    cy.loginAsSeededAdmin()
    cy.visit('/admin')
    cy.url().should('include', '/admin')
    cy.contains('Dashboard oversikt').should('be.visible')
  })

  it('allows admin access to admin map and scenarios routes', () => {
    cy.loginAsSeededAdmin()

    cy.visit('/admin/kart')
    cy.url().should('include', '/admin/kart')
    cy.contains('Kartstyring').should('be.visible')
    cy.get('body').should('not.contain', 'Siden ble ikke funnet')

    cy.visit('/admin/scenarios')
    cy.url().should('include', '/admin/scenarios')
    cy.contains('Krisescenarioer').should('be.visible')
    cy.get('body').should('not.contain', 'Siden ble ikke funnet')
  })

  it('blocks admin from super-admin route', () => {
    cy.loginAsSeededAdmin()
    cy.visit('/admin/invite')
    cy.contains('Siden ble ikke funnet').should('be.visible')
  })

  it('allows super admin into super-admin route', () => {
    cy.loginAsSeededSuperAdmin()
    cy.visit('/admin/invite')
    cy.url().should('include', '/admin/invite')
  })
})
