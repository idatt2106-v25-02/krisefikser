import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import EventForm from './EventForm.vue'

type EventUpdatePayload = [{ title?: string }]

describe('EventForm', () => {
  function mountForm() {
    return mount(EventForm, {
      props: {
        title: 'Ny hendelse',
        modelValue: {
          title: 'Eksisterende',
          description: 'Beskrivelse',
          radius: 100,
          level: 'GREEN',
          status: 'UPCOMING',
          latitude: 63.43,
          longitude: 10.39,
        },
      },
    })
  }

  it('emits submit and update:modelValue on save', async () => {
    const wrapper = mountForm()

    await wrapper.get('#event-title').setValue('Oppdatert navn')
    const saveButton = wrapper.findAll('button').find((button) => button.text().trim() === 'Lagre')
    expect(saveButton).toBeTruthy()
    await saveButton!.trigger('click')

    expect(wrapper.emitted('submit')).toBeTruthy()
    const updateEvent = wrapper.emitted('update:modelValue')
    expect(updateEvent).toBeTruthy()
    const firstPayload = (updateEvent?.[0] ?? []) as EventUpdatePayload
    expect(firstPayload[0].title).toBe('Oppdatert navn')
  })

  it('emits start-map-selection when map button is clicked', async () => {
    const wrapper = mountForm()

    const mapButton = wrapper
      .findAll('button')
      .find((button) => button.text().includes('Velg plassering på kartet'))
    expect(mapButton).toBeTruthy()
    await mapButton!.trigger('click')

    expect(wrapper.emitted('start-map-selection')).toBeTruthy()
  })

  it('resets local edits and emits cancel', async () => {
    const wrapper = mountForm()

    const titleInput = wrapper.get('#event-title')
    await titleInput.setValue('Midlertidig')

    const cancelButton = wrapper.findAll('button').find((button) => button.text().includes('Avbryt'))
    expect(cancelButton).toBeTruthy()
    await cancelButton!.trigger('click')

    expect(wrapper.emitted('cancel')).toBeTruthy()
    expect((wrapper.get('#event-title').element as HTMLInputElement).value).toBe('Eksisterende')
  })
})
