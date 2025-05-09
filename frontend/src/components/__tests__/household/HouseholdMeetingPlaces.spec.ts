import { mount } from '@vue/test-utils'
import { describe, it, expect, vi } from 'vitest'
import HouseholdMeetingPlaces from '@/components/household/HouseholdMeetingPlaces.vue'
import { createRouter, createWebHistory } from 'vue-router'
import { useGetMeetingPoints } from '@/api/generated/meeting-points/meeting-points'
import { computed, ref } from 'vue'
import type { DataTag, QueryKey, UseQueryReturnType } from '@tanstack/vue-query'

type MockQueryResult = UseQueryReturnType<unknown, unknown> & { queryKey: DataTag<QueryKey, unknown, unknown> }

const createMockQueryResult = (data: unknown = []): MockQueryResult => {
  const result = {
    data: ref(data),
    dataUpdatedAt: computed(() => Date.now()),
    error: computed(() => null),
    errorUpdatedAt: computed(() => Date.now()),
    failureCount: computed(() => 0),
    failureReason: computed(() => null),
    errorUpdateCount: computed(() => 0),
    isError: computed(() => false),
    isFetched: computed(() => true),
    isFetchedAfterMount: computed(() => true),
    isFetching: computed(() => false),
    isInitialLoading: computed(() => false),
    isLoading: computed(() => false),
    isLoadingError: computed(() => false),
    isPaused: computed(() => false),
    isPending: computed(() => false),
    isPlaceholderData: computed(() => false),
    isRefetchError: computed(() => false),
    isRefetching: computed(() => false),
    isStale: computed(() => false),
    isSuccess: computed(() => true),
    refetch: vi.fn(),
    status: computed(() => 'success' as const),
    fetchStatus: computed(() => 'idle' as const),
    promise: computed(() => Promise.resolve()),
    queryKey: ['meeting-points'] as unknown as DataTag<QueryKey, unknown, unknown>,
    suspense: () => Promise.resolve({ data, status: 'success' })
  }

  return result as unknown as MockQueryResult
}

// Mock the API hook
vi.mock('@/api/generated/meeting-points/meeting-points', () => ({
  useGetMeetingPoints: vi.fn(() => createMockQueryResult([]))
}))

// Mock router
const router = createRouter({
  history: createWebHistory(),
  routes: [{ path: '/kart', component: {} }]
})

describe('HouseholdMeetingPlaces', () => {
  const defaultProps = {
    householdId: 'test-id',
    householdLatitude: 59.9139,
    householdLongitude: 10.7522
  }

  const mockMeetingPlaces = [
    {
      id: '1',
      name: 'Meeting Place 1',
      description: 'Description 1',
      latitude: 59.9139,
      longitude: 10.7522,
      householdId: 'test-id'
    },
    {
      id: '2',
      name: 'Meeting Place 2',
      description: 'Description 2',
      latitude: 59.9140,
      longitude: 10.7523,
      householdId: 'test-id'
    }
  ]

  const mountComponent = (props = defaultProps) => {
    return mount(HouseholdMeetingPlaces, {
      props,
      global: {
        plugins: [router],
        stubs: {
          'AlertCircle': true,
          'MapIcon': true,
          'Dialog': true,
          'DialogContent': true,
          'DialogHeader': true,
          'DialogTitle': true,
          'DialogDescription': true,
          'HouseholdMeetingMap': true
        }
      }
    })
  }

  it('displays error message when householdId is missing', () => {
    const wrapper = mountComponent({ ...defaultProps, householdId: '' })
    expect(wrapper.text()).toContain('Feil: householdId mangler')
  })

  it('displays "no meeting places" message when list is empty', () => {
    const wrapper = mountComponent()
    expect(wrapper.text()).toContain('Ingen møteplasser registrert')
  })

  it('renders meeting places when available', async () => {
    vi.mocked(useGetMeetingPoints).mockReturnValue(createMockQueryResult(mockMeetingPlaces))

    const wrapper = mountComponent()

    // Wait for component to update
    await wrapper.vm.$nextTick()

    mockMeetingPlaces.forEach(place => {
      expect(wrapper.text()).toContain(place.name)
      expect(wrapper.text()).toContain(place.description)
    })
  })

  it('contains link to map view', () => {
    const wrapper = mountComponent()
    const mapLink = wrapper.find('a[href="/kart"]')
    expect(mapLink.exists()).toBe(true)
    expect(mapLink.text()).toContain('Vis kart')
  })

  it('opens map dialog when meeting place is clicked', async () => {
    vi.mocked(useGetMeetingPoints).mockReturnValue(createMockQueryResult(mockMeetingPlaces))

    const wrapper = mountComponent()
    await wrapper.vm.$nextTick()

    const placeElement = wrapper.find('.divide-y > div')
    await placeElement.trigger('click')

    // Check if dialog is rendered
    expect(wrapper.findComponent({ name: 'Dialog' }).exists()).toBe(true)
  })

  it('exposes refreshMeetingPlaces method', () => {
    const wrapper = mountComponent()
    expect(wrapper.vm.refreshMeetingPlaces).toBeDefined()
  })
})
