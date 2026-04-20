import posthog from 'posthog-js'
import type { Plugin } from 'vue'

const posthogPlugin: Plugin = {
  install(app) {
    const key = import.meta.env.VITE_POSTHOG_KEY
    const host = import.meta.env.VITE_POSTHOG_HOST || 'https://eu.i.posthog.com'

    if (!key) {
      console.warn('PostHog key is missing. Skipping analytics initialization.')
      return
    }

    posthog.init(key, {
      api_host: host,
      capture_pageview: false,
      autocapture: true,
      persistence: 'localStorage',
    })

    app.config.globalProperties.$posthog = posthog
  },
}

export default posthogPlugin
