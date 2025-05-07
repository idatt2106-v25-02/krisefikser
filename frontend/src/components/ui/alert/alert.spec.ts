import { createComponentWrapper } from '@/components/__tests__/test-utils'
import { describe, it, expect } from 'vitest'
import { Alert, AlertDescription, AlertTitle } from '.'
import { mount } from '@vue/test-utils'

describe('Alert', () => {
  it('renders correctly with default props', () => {
    const wrapper = createComponentWrapper(Alert)
    expect(wrapper.exists()).toBe(true)
    expect(wrapper.attributes('role')).toBe('alert')
  })

  it('applies variant classes correctly', () => {
    const wrapper = createComponentWrapper(Alert, { variant: 'destructive' })
    expect(wrapper.classes()).toContain('text-destructive')
  })

  it('renders slot content', () => {
    const slotContent = 'Alert Content'
    const wrapper = createComponentWrapper(Alert, {}, { default: slotContent })
    expect(wrapper.text()).toContain(slotContent)
  })

  it('renders correctly with title and description', () => {
    const wrapper = mount({
      components: { Alert, AlertTitle, AlertDescription },
      template: `
        <Alert>
          <AlertTitle>Test Title</AlertTitle>
          <AlertDescription>Test Description</AlertDescription>
        </Alert>
      `,
    })

    expect(wrapper.findComponent(AlertTitle).exists()).toBe(true)
    expect(wrapper.findComponent(AlertTitle).text()).toBe('Test Title')
    expect(wrapper.findComponent(AlertDescription).exists()).toBe(true)
    expect(wrapper.findComponent(AlertDescription).text()).toBe('Test Description')
  })
})
