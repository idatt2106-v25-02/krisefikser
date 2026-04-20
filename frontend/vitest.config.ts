import { fileURLToPath } from 'node:url'
import { mergeConfig, defineConfig, configDefaults } from 'vitest/config'
import viteConfig from './vite.config'

export default mergeConfig(
  viteConfig,
  defineConfig({
    test: {
      environment: 'jsdom',
      exclude: [...configDefaults.exclude, 'e2e/**', 'cypress/**'],
      root: fileURLToPath(new URL('./', import.meta.url)),
      coverage: {
        provider: 'v8',
        all: false,
        reporter: ['text-summary', 'lcov', 'html'],
        reportsDirectory: './coverage',
        exclude: [
          '**/*.d.ts',
          'src/main.ts',
          'src/views/**',
          'src/router/**',
          'src/services/**',
          'src/plugins/**',
          'src/api/generated/**',
          'cypress/**',
          'e2e/**',
        ],
        thresholds: {
          lines: 20,
          functions: 20,
          branches: 15,
          statements: 20,
        },
      },
    },
  }),
)
