import path from 'path'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import tailwindcss from '@tailwindcss/vite'
import vueDevTools from 'vite-plugin-vue-devtools'
import istanbul from 'vite-plugin-istanbul'

const enableE2eCoverage =
  process.env.CYPRESS_COVERAGE === 'true' || process.env.VITE_COVERAGE === 'true'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    tailwindcss(),
    ...(enableE2eCoverage
      ? [
          istanbul({
            cypress: true,
            include: 'src/**/*',
            exclude: [
              'node_modules/**',
              '**/cypress/**',
              '**/*.spec.ts',
              '**/__tests__/**',
              'src/api/generated/**',
            ],
            extension: ['.js', '.ts', '.vue'],
          }),
        ]
      : []),
    vueDevTools(),
  ],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  define: {
    global: {},
  },
  test: {
    globals: true,
    environment: 'jsdom',
  },
})
