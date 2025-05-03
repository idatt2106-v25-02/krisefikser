import './styles/globals.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'
import { setupVueQuery } from './plugins/vue-query'
import { WebSocketService } from '@/api/WebSocketService.ts'

const app = createApp(App)
export const webSocket = new WebSocketService()

app.use(createPinia())
app.use(router)

setupVueQuery(app)

app.mount('#app')
