import './styles/globals.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'
import { setupVueQuery } from './plugins/vue-query'
import accessibilityPlugin from './plugins/accessibility'
import posthogPlugin from './plugins/posthog'
const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(accessibilityPlugin)

if (import.meta.env.VITE_POSTHOG_ENABLED === "true") {
  app.use(posthogPlugin)
} else {
  console.log("PostHog is disabled")
}

setupVueQuery(app)

app.mount('#app')
