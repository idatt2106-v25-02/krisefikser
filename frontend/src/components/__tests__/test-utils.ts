import { mount } from '@vue/test-utils'

// Helper function to create consistent test wrappers
export function createComponentWrapper(component: any, props = {}, slots = {}) {
  return mount(component, {
    props,
    slots,
  })
}
