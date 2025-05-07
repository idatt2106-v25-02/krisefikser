import { createComponentWrapper } from '@/components/__tests__/test-utils'
import { describe, it, expect, vi } from 'vitest'
import AddMemberDialog from '../../components/admin/AddMemberDialog.vue'

vi.mock('@/components/admin/UserSelect.vue', () => ({
  default: {
    name: 'UserSelect',
    template: '<div data-testid="user-select">User Select Component</div>',
    props: ['modelValue'],
    emits: ['update:modelValue'],
  },
}))

vi.mock('@/components/ui/dialog', () => ({
  Dialog: {
    name: 'Dialog',
    template: '<div class="dialog"><slot></slot></div>',
    props: ['open'],
    emits: ['update:open'],
  },
  DialogContent: {
    name: 'DialogContent',
    template: '<div class="dialog-content sm:max-w-md"><slot></slot></div>',
  },
  DialogHeader: {
    name: 'DialogHeader',
    template: '<div class="dialog-header"><slot></slot></div>',
  },
  DialogTitle: {
    name: 'DialogTitle',
    template: '<div class="dialog-title"><slot></slot></div>',
  },
  DialogDescription: {
    name: 'DialogDescription',
    template: '<div class="dialog-description"><slot></slot></div>',
  },
  DialogFooter: {
    name: 'DialogFooter',
    template: '<div class="dialog-footer"><slot></slot></div>',
  },
}))

vi.mock('@/components/ui/button', () => ({
  Button: {
    name: 'Button',
    template: `
      <button
        :data-variant="variant"
        :data-disabled="disabled"
        @click="$emit('click')">
        <slot></slot>
      </button>
    `,
    props: {
      variant: { type: String, default: 'default' },
      disabled: { type: Boolean, default: false },
    },
    emits: ['click'],
  },
}))

describe('AddMemberDialog', () => {
  const mockHousehold = {
    id: '123',
    name: 'Test Household',
    address: 'Test Address',
    members: [],
  }

  it('renders dialog when open prop is true', () => {
    const wrapper = createComponentWrapper(AddMemberDialog, {
      household: mockHousehold,
      open: true,
    })

    expect(wrapper.find('.dialog-content').exists()).toBe(true)
    expect(wrapper.text()).toContain('Legg til medlem')
    expect(wrapper.text()).toContain('Test Household')
  })

  it('emits close event when cancel button is clicked', async () => {
    const wrapper = createComponentWrapper(AddMemberDialog, {
      household: mockHousehold,
      open: true,
    })

    await wrapper.find('button[data-variant="outline"]').trigger('click')
    expect(wrapper.emitted('update:open')).toBeTruthy()
    expect(wrapper.emitted('update:open')?.[0]).toEqual([false])
  })

  it('emits add event with correct data when add button is clicked', async () => {
    const wrapper = createComponentWrapper(AddMemberDialog, {
      household: mockHousehold,
      open: true,
    })

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const vm = wrapper.vm as any

    vm.selectedUserId = 'user123'
    await wrapper.vm.$nextTick()

    await wrapper.find('button[data-variant="default"]').trigger('click')

    expect(wrapper.emitted('add')).toBeTruthy()
    expect(wrapper.emitted('add')?.[0]).toEqual([
      {
        householdId: '123',
        userId: 'user123',
      },
    ])
    expect(wrapper.emitted('update:open')).toBeTruthy()
    expect(wrapper.emitted('update:open')?.[0]).toEqual([false])
  })

  it('disables add button when no user is selected', () => {
    const wrapper = createComponentWrapper(AddMemberDialog, {
      household: mockHousehold,
      open: true,
    })

    const addButton = wrapper.find('button[data-variant="default"]')
    expect(addButton.attributes('data-disabled')).toBe('true')
  })
})
