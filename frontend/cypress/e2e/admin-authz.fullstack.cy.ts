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
