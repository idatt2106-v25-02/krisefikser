const apiBaseUrl = () => (Cypress.env('apiUrl') as string) || 'http://127.0.0.1:8080'

function mailHookSecret(): string {
  const secret = Cypress.env('e2eMailHookSecret') as string | undefined
  if (!secret) {
    throw new Error(
      'e2eMailHookSecret is not set. For local full-stack runs: export E2E_MAIL_HOOK_SECRET=... ' +
        'and start backend with profiles dev,e2e and the same secret, then CYPRESS_e2eMailHookSecret=...',
    )
  }
  return secret
}

function extractVerifyToken(html: string): string {
  const decoded = html.replace(/&amp;/g, '&')
  const match = decoded.match(/verify\?token=([a-fA-F0-9-]+)/)
  if (!match?.[1]) {
    throw new Error('Could not find verify?token= in captured email HTML')
  }
  return match[1]
}

describe('Registration and email verification (full-stack)', () => {
  before(function () {
    if (!(Cypress.env('e2eMailHookSecret') as string | undefined)) {
      // Mocha: skip suite when running full-stack locally without hook secret / backend e2e profile.
      this.skip()
    }
  })

  beforeEach(() => {
    cy.clearAuthSession()
    cy.stubTurnstile()
  })

  it('registers, reads captured verification email, verifies, and logs in', () => {
    const email = `e2e.verify.${Date.now()}@testern.no`
    const password = 'Password123!'

    cy.intercept('POST', '/api/auth/register').as('register')

    cy.visit('/registrer')
    cy.get('input[name="firstName"]').type('E2E')
    cy.get('input[name="lastName"]').type('Verify')
    cy.get('input[name="email"]').type(email)
    cy.get('input[name="password"]').type(password)
    cy.get('input[name="confirmPassword"]').type(password)
    cy.get('input#acceptedPrivacyPolicy').check({ force: true })
    cy.get('button[type="submit"]').click()

    cy.wait('@register').its('response.statusCode').should('eq', 200)
    cy.url({ timeout: 15000 }).should('include', '/bekreft-e-post')

    cy.request({
      method: 'GET',
      url: `${apiBaseUrl()}/api/e2e/mail/latest`,
      qs: { email },
      headers: { 'X-E2E-Mail-Hook': mailHookSecret() },
      failOnStatusCode: true,
    }).then((res) => {
      expect(res.status).to.eq(200)
      const body = res.body as { html?: string }
      expect(body.html).to.be.a('string')
      const token = extractVerifyToken(body.html as string)
      cy.wrap(token).as('verifyToken')
    })

    cy.get<string>('@verifyToken').then((token) => {
      cy.visit(`/verify?token=${encodeURIComponent(token)}`)
    })

    cy.contains('E-post verifisert!', { timeout: 25000 }).should('be.visible')

    cy.loginByApi(email, password)
    cy.visit('/dashboard')
    cy.contains('Min profil', { timeout: 20000 }).should('be.visible')
  })
})
