describe('Admin authorization matrix full-stack', () => {
  beforeEach(() => {
    cy.clearAuthSession()
  })

  it('allows admin access to admin dashboard', () => {
    cy.loginAsSeededAdmin()
    cy.visit('/admin')
    cy.location('pathname', { timeout: 20000 }).should('eq', '/admin')
    cy.get('[data-cy=admin-dashboard]', { timeout: 20000 }).should('be.visible')
  })

  it('allows admin access to admin map and scenarios routes', () => {
    cy.loginAsSeededAdmin()

    cy.visit('/admin/kart')
    cy.location('pathname', { timeout: 20000 }).should('eq', '/admin/kart')
    cy.get('[data-cy=admin-map]', { timeout: 20000 }).should('be.visible')
    cy.get('body').should('not.contain', 'Siden ble ikke funnet')

    cy.visit('/admin/scenarios')
    cy.location('pathname', { timeout: 20000 }).should('eq', '/admin/scenarios')
    cy.get('[data-cy=admin-scenarios]', { timeout: 20000 }).should('be.visible')
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
    cy.location('pathname', { timeout: 20000 }).should('eq', '/admin/invite')
  })
})
