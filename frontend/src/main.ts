import './styles/globals.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'
import { setupVueQuery } from './plugins/vue-query'
import { createWebSocketService } from '@/api/websocket/webSocketProvider'
import type { IWebSocketService } from '@/api/websocket/IWebSocketService'
import accessibilityPlugin from './plugins/tts/accessibility'
import posthogPlugin from './plugins/docs/posthog'
import {
  applyStoredTrackingConsent,
  registerPostHogActivationHandler,
} from './plugins/docs/posthog-consent'
import { setupPostHogAppHooks } from './plugins/docs/posthog-app-hooks'
import { useAuthStore } from './stores/auth/useAuthStore'
import { setStoreRef } from './api/storeRef'
const app = createApp(App)
export const webSocket: IWebSocketService = createWebSocketService()

app.use(createPinia())
setupVueQuery(app)

const authStore = useAuthStore()
setStoreRef(authStore)

app.use(router)
app.use(accessibilityPlugin)

const posthogEnabled =
  import.meta.env.VITE_POSTHOG_ENABLED === 'true' && !!import.meta.env.VITE_POSTHOG_KEY

if (posthogEnabled) {
  app.use(posthogPlugin)

  registerPostHogActivationHandler(() => {
    setupPostHogAppHooks(app, router, authStore)
  })

  applyStoredTrackingConsent()
} else {
  console.log('PostHog is disabled or missing key')
}

app.mount('#app')
