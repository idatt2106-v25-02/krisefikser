import { mount } from '@vue/test-utils'

// Helper function to create consistent test wrappers
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export function createComponentWrapper(component: any, props = {}, slots = {}) {
  return mount(component, {
    props,
    slots,
  })
}
