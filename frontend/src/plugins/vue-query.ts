import { VueQueryPlugin } from '@tanstack/vue-query';
import type { App } from 'vue';

export function setupVueQuery(app: App) {
  app.use(VueQueryPlugin, {
    queryClientConfig: {
      defaultOptions: {
        queries: {
          staleTime: 10000, // 10 seconds
          refetchOnWindowFocus: false,
        },
      },
    },
  });
}