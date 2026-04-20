describe('Cookie consent and settings', () => {
  beforeEach(() => {
    cy.visit('/', {
      onBeforeLoad(win) {
        win.localStorage.clear()
      },
    })
  })

  it('shows cookie banner when no prior consent exists', () => {
    cy.contains('Vi bruker nødvendige informasjonskapsler').should('be.visible')
    cy.contains('button', 'Aksepter alle').should('be.visible')
    cy.contains('button', 'Kun nødvendige').should('be.visible')
  })

  it('stores necessary-only choice without granting analytics', () => {
    cy.contains('button', 'Kun nødvendige').click()
    cy.window().then((win) => {
      const raw = win.localStorage.getItem('krisefikser_cookie_consent_v1')
      expect(raw, 'structured consent').to.be.a('string')
      const parsed = JSON.parse(raw!) as { analytics: boolean; dismissed: boolean }
      expect(parsed.dismissed).to.eq(true)
      expect(parsed.analytics).to.eq(false)
      expect(win.localStorage.getItem('posthog_tracking_consent')).to.eq('denied')
    })
    cy.contains('Vi bruker nødvendige informasjonskapsler').should('not.exist')
  })

  it('stores accept-all with analytics granted', () => {
    cy.contains('button', 'Aksepter alle').click()
    cy.window().then((win) => {
      const parsed = JSON.parse(win.localStorage.getItem('krisefikser_cookie_consent_v1')!) as {
        analytics: boolean
      }
      expect(parsed.analytics).to.eq(true)
      expect(win.localStorage.getItem('posthog_tracking_consent')).to.eq('granted')
    })
  })

  it('opens cookie settings from footer and saves analytics toggle', () => {
    cy.contains('button', 'Kun nødvendige').click()
    cy.contains('button', 'Cookie-innstillinger').click()
    cy.contains('Informasjonskapsler og analyse').should('be.visible')
    cy.get('input[aria-label="Tillat analyse med PostHog"]').check()
    cy.contains('button', 'Lagre valg').click()
    cy.window().then((win) => {
      const parsed = JSON.parse(win.localStorage.getItem('krisefikser_cookie_consent_v1')!) as {
        analytics: boolean
        source: string
      }
      expect(parsed.analytics).to.eq(true)
      expect(parsed.source).to.eq('settings')
    })
  })

  it('hides banner when consent already recorded', () => {
    cy.window().then((win) => {
      win.localStorage.setItem(
        'krisefikser_cookie_consent_v1',
        JSON.stringify({
          dismissed: true,
          analytics: false,
          policyVersion: '2026-04-20',
          updatedAt: new Date().toISOString(),
          source: 'banner_necessary_only',
        }),
      )
      win.localStorage.setItem('posthog_tracking_consent', 'denied')
      win.localStorage.setItem('cookiesAccepted', 'true')
    })
    cy.reload()
    cy.contains('Vi bruker nødvendige informasjonskapsler').should('not.exist')
  })
})
