import './styles/globals.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'
import { setupVueQuery } from './plugins/vue-query'
import { WebSocketService } from '@/api/websocket/WebSocketService.ts'
import accessibilityPlugin from './plugins/tts/accessibility.ts'
import posthogPlugin from './plugins/docs/posthog.ts'
import { useAuthStore } from './stores/auth/useAuthStore.ts'
import { setStoreRef } from './api/storeRef'
const app = createApp(App)
export const webSocket = new WebSocketService()

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
