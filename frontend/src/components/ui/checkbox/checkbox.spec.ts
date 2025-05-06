import { mount } from '@vue/test-utils'
import { describe, it, expect } from 'vitest'
import { nextTick } from 'vue'
import { Checkbox } from '.'

describe('Checkbox', () => {
  it('renders correctly with default props', () => {
    const wrapper = mount(Checkbox)
    expect(wrapper.exists()).toBe(true)
    expect(wrapper.classes()).toContain('peer')
  })

  it('can be checked and unchecked', async () => {
    const wrapper = mount(Checkbox, {
      props: {
        'onUpdate:modelValue': (e) => wrapper.setProps({ modelValue: e }),
      },
    })

    await wrapper.trigger('click')
    expect(wrapper.emitted('update:modelValue')).toBeTruthy()
  })

  it('applies disabled state correctly', () => {
    const wrapper = mount(Checkbox, {
      props: {
        disabled: true,
      },
    })
    expect(wrapper.classes()).toContain('disabled:opacity-50')
    expect(wrapper.attributes('disabled')).toBeDefined()
  })

  it('renders custom slot content', async () => {
    const wrapper = mount(Checkbox, {
      props: {
        modelValue: true,
      },
      slots: {
        default: '<span data-testid="custom-check">✓</span>',
      },
    })

    await nextTick()

    expect(wrapper.html()).toContain('✓')
    expect(wrapper.html()).toBeTruthy()
    expect(wrapper.isVisible()).toBe(true)
  })
})
