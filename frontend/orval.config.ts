import { defineConfig } from 'orval'
import { loadEnv } from 'vite'


const env = loadEnv("production", process.cwd())


export default defineConfig({
  krisefikser: {
    output: {
      target: './src/api/generated',
      client: 'vue-query',
      mode: 'tags-split',
      schemas: './src/api/generated/model',
      mock: false,
      prettier: true,
      clean: true,
      baseUrl: env.VITE_API_URL || 'http://localhost:8080',
      override: {
        mutator: {
          path: './src/api/axios.ts',
          name: 'customInstance',
        },
      },
    },
    input: {
      target:  env.VITE_API_URL ? `${env.VITE_API_URL}/v3/api-docs` : 'http://localhost:8080/v3/api-docs',
    },
  },
})
