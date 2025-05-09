import { mount } from '@vue/test-utils'
import { describe, it, expect, vi } from 'vitest'
import Badge from '@/components/ui/badge/Badge.vue'
import { Primitive } from 'reka-ui'

// Mock reka-ui component
vi.mock('reka-ui', () => ({
  Primitive: {
    name: 'Primitive',
    template: '<div><slot /></div>'
  }
}))

describe('Badge', () => {
  const mountComponent = (props = {}, slots = {}) => {
    return mount(Badge, {
      props,
      slots,
      global: {
        stubs: {
          Primitive
        }
      }
    })
  }

  it('renders correctly with default variant', () => {
    const wrapper = mountComponent()
    expect(wrapper.exists()).toBe(true)
    expect(wrapper.attributes('data-slot')).toBe('badge')
    expect(wrapper.classes()).toContain('bg-primary')
    expect(wrapper.classes()).toContain('text-primary-foreground')
  })

  it('applies secondary variant classes correctly', () => {
    const wrapper = mountComponent({ variant: 'secondary' })
    expect(wrapper.classes()).toContain('bg-secondary')
    expect(wrapper.classes()).toContain('text-secondary-foreground')
  })

  it('applies destructive variant classes correctly', () => {
    const wrapper = mountComponent({ variant: 'destructive' })
    expect(wrapper.classes()).toContain('bg-destructive')
    expect(wrapper.classes()).toContain('text-white')
  })

  it('applies outline variant classes correctly', () => {
    const wrapper = mountComponent({ variant: 'outline' })
    expect(wrapper.classes()).toContain('text-foreground')
  })

  it('merges custom classes with variant classes', () => {
    const customClass = 'custom-badge'
    const wrapper = mountComponent({
      variant: 'default',
      class: customClass
    })
    expect(wrapper.classes()).toContain('bg-primary')
    expect(wrapper.classes()).toContain(customClass)
  })

  it('renders slot content', () => {
    const slotContent = 'Badge Text'
    const wrapper = mountComponent({}, {
      default: slotContent
    })
    expect(wrapper.text()).toContain(slotContent)
  })

  it('renders complex slot content', () => {
    const wrapper = mountComponent({}, {
      default: `
        <span class="icon">🔥</span>
        <span class="text">Hot</span>
      `
    })
    expect(wrapper.find('.icon').exists()).toBe(true)
    expect(wrapper.find('.text').exists()).toBe(true)
    expect(wrapper.text()).toContain('🔥')
    expect(wrapper.text()).toContain('Hot')
  })

  it('has correct base styles', () => {
    const wrapper = mountComponent()
    const baseClasses = [
      'inline-flex',
      'items-center',
      'justify-center',
      'rounded-md',
      'border',
      'text-xs',
      'font-medium',
      'w-fit',
      'whitespace-nowrap',
      'shrink-0'
    ]
    baseClasses.forEach(className => {
      expect(wrapper.classes()).toContain(className)
    })
  })

  it('forwards native HTML attributes', () => {
    const wrapper = mountComponent({
      id: 'test-badge',
      'aria-label': 'Status Badge',
      role: 'status'
    })
    expect(wrapper.attributes('id')).toBe('test-badge')
    expect(wrapper.attributes('aria-label')).toBe('Status Badge')
    expect(wrapper.attributes('role')).toBe('status')
  })


})
