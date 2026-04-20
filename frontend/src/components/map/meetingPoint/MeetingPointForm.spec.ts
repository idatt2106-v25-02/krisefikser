import { mount } from '@vue/test-utils'
import { describe, expect, it, beforeEach, vi } from 'vitest'
import MeetingPointForm from './MeetingPointForm.vue'
import type { MeetingPointResponse } from '@/api/generated/model'

const refetchMock = vi.fn()
const createMutateMock = vi.fn()
const updateMutateMock = vi.fn()
const deleteMutateMock = vi.fn()

vi.mock('@/api/generated/meeting-points/meeting-points', () => ({
  useGetMeetingPoints: vi.fn(() => ({ refetch: refetchMock })),
  useCreateMeetingPoint: vi.fn((config?: { mutation?: { onSuccess?: () => void } }) => ({
    mutate: (payload: unknown) => {
      createMutateMock(payload)
      config?.mutation?.onSuccess?.()
    },
  })),
  useUpdateMeetingPoint: vi.fn((config?: { mutation?: { onSuccess?: () => void } }) => ({
    mutate: (payload: unknown) => {
      updateMutateMock(payload)
      config?.mutation?.onSuccess?.()
    },
  })),
  useDeleteMeetingPoint: vi.fn((config?: { mutation?: { onSuccess?: () => void } }) => ({
    mutate: async (payload: unknown) => {
      deleteMutateMock(payload)
      config?.mutation?.onSuccess?.()
    },
  })),
}))

type MeetingPointFormProps = {
  householdId: string
  point?: MeetingPointResponse
  position?: { lat: number; lng: number }
}

function mountForm(props: MeetingPointFormProps) {
  return mount(MeetingPointForm, {
    props,
    global: {
      stubs: {
        Input: {
          props: ['modelValue'],
          emits: ['update:modelValue'],
          template:
            '<input :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.valueAsNumber || $event.target.value)" />',
        },
      },
    },
  })
}

describe('MeetingPointForm', () => {
  beforeEach(() => {
    refetchMock.mockClear()
    createMutateMock.mockClear()
    updateMutateMock.mockClear()
    deleteMutateMock.mockClear()
  })

  it('creates a meeting point and emits saved/close', async () => {
    const wrapper = mountForm({
      householdId: 'house-1',
      position: { lat: 63.43, lng: 10.39 },
    })

    const inputs = wrapper.findAll('input')
    await inputs[0].setValue('Skole')
    await inputs[1].setValue('Ved inngangen')
    await inputs[2].setValue('63.43')
    await inputs[3].setValue('10.39')
    await wrapper.find('form').trigger('submit.prevent')

    expect(createMutateMock).toHaveBeenCalledTimes(1)
    expect(createMutateMock).toHaveBeenCalledWith({
      householdId: 'house-1',
      data: {
        name: 'Skole',
        description: 'Ved inngangen',
        latitude: 63.43,
        longitude: 10.39,
      },
    })
    expect(refetchMock).toHaveBeenCalledTimes(1)
    expect(wrapper.emitted('saved')).toBeTruthy()
    expect(wrapper.emitted('close')).toBeTruthy()
  })

  it('updates an existing meeting point when point prop is provided', async () => {
    const wrapper = mountForm({
      householdId: 'house-1',
      point: {
        id: 'point-1',
        name: 'Old',
        description: 'Old desc',
        latitude: 63.4,
        longitude: 10.3,
      },
    })

    const inputs = wrapper.findAll('input')
    await inputs[0].setValue('Nytt navn')
    await inputs[1].setValue('Ny beskrivelse')
    await inputs[2].setValue('63.5')
    await inputs[3].setValue('10.4')
    await wrapper.find('form').trigger('submit.prevent')

    expect(updateMutateMock).toHaveBeenCalledTimes(1)
    expect(updateMutateMock).toHaveBeenCalledWith({
      householdId: 'house-1',
      id: 'point-1',
      data: {
        name: 'Nytt navn',
        description: 'Ny beskrivelse',
        latitude: 63.5,
        longitude: 10.4,
      },
    })
    expect(refetchMock).toHaveBeenCalledTimes(1)
  })

  it('deletes an existing meeting point and emits close', async () => {
    const wrapper = mountForm({
      householdId: 'house-1',
      point: {
        id: 'point-2',
        name: 'To delete',
        description: 'desc',
        latitude: 63.4,
        longitude: 10.3,
      },
    })

    const deleteButton = wrapper.findAll('button').find((button) => button.text().includes('Slett'))
    expect(deleteButton).toBeTruthy()
    await deleteButton!.trigger('click')

    expect(deleteMutateMock).toHaveBeenCalledWith({
      householdId: 'house-1',
      id: 'point-2',
    })
    expect(refetchMock).toHaveBeenCalledTimes(1)
    expect(wrapper.emitted('close')).toBeTruthy()
  })
})
