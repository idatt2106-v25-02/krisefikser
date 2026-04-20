import { defineConfig } from 'cypress'

export default defineConfig({
  e2e: {
    specPattern: 'cypress/e2e/**/*.{cy,spec}.{js,jsx,ts,tsx}',
    baseUrl: 'http://localhost:5173',
    viewportWidth: 1280,
    viewportHeight: 720,
  },
  env: {
    apiUrl: 'http://localhost:8080',
    e2eUserEmail: 'brotherman@testern.no',
    e2eUserPassword: 'password',
    e2eAdminEmail: 'admin@example.com',
    e2eAdminPassword: 'admin123',
    e2eSuperAdminEmail: 'admin@krisefikser.app',
    e2eSuperAdminPassword: 'admin123',
  },
})
