import { defineConfig } from 'orval'
import { config } from 'dotenv'

config()

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
      baseUrl: process.env.VITE_API_URL,
      override: {
        mutator: {
          path: './src/api/axios.ts',
          name: 'customInstance',
        },
      },
    },
    input: {
      target: `${process.env.VITE_API_URL}/v3/api-docs`,
    },
  },
})
