import { defineConfig } from 'orval'

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
      baseUrl: 'http://localhost:8080',
      override: {
        mutator: {
          path: './src/api/axios.ts',
          name: 'customInstance',
        },
      },
    },
    input: {
      target: 'http://localhost:8080/v3/api-docs',
    },
  },
})
