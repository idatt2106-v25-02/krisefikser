import { createComponentWrapper } from '@/components/__tests__/test-utils'
import { describe, it, expect } from 'vitest'
import { Badge } from '.'

describe('Badge', () => {
  it('renders correctly with default props', () => {
    const wrapper = createComponentWrapper(Badge)
    expect(wrapper.exists()).toBe(true)
    expect(wrapper.classes()).toContain('inline-flex')
  })

  it('applies variant classes correctly', () => {
    const wrapper = createComponentWrapper(Badge, { variant: 'secondary' })
    expect(wrapper.classes()).toContain('bg-secondary')
  })

  it('renders slot content', () => {
    const slotContent = 'New'
    const wrapper = createComponentWrapper(Badge, {}, { default: slotContent })
    expect(wrapper.text()).toContain(slotContent)
  })

  it('renders custom class properly', () => {
    const customClass = 'custom-badge'
    const wrapper = createComponentWrapper(Badge, { class: customClass })
    expect(wrapper.classes()).toContain(customClass)
  })
})
