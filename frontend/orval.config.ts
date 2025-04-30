import { defineConfig } from 'orval'
import { config } from 'dotenv'

config()

const openApiUrl = process.env.VITE_API_URL ? `${process.env.VITE_API_URL}/v3/api-docs` : './openapi.json'

export default defineConfig({
  krisefikser: {
    output: {
      target: './src/api/generated',
      client: 'vue-query',
      mode: 'tags-split',
      schemas: './src/api/generated/model',
      mock: false,
      prettier: true,
      clean: false,
      baseUrl: process.env.VITE_API_URL || 'http://localhost:8080',
      override: {
        mutator: {
          path: './src/api/axios.ts',
          name: 'customInstance',
        },
      },
    },
    input: {
      target: openApiUrl,
    },
  },
})
