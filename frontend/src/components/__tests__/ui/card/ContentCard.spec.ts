import { mount } from '@vue/test-utils'
import { describe, it, expect } from 'vitest'
import ContentCard from '@/components/ui/card/ContentCard.vue'

describe('ContentCard', () => {
  const mountComponent = (props = {}, slots = {}) => {
    return mount(ContentCard, {
      props,
      slots
    })
  }



  it('renders without padding when noPadding is true', () => {
    const wrapper = mountComponent({ noPadding: true })
    const innerDiv = wrapper.find('div > div')
    expect(innerDiv.classes()).not.toContain('p-4')
  })

  it('renders slot content', () => {
    const slotContent = 'Test Content'
    const wrapper = mountComponent({}, {
      default: slotContent
    })
    expect(wrapper.text()).toContain(slotContent)
  })

  it('has correct base classes', () => {
    const wrapper = mountComponent()
    const outerDiv = wrapper.find('div')
    expect(outerDiv.classes()).toContain('bg-white')
    expect(outerDiv.classes()).toContain('rounded-lg')
    expect(outerDiv.classes()).toContain('shadow')
    expect(outerDiv.classes()).toContain('overflow-hidden')
    expect(outerDiv.classes()).toContain('mb-6')
  })

  it('renders complex slot content', () => {
    const wrapper = mountComponent({}, {
      default: `
        <div class="test-content">
          <h1>Title</h1>
          <p>Description</p>
        </div>
      `
    })
    expect(wrapper.find('.test-content').exists()).toBe(true)
    expect(wrapper.text()).toContain('Title')
    expect(wrapper.text()).toContain('Description')
  })
})
