import posthog from 'posthog-js'
import type { Plugin } from 'vue'

/**
 * Registers `$posthog` on the app. SDK initialization is deferred until analytics consent
 * (see {@link initializePostHogSdk} in `posthog-sdk.ts`).
 */
const posthogPlugin: Plugin = {
  install(app) {
    if (!import.meta.env.VITE_POSTHOG_KEY) {
      console.warn('PostHog key is missing. Skipping analytics plugin install.')
      return
    }

    app.config.globalProperties.$posthog = posthog
  },
}

export default posthogPlugin
