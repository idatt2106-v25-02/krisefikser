import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import Textarea from './Textarea.vue'

describe('Textarea', () => {
  it('renders initial model value', () => {
    const wrapper = mount(Textarea, {
      props: {
        modelValue: 'Initial tekst',
      },
    })

    expect(wrapper.element.tagName).toBe('TEXTAREA')
    expect((wrapper.element as HTMLTextAreaElement).value).toBe('Initial tekst')
  })

  it('emits update:modelValue when text changes', async () => {
    const wrapper = mount(Textarea, {
      props: {
        modelValue: '',
      },
    })

    await wrapper.setValue('Ny tekst')
    expect(wrapper.emitted('update:modelValue')?.[0]).toEqual(['Ny tekst'])
  })
})
