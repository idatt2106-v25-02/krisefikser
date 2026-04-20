import { mount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'
import MapPointForm from './MapPointForm.vue'

vi.mock('@/api/generated/map-point-type/map-point-type', () => ({
  useGetAllMapPointTypes: vi.fn(() => ({
    data: [
      { id: 'type-1', title: 'Tilfluktsrom' },
      { id: 'type-2', title: 'Hjelpepunkt' },
    ],
  })),
}))

describe('MapPointForm', () => {
  function mountForm() {
    return mount(MapPointForm, {
      props: {
        title: 'Kartpunkt',
        modelValue: {
          latitude: 63.4,
          longitude: 10.3,
        },
      },
    })
  }

  it('renders map point types and emits submit flow', async () => {
    const wrapper = mountForm()

    await wrapper.findAll('button').find((button) => button.text().includes('Lagre'))!.trigger('click')

    expect(wrapper.text()).toContain('Tilfluktsrom')
    expect(wrapper.emitted('submit')).toBeTruthy()
    expect(wrapper.emitted('update:modelValue')).toBeTruthy()
  })

  it('emits start-map-selection when map picker button is clicked', async () => {
    const wrapper = mountForm()

    const mapButton = wrapper
      .findAll('button')
      .find((button) => button.text().includes('Velg plassering på kartet'))
    expect(mapButton).toBeTruthy()
    await mapButton!.trigger('click')

    expect(wrapper.emitted('start-map-selection')).toBeTruthy()
  })

  it('emits cancel when cancel button is clicked', async () => {
    const wrapper = mountForm()

    const cancelButton = wrapper.findAll('button').find((button) => button.text().includes('Avbryt'))
    expect(cancelButton).toBeTruthy()
    await cancelButton!.trigger('click')

    expect(wrapper.emitted('cancel')).toBeTruthy()
  })
})
