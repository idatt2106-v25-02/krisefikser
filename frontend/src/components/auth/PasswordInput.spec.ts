import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import PasswordInput from './PasswordInput.vue'

function mountPasswordInput(componentFieldValue = '') {
  return mount(PasswordInput, {
    props: {
      name: 'password',
      componentField: {
        value: componentFieldValue,
      },
      showComplexityRequirements: true,
    },
    global: {
      stubs: {
        FormItem: { template: '<div><slot /></div>' },
        FormLabel: { template: '<label><slot /></label>' },
        FormControl: { template: '<div><slot /></div>' },
        FormMessage: { template: '<div><slot /></div>' },
        Input: {
          props: ['type', 'placeholder'],
          template: '<input :type="type" :placeholder="placeholder" />',
        },
        Eye: true,
        EyeOff: true,
        Lock: true,
      },
    },
  })
}

describe('PasswordInput', () => {
  it('toggles input type between password and text', async () => {
    const wrapper = mountPasswordInput('Abc123!!')

    const input = wrapper.get('input')
    expect(input.attributes('type')).toBe('password')

    await wrapper.get('button').trigger('click')
    expect(wrapper.get('input').attributes('type')).toBe('text')
  })

  it('shows weak password message for low complexity password', () => {
    const wrapper = mountPasswordInput('abc')

    expect(wrapper.text()).toContain('Svakt passord')
    expect(wrapper.text()).toContain('Minst 8 tegn')
  })

  it('shows strong password message for high complexity password', () => {
    const wrapper = mountPasswordInput('Abcd123!')

    expect(wrapper.text()).toContain('Sterkt passord')
  })
})
