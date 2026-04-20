import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import type { HouseholdResponse } from '@/api/generated/model'
import EditHouseholdDialog from './EditHouseholdDialog.vue'

function mountDialog() {
  const household: HouseholdResponse = {
    id: 'h-1',
    name: 'Familien Hansen',
    latitude: 63.4,
    longitude: 10.3,
    address: 'Testveien 1',
    city: 'Trondheim',
    postalCode: '7010',
    owner: {
      id: 'u-1',
      firstName: 'Test',
      lastName: 'Bruker',
      email: 'test@example.com',
      roles: ['USER'],
    },
    members: [],
    guests: [],
    createdAt: '2099-01-01T00:00:00Z',
  }

  return mount(EditHouseholdDialog, {
    props: {
      open: true,
      household,
    },
    global: {
      stubs: {
        Dialog: { template: '<div><slot /></div>' },
        DialogContent: { template: '<div><slot /></div>' },
        DialogDescription: { template: '<div><slot /></div>' },
        DialogFooter: { template: '<div><slot /></div>' },
        DialogHeader: { template: '<div><slot /></div>' },
        DialogTitle: { template: '<div><slot /></div>' },
        Button: { template: '<button @click="$emit(\'click\')"><slot /></button>' },
      },
    },
  })
}

describe('EditHouseholdDialog', () => {
  it('preloads household values into form inputs', () => {
    const wrapper = mountDialog()

    expect((wrapper.get('input[type="text"]').element as HTMLInputElement).value).toBe('Familien Hansen')
    expect(wrapper.text()).toContain('Rediger husholdning')
  })

  it('emits save and closes dialog when save is clicked', async () => {
    const wrapper = mountDialog()

    const textInputs = wrapper.findAll('input[type="text"]')
    await textInputs[0].setValue('Nytt navn')
    await textInputs[1].setValue('Ny adresse')

    const saveButton = wrapper.findAll('button').find((button) => button.text().includes('Lagre endringer'))
    expect(saveButton).toBeTruthy()
    await saveButton!.trigger('click')

    expect(wrapper.emitted('save')).toBeTruthy()
    expect(wrapper.emitted('update:open')).toBeTruthy()
    expect(wrapper.emitted('save')?.[0]?.[0]).toMatchObject({
      id: 'h-1',
      data: {
        name: 'Nytt navn',
        address: 'Ny adresse',
      },
    })
  })

  it('emits update:open false when cancel is clicked', async () => {
    const wrapper = mountDialog()

    const cancelButton = wrapper.findAll('button').find((button) => button.text().includes('Avbryt'))
    expect(cancelButton).toBeTruthy()
    await cancelButton!.trigger('click')

    expect(wrapper.emitted('update:open')?.[0]).toEqual([false])
  })
})
