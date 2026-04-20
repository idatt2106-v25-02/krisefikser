import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import AddMemberDialog from '@/components/admin/users/AddMemberDialog.vue'

describe('AddMemberDialog', () => {
  it('emits add event with selected user and closes dialog', async () => {
    const wrapper = mount(AddMemberDialog, {
      props: {
        open: true,
        household: { id: 'h-1', name: 'Team A' },
      },
      global: {
        stubs: {
          Dialog: { template: '<div><slot /></div>' },
          DialogContent: { template: '<div><slot /></div>' },
          DialogHeader: { template: '<div><slot /></div>' },
          DialogTitle: { template: '<div><slot /></div>' },
          DialogDescription: { template: '<div><slot /></div>' },
          DialogFooter: { template: '<div><slot /></div>' },
          Button: { template: '<button @click="$emit(\'click\')"><slot /></button>' },
          UserSelect: {
            props: ['modelValue'],
            emits: ['update:modelValue'],
            template:
              '<input data-testid="user-select" @input="$emit(\'update:modelValue\', $event.target.value)" />',
          },
        },
      },
    })

    await wrapper.find('[data-testid="user-select"]').setValue('u-123')
    const buttons = wrapper.findAll('button')
    await buttons[1].trigger('click')

    expect(wrapper.emitted('add')).toEqual([[{ householdId: 'h-1', userId: 'u-123' }]])
    expect(wrapper.emitted('update:open')).toContainEqual([false])
  })
})
