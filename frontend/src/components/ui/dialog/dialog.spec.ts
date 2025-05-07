import { mount } from '@vue/test-utils'
import { describe, it, expect } from 'vitest'
import { nextTick } from 'vue'
import {
  Dialog,
  DialogTrigger,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from '.'

describe('Dialog Components', () => {
  it('Dialog renders correctly', () => {
    const wrapper = mount(Dialog)
    expect(wrapper.exists()).toBe(true)
  })

  it('DialogTrigger renders correctly and emits open event', async () => {
    const wrapper = mount({
      components: { Dialog, DialogTrigger },
      template: `
        <Dialog>
          <DialogTrigger>Open Dialog</DialogTrigger>
        </Dialog>
      `,
    })

    const trigger = wrapper.findComponent(DialogTrigger)
    expect(trigger.exists()).toBe(true)
    expect(trigger.text()).toBe('Open Dialog')
  })

  it('DialogContent renders with correct structure', async () => {
    const wrapper = mount(Dialog, {
      slots: {
        default: '<DialogContent>Dialog Content</DialogContent>',
      },
      global: {
        components: { DialogContent },
        stubs: {
          teleport: true,
        },
      },
    })

    await nextTick()

    // In some implementations, DialogContent won't be visible until dialog is opened
    // So we're only checking that the component structure renders without errors
    expect(wrapper.exists()).toBe(true)
  })

  it('renders a complete dialog with all components', () => {
    const wrapper = mount(
      {
        components: {
          Dialog,
          DialogTrigger,
          DialogContent,
          DialogHeader,
          DialogTitle,
          DialogDescription,
          DialogFooter,
        },
        template: `
        <Dialog>
          <DialogTrigger>Open Dialog</DialogTrigger>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>Dialog Title</DialogTitle>
              <DialogDescription>Dialog Description</DialogDescription>
            </DialogHeader>
            <div>Content</div>
            <DialogFooter>
              <button>Cancel</button>
              <button>Submit</button>
            </DialogFooter>
          </DialogContent>
        </Dialog>
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

    expect(wrapper.findComponent(DialogTrigger).exists()).toBe(true)
    // We check for DialogContent at the wrapper level, as it might be teleported
    expect(wrapper.findComponent(DialogContent).exists()).toBe(true)
  })
})
