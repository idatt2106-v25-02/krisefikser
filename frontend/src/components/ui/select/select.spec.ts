import { mount } from '@vue/test-utils'
import { describe, it, expect } from 'vitest'
import { nextTick } from 'vue'
import { Select, SelectTrigger, SelectValue, SelectContent, SelectItem } from '.'

describe('Select Components', () => {
  it('renders basic select structure', async () => {
    const wrapper = mount({
      components: {
        Select,
        SelectTrigger,
        SelectValue,
        SelectContent,
        SelectItem,
      },
      template: `
        <Select>
          <SelectTrigger>
            <SelectValue placeholder="Select an option" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="option1">Option 1</SelectItem>
            <SelectItem value="option2">Option 2</SelectItem>
          </SelectContent>
        </Select>
      `,
    })

    await nextTick()

    expect(wrapper.findComponent(Select).exists()).toBe(true)
    expect(wrapper.findComponent(SelectTrigger).exists()).toBe(true)
    expect(wrapper.findComponent(SelectValue).exists()).toBe(true)

    expect(wrapper.html()).toContain('Select an option')
  })

  it('SelectTrigger renders with correct classes', async () => {
    const wrapper = mount(Select, {
      slots: {
        default: '<SelectTrigger>Test Trigger</SelectTrigger>',
      },
      global: {
        components: { SelectTrigger },
      },
    })

    await nextTick()

    const trigger = wrapper.findComponent(SelectTrigger)
    expect(trigger.exists()).toBe(true)
    expect(trigger.attributes('class')).toContain('border-input')
  })

  it('SelectItem mounts within Select', async () => {
    const wrapper = mount(
      {
        components: {
          Select,
          SelectContent,
          SelectItem,
        },
        template: `
        <Select>
          <SelectContent>
            <SelectItem value="test-value">Test Option</SelectItem>
          </SelectContent>
        </Select>
      `,
        attachTo: document.body,
      },
      {
        global: {
          stubs: {
            teleport: false,
          },
        },
      },
    )

    await nextTick()

    expect(wrapper.findComponent(Select).exists()).toBe(true)
  })
})
