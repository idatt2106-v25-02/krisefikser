import { mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ref } from 'vue'
import HouseholdDialogs from './HouseholdDialogs.vue'

const invalidateQueriesMock = vi.fn()
const toastMock = vi.fn()

vi.mock('@tanstack/vue-query', () => ({
  useQueryClient: vi.fn(() => ({
    invalidateQueries: invalidateQueriesMock,
  })),
}))

vi.mock('@/stores/auth/useAuthStore', () => ({
  useAuthStore: vi.fn(() => ({
    isAuthenticated: true,
  })),
}))

vi.mock('@/components/ui/toast/use-toast', () => ({
  useToast: vi.fn(() => ({
    toast: toastMock,
  })),
}))

const householdRef = ref({
  id: 'h-1',
  name: 'Familien Test',
  address: 'Testveien 1',
  postalCode: '7010',
  city: 'Trondheim',
  latitude: 63.43,
  longitude: 10.39,
  meetingPlaces: [],
})

vi.mock('@/api/generated/household/household', () => ({
  getGetActiveHouseholdQueryKey: vi.fn(() => ['getActiveHousehold']),
  useGetAllUserHouseholds: vi.fn(() => ({
    data: ref([{ id: 'h-1', name: 'Familien Test' }]),
  })),
  useGetActiveHousehold: vi.fn(() => ({
    data: householdRef,
  })),
  useUpdateActiveHousehold: vi.fn(() => ({
    mutateAsync: vi.fn(),
  })),
  useSetActiveHousehold: vi.fn(() => ({
    mutate: vi.fn(),
  })),
  useAddGuestToHousehold: vi.fn(() => ({
    mutate: vi.fn(),
    isPending: ref(false),
  })),
}))

vi.mock('@/api/generated/household-invite-controller/household-invite-controller', () => ({
  useCreateInvite: vi.fn(() => ({
    mutate: vi.fn(),
  })),
}))

describe('HouseholdDialogs coverage smoke', () => {
  beforeEach(() => {
    invalidateQueriesMock.mockClear()
    toastMock.mockClear()
  })

  it('renders dialogs when open props are true', async () => {
    const wrapper = mount(HouseholdDialogs, {
      props: {
        isEditDialogOpen: true,
        isAddMemberDialogOpen: true,
        isMeetingMapDialogOpen: true,
        isChangeHouseholdDialogOpen: true,
        isPreparednessInfoDialogOpen: true,
      },
      global: {
        stubs: {
          Dialog: { template: '<div><slot /></div>' },
          DialogContent: { template: '<div><slot /></div>' },
          DialogDescription: { template: '<div><slot /></div>' },
          DialogHeader: { template: '<div><slot /></div>' },
          DialogTitle: { template: '<div><slot /></div>' },
          DialogFooter: { template: '<div><slot /></div>' },
          Button: { template: '<button @click="$emit(\'click\')"><slot /></button>' },
          Input: { template: '<input />' },
          Form: { template: '<form><slot /></form>' },
          FormControl: { template: '<div><slot /></div>' },
          FormField: { template: '<div><slot :field=\"{}\" :errorMessage=\"\'\'\" :componentField=\"{}\" /></div>' },
          FormItem: { template: '<div><slot /></div>' },
          FormLabel: { template: '<label><slot /></label>' },
          FormMessage: { template: '<div><slot /></div>' },
          HouseholdMeetingMap: { template: '<div>HouseholdMeetingMap</div>' },
          PreparednessInfoDialog: { template: '<div>PreparednessInfoDialog</div>' },
          CheckCircle: true,
        },
      },
    })

    expect(wrapper.text()).toContain('Endre husstandsinformasjon')
    expect(wrapper.text()).toContain('Legg til medlem')
    expect(wrapper.text()).toContain('Bytt aktiv husstand')
    expect(wrapper.text()).toContain('Møtepunkter')
    expect(wrapper.text()).toContain('PreparednessInfoDialog')
  })
})
