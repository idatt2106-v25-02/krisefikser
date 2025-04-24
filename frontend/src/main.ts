import './styles/globals.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'
import { setupVueQuery } from './plugins/vue-query'

const app = createApp(App)

app.use(createPinia())
app.use(router)

setupVueQuery(app)

app.mount('#app')
