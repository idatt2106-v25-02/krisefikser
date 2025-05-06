import { createComponentWrapper } from '@/components/__tests__/test-utils'
import { describe, it, expect } from 'vitest'
import { Button } from '.'

describe('Button', () => {
  it('renders correctly with default props', () => {
    const wrapper = createComponentWrapper(Button)
    expect(wrapper.exists()).toBe(true)
    expect(wrapper.classes()).toContain('inline-flex')
  })

  it('applies variant classes correctly', () => {
    const wrapper = createComponentWrapper(Button, { variant: 'destructive' })
    expect(wrapper.classes()).toContain('bg-destructive')
  })

  it('applies size classes correctly', () => {
    const wrapper = createComponentWrapper(Button, { size: 'sm' })
    expect(wrapper.classes()).toContain('h-8')
  })

  it('renders custom class properly', () => {
    const customClass = 'test-class'
    const wrapper = createComponentWrapper(Button, { class: customClass })
    expect(wrapper.classes()).toContain(customClass)
  })

  it('renders slot content', () => {
    const slotContent = 'Button Text'
    const wrapper = createComponentWrapper(Button, {}, { default: slotContent })
    expect(wrapper.text()).toContain(slotContent)
  })

  it('disables the button correctly', () => {
    const wrapper = createComponentWrapper(Button, { disabled: true })
    expect(wrapper.attributes('disabled')).toBeDefined()
  })

  it('passes events to underlying button element', async () => {
    const wrapper = createComponentWrapper(Button)
    await wrapper.trigger('click')
    expect(wrapper.emitted('click')).toBeTruthy()
  })
})
