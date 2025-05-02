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

if (import.meta.env.NODE_ENV === 'production') {
  app.use(posthogPlugin)
}

setupVueQuery(app)

app.mount('#app')
