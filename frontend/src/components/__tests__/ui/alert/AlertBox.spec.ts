import { mount } from '@vue/test-utils'
import { describe, it, expect } from 'vitest'
import AlertBox from '@/components/ui/alert/AlertBox.vue'
import { defineComponent } from 'vue'

describe('AlertBox', () => {
  const CustomIcon = defineComponent({
    name: 'CustomIcon',
    template: '<div class="custom-icon h-5 w-5 mt-0.5 mr-2 flex-shrink-0">Custom Icon</div>'
  })

  const mountComponent = (props = {}, slots = {}) => {
    return mount(AlertBox, {
      props,
      slots
    })
  }

  it('renders with default type (info)', () => {
    const wrapper = mountComponent()
    expect(wrapper.classes()).toContain('bg-blue-50')
    expect(wrapper.find('p').classes()).toContain('text-blue-800')
  })

  it('renders warning type correctly', () => {
    const wrapper = mountComponent({ type: 'warning' })
    expect(wrapper.classes()).toContain('bg-yellow-50')
    expect(wrapper.find('p').classes()).toContain('text-yellow-800')
  })

  it('renders success type correctly', () => {
    const wrapper = mountComponent({ type: 'success' })
    expect(wrapper.classes()).toContain('bg-green-50')
    expect(wrapper.find('p').classes()).toContain('text-green-800')
  })

  it('renders error type correctly', () => {
    const wrapper = mountComponent({ type: 'error' })
    expect(wrapper.classes()).toContain('bg-red-50')
    expect(wrapper.find('p').classes()).toContain('text-red-800')
  })

  it('renders custom icon component', () => {
    const wrapper = mountComponent({
      type: 'info',
      iconComponent: CustomIcon
    })
    expect(wrapper.find('.custom-icon').exists()).toBe(true)
  })

  it('applies correct icon classes', () => {
    const wrapper = mountComponent({
      type: 'info',
      iconComponent: CustomIcon
    })
    const icon = wrapper.find('.custom-icon')
    expect(icon.classes()).toContain('h-5')
    expect(icon.classes()).toContain('w-5')
    expect(icon.classes()).toContain('mt-0.5')
    expect(icon.classes()).toContain('mr-2')
    expect(icon.classes()).toContain('flex-shrink-0')
    expect(wrapper.find('.flex').classes()).toContain('items-start')
  })

  it('renders slot content', () => {
    const slotContent = 'Alert message'
    const wrapper = mountComponent({}, {
      default: slotContent
    })
    expect(wrapper.text()).toContain(slotContent)
  })

  it('validates type prop', () => {
    const validator = AlertBox.props.type.validator
    expect(validator('info')).toBe(true)
    expect(validator('warning')).toBe(true)
    expect(validator('success')).toBe(true)
    expect(validator('error')).toBe(true)
    expect(validator('invalid')).toBe(false)
  })

  it('has correct base structure', () => {
    const wrapper = mountComponent()
    expect(wrapper.classes('p-4')).toBe(true)
    expect(wrapper.classes('rounded-md')).toBe(true)
    expect(wrapper.find('.flex').classes('items-start')).toBe(true)
  })


})
