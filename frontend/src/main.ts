import './styles/globals.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'
import LoginView from './views/auth/LoginView.vue'

const app = createApp(LoginView)

app.use(createPinia())
app.use(router)

app.mount('#app')
