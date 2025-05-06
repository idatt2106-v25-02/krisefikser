import { mount } from '@vue/test-utils'
import { describe, it, expect } from 'vitest'

// Helper function to create consistent test wrappers
export function createComponentWrapper(component: any, props = {}, slots = {}) {
  return mount(component, {
    props,
    slots,
  })
}

export { describe, it, expect }
