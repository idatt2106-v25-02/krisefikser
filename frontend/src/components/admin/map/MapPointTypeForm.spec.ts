import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import MapPointTypeForm from './MapPointTypeForm.vue'

describe('MapPointTypeForm', () => {
  function mountForm() {
    return mount(MapPointTypeForm, {
      props: {
        title: 'Ny kartpunkttype',
        modelValue: {
          title: 'Trygghetspunkt',
          description: 'Beskrivelse',
          iconUrl: 'icon.png',
          openingTime: '08-16',
        },
      },
      global: {
        stubs: {
          IconPicker: {
            props: ['modelValue'],
            emits: ['update:modelValue'],
            template:
              '<input data-testid="icon-picker" :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />',
          },
        },
      },
    })
  }

  it('renders editable fields and accepts user input', async () => {
    const wrapper = mountForm()

    await wrapper.get('#map-point-type-title').setValue('Oppdatert tittel')
    await wrapper.get('#map-point-type-description').setValue('Oppdatert beskrivelse')
    await wrapper.get('[data-testid="icon-picker"]').setValue('new-icon.svg')
    await wrapper.get('#map-point-type-opening-time').setValue('10-18')

    expect((wrapper.get('#map-point-type-title').element as HTMLInputElement).value).toBe(
      'Oppdatert tittel',
    )
    expect((wrapper.get('#map-point-type-description').element as HTMLTextAreaElement).value).toBe(
      'Oppdatert beskrivelse',
    )
    expect((wrapper.get('[data-testid="icon-picker"]').element as HTMLInputElement).value).toBe(
      'new-icon.svg',
    )
    expect((wrapper.get('#map-point-type-opening-time').element as HTMLInputElement).value).toBe(
      '10-18',
    )
  })

  it('emits submit and cancel actions', async () => {
    const wrapper = mountForm()

    const cancelButton = wrapper.findAll('button').find((button) => button.text().includes('Avbryt'))
    const saveButton = wrapper.findAll('button').find((button) => button.text().includes('Lagre'))
    expect(cancelButton).toBeTruthy()
    expect(saveButton).toBeTruthy()

    await cancelButton!.trigger('click')
    await saveButton!.trigger('click')

    expect(wrapper.emitted('cancel')).toBeTruthy()
    expect(wrapper.emitted('submit')).toBeTruthy()
  })
})
