import { mount } from '@vue/test-utils'
import { describe, it, expect } from 'vitest'
import { nextTick } from 'vue'
import { DropdownMenu, DropdownMenuTrigger, DropdownMenuContent } from '.'

describe('DropdownMenu Components', () => {
  it('renders basic dropdown structure', async () => {
    const wrapper = mount(
      {
        components: {
          DropdownMenu,
          DropdownMenuTrigger,
          DropdownMenuContent,
        },
        template: `
        <DropdownMenu>
          <DropdownMenuTrigger>Menu</DropdownMenuTrigger>
          <DropdownMenuContent>
            <div>Item 1</div>
            <div>Item 2</div>
          </DropdownMenuContent>
        </DropdownMenu>
      `,
      },
      {
        global: {
          stubs: {
            teleport: true,
          },
        },
      },
    )

    await nextTick()

    expect(wrapper.findComponent(DropdownMenu).exists()).toBe(true)
    expect(wrapper.findComponent(DropdownMenuTrigger).exists()).toBe(true)
    expect(wrapper.findComponent(DropdownMenuTrigger).text()).toBe('Menu')
    expect(wrapper.findComponent(DropdownMenuContent).exists()).toBe(true)
  })
})
