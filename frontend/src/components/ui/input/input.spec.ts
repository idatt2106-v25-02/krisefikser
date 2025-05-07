import { mount } from '@vue/test-utils'
import { describe, it, expect } from 'vitest'
import { Input } from '.'

describe('Input', () => {
  it('renders correctly', () => {
    const wrapper = mount(Input)
    expect(wrapper.exists()).toBe(true)
    expect(wrapper.element.tagName).toBe('INPUT')
  })

  it('accepts modelValue prop', async () => {
    const wrapper = mount(Input, {
      props: {
        modelValue: 'test value',
      },
    })
    expect(wrapper.element.value).toBe('test value')
  })

  it('emits update:modelValue event on input', async () => {
    const wrapper = mount(Input)
    await wrapper.setValue('new value')

    const emittedEvents = wrapper.emitted('update:modelValue')
    expect(emittedEvents).toBeTruthy()
    expect(emittedEvents && emittedEvents[0]).toEqual(['new value'])
  })

  it('applies custom class correctly', () => {
    const customClass = 'custom-input'
    const wrapper = mount(Input, {
      props: {
        class: customClass,
      },
    })
    expect(wrapper.classes()).toContain(customClass)
  })

  it('applies disabled attribute correctly', () => {
    const wrapper = mount(Input, {
      attrs: {
        disabled: true,
      },
    })
    expect(wrapper.attributes('disabled')).toBeDefined()
  })

  it('applies placeholder attribute correctly', () => {
    const placeholder = 'Enter text...'
    const wrapper = mount(Input, {
      attrs: {
        placeholder,
      },
    })
    expect(wrapper.attributes('placeholder')).toBe(placeholder)
  })
})
