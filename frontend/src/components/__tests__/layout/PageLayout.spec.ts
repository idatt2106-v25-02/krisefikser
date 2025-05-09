import { mount } from '@vue/test-utils'
import { describe, it, expect } from 'vitest'
import PageLayout from '@/components/layout/PageLayout.vue'
import { Home, ChevronRight } from 'lucide-vue-next'
import { createRouter, createWebHistory } from 'vue-router'

// Mock router
const router = createRouter({
  history: createWebHistory(),
  routes: [{ path: '/', name: 'home' }]
})

describe('PageLayout', () => {
  const defaultProps = {
    pageTitle: 'Test Page',
    sectionName: 'Test Section',
    iconComponent: Home
  }

  const mountComponent = (props = defaultProps) => {
    return mount(PageLayout, {
      props,
      global: {
        plugins: [router],
        stubs: {
          Home: true,
          ChevronRight: true
        }
      }
    })
  }

  it('renders with default props', () => {
    const wrapper = mountComponent()
    expect(wrapper.text()).toContain('Test Page')
    expect(wrapper.text()).toContain('Test Section')
    expect(wrapper.text()).toContain('Hjem')
  })

  it('applies default blue color classes when no color is specified', () => {
    const wrapper = mountComponent()
    const iconContainer = wrapper.find('.rounded-full')
    expect(iconContainer.classes()).toContain('bg-blue-100')
    expect(wrapper.find('.h-8').classes()).toContain('text-blue-600')
  })

  it('applies correct color classes for yellow variant', () => {
    const wrapper = mountComponent({
      ...defaultProps,
      iconBgColor: 'yellow'
    })
    const iconContainer = wrapper.find('.rounded-full')
    expect(iconContainer.classes()).toContain('bg-yellow-100')
    expect(wrapper.find('.h-8').classes()).toContain('text-yellow-600')
  })

  it('applies correct color classes for green variant', () => {
    const wrapper = mountComponent({
      ...defaultProps,
      iconBgColor: 'green'
    })
    const iconContainer = wrapper.find('.rounded-full')
    expect(iconContainer.classes()).toContain('bg-green-100')
    expect(wrapper.find('.h-8').classes()).toContain('text-green-600')
  })

  it('renders breadcrumb navigation correctly', () => {
    const wrapper = mountComponent()
    const breadcrumb = wrapper.find('nav[aria-label="Breadcrumb"]')
    expect(breadcrumb.exists()).toBe(true)
    expect(breadcrumb.find('a').attributes('href')).toBe('/')
  })

  it('renders custom icon component', () => {
    const CustomIcon = {
      template: '<div class="custom-icon">Custom Icon</div>'
    }
    const wrapper = mountComponent({
      ...defaultProps,
      iconComponent: CustomIcon
    })
    expect(wrapper.find('.custom-icon').exists()).toBe(true)
  })

  it('renders content in default slot', () => {
    const wrapper = mount(PageLayout, {
      props: defaultProps,
      slots: {
        default: '<div class="test-content">Test Content</div>'
      },
      global: {
        plugins: [router],
        stubs: {
          Home: true,
          ChevronRight: true
        }
      }
    })
    expect(wrapper.find('.test-content').exists()).toBe(true)
    expect(wrapper.text()).toContain('Test Content')
  })

  it('has correct layout structure', () => {
    const wrapper = mountComponent()
    expect(wrapper.find('.min-h-screen').exists()).toBe(true)
    expect(wrapper.find('.container').exists()).toBe(true)
    expect(wrapper.find('.flex-grow').exists()).toBe(true)
  })
})
