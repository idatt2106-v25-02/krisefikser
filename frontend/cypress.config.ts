import { createRequire } from 'node:module'
import { defineConfig } from 'cypress'

const require = createRequire(import.meta.url)

const e2eCoverageEnabled = process.env.CYPRESS_COVERAGE === 'true'

export default defineConfig({
  e2e: {
    specPattern: 'cypress/e2e/**/*.{cy,spec}.{js,jsx,ts,tsx}',
    baseUrl: 'http://127.0.0.1:5173',
    viewportWidth: 1280,
    viewportHeight: 720,
    supportFile: e2eCoverageEnabled
      ? 'cypress/support/e2eCoverage.ts'
      : 'cypress/support/e2eStandard.ts',
    setupNodeEvents(on, config) {
      if (e2eCoverageEnabled) {
        require('@cypress/code-coverage/task')(on, config)
      }
      return config
    },
  },
  env: {
    apiUrl: 'http://127.0.0.1:8080',
    e2eUserEmail: 'brotherman@testern.no',
    e2eUserPassword: 'password',
    e2eAdminEmail: 'admin@example.com',
    e2eAdminPassword: 'admin123',
    e2eSuperAdminEmail: 'admin@krisefikser.app',
    e2eSuperAdminPassword: 'admin123',
  },
})
