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

if (import.meta.env.VITE_POSTHOG_ENABLED === 'true') {
  app.use(posthogPlugin)
} else {
  console.log('PostHog is disabled')
}

app.mount('#app')
