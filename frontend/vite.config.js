import path from 'path';
import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import tailwindcss from '@tailwindcss/vite';
import vueDevTools from 'vite-plugin-vue-devtools';
// https://vite.dev/config/
export default defineConfig({
    plugins: [vue(), tailwindcss(), vueDevTools()],
    resolve: {
        alias: {
            '@': path.resolve(__dirname, './src')
        }
    },
    define: {
        global: {},
    },
    test: {
        globals: true,
        environment: 'jsdom'
    }
});
