import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { VueQueryPlugin } from '@tanstack/vue-query'
import { QueryClient } from '@tanstack/vue-query'

// Create a fresh query client for tests
const createTestQueryClient = () =>
  new QueryClient({
    defaultOptions: {
      queries: {
        retry: false,
      },
    },
  })

// Helper function to create consistent test wrappers

interface WrapperOptions {
  stubs?: Record<string, boolean>
  mocks?: Record<string, unknown>
  [key: string]: unknown
}

export function createComponentWrapper(
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  component: any,
  props = {},
  slots = {},
  options: WrapperOptions = {},
) {
  // Create a fresh Pinia instance for each test
  const pinia = createPinia()
  setActivePinia(pinia)

  // Create a fresh Vue Query client for each test
  const queryClient = createTestQueryClient()

  // We won't create a router here to avoid conflicts with mocks
  return mount(component, {
    props,
    slots,
    global: {
      plugins: [pinia, [VueQueryPlugin, { queryClient }]],
      stubs: {
        'router-link': true,
        'router-view': true,
        ...options?.stubs,
      },
      mocks: {
        ...options?.mocks,
      },
    },
    ...options,
  })
}
