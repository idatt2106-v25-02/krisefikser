import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import SearchBar from './SearchBar.vue'

describe('SearchBar', () => {
  it('renders with provided value and placeholder', () => {
    const wrapper = mount(SearchBar, {
      props: {
        modelValue: 'Hermetikk',
        placeholder: 'Sok i varer',
      },
      global: {
        stubs: { Search: true },
      },
    })

    const input = wrapper.get('input')
    expect(input.element.value).toBe('Hermetikk')
    expect(input.attributes('placeholder')).toBe('Sok i varer')
  })

  it('emits update:modelValue on input', async () => {
    const wrapper = mount(SearchBar, {
      props: {
        modelValue: '',
      },
      global: {
        stubs: { Search: true },
      },
    })

    await wrapper.get('input').setValue('Vann')
    expect(wrapper.emitted('update:modelValue')?.[0]).toEqual(['Vann'])
  })
})
