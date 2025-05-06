import { mount } from '@vue/test-utils'
import { describe, it, expect, vi } from 'vitest'
import { Form, FormField, FormItem, FormLabel, FormControl, FormDescription, FormMessage } from '.'
import { Input } from '@/components/ui/input'

describe('Form Components', () => {
  it('renders basic form structure', async () => {
    const mockForm = {
      handleSubmit: vi.fn(),
      values: { username: 'test' },
      errors: {},
      resetForm: vi.fn(),
      submitCount: 0,
      meta: { valid: true },
    }

    const wrapper = mount({
      components: {
        Form,
        FormField,
        FormItem,
        FormLabel,
        FormControl,
        FormDescription,
        FormMessage,
        Input,
      },
      setup() {
        return { mockForm }
      },
      template: `
        <Form>
          <FormField name="username">
            <FormItem>
              <FormLabel>Username</FormLabel>
              <FormControl>
                <Input placeholder="Enter username" />
              </FormControl>
              <FormDescription>This is your public display name.</FormDescription>
              <FormMessage />
            </FormItem>
          </FormField>
        </Form>
      `,
    })

    // Check that all components render correctly
    expect(wrapper.findComponent(Form).exists()).toBe(true)
    expect(wrapper.findComponent(FormField).exists()).toBe(true)
    expect(wrapper.findComponent(FormItem).exists()).toBe(true)
    expect(wrapper.findComponent(FormLabel).exists()).toBe(true)
    expect(wrapper.findComponent(FormLabel).text()).toBe('Username')
    expect(wrapper.findComponent(FormControl).exists()).toBe(true)
    expect(wrapper.findComponent(Input).exists()).toBe(true)
    expect(wrapper.findComponent(FormDescription).exists()).toBe(true)
    expect(wrapper.findComponent(FormDescription).text()).toBe('This is your public display name.')
    expect(wrapper.findComponent(FormMessage).exists()).toBe(true)
  })
})
