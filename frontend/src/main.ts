import './styles/globals.css'

import { createApp, watch } from 'vue'
import { createPinia } from 'pinia'
import posthog from 'posthog-js'

import App from './App.vue'
import router from './router'
import { setupVueQuery } from './plugins/vue-query'
import { createWebSocketService } from '@/api/websocket/webSocketProvider'
import type { IWebSocketService } from '@/api/websocket/IWebSocketService'
import accessibilityPlugin from './plugins/tts/accessibility'
import posthogPlugin from './plugins/docs/posthog'
import { applyStoredTrackingConsent } from './plugins/docs/posthog-consent'
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

  applyStoredTrackingConsent()

  router.afterEach((to) => {
    posthog.capture('$pageview', {
      path: to.path,
      routeName: typeof to.name === 'string' ? to.name : null,
      query: to.query,
    })
  })

  watch(
    () => authStore.currentUser,
    (currentUser) => {
      if (!currentUser?.id) {
        return
      }

      posthog.identify(currentUser.id, {
        email: currentUser.email,
        roles: currentUser.roles,
      })
    },
    { immediate: true },
  )

  watch(
    () => authStore.isAuthenticated,
    (isAuthenticated) => {
      if (!isAuthenticated) {
        posthog.reset()
      }
    },
    { immediate: true },
  )
} else {
  console.log('PostHog is disabled or missing key')
}

app.mount('#app')
