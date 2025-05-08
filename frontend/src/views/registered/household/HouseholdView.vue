<script setup lang="ts">
import { ref, computed, watchEffect } from 'vue'
import router from '@/router'
import { useAuthStore } from '@/stores/auth/useAuthStore.ts'
import { useQueryClient } from '@tanstack/vue-query'
import axios from 'axios'

// Import API hooks
import {
  useGetAllUserHouseholds,
  useGetActiveHousehold,
  useLeaveHousehold,
  useDeleteHousehold,
  useAddGuestToHousehold,
  useRemoveGuestFromHousehold,
  useUpdateActiveHousehold,
  getGetActiveHouseholdQueryKey,
  useSetActiveHousehold,
} from '@/api/generated/household/household.ts'
import {
  useCreateInvite,
  useGetPendingInvitesForUser,
  useAcceptInvite,
  useDeclineInvite,
  useGetPendingInvitesForHousehold,
} from '@/api/generated/household-invite-controller/household-invite-controller.ts'
import { useGetInventorySummary, getGetInventorySummaryQueryKey } from '@/api/generated/item/item'
import { useToast } from '@/components/ui/toast/use-toast.ts'

// Import components
import HouseholdHeader from './components/HouseholdHeader.vue'
import HouseholdMembers from './components/HouseholdMembers.vue'
import HouseholdEmergencySuppliesCard from './components/HouseholdEmergencySuppliesCard.vue'
import HouseholdMeetingPlaces from './components/HouseholdMeetingPlaces.vue'
import HouseholdActions from './components/HouseholdActions.vue'
import HouseholdDialogs from './components/HouseholdDialogs.vue'
import InvitedPendingList from '@/components/household/InvitedPendingList.vue'

// Types
import type {
  HouseholdResponse,
  GuestResponse,
  HouseholdMemberResponse,
  CreateHouseholdRequest,
} from '@/api/generated/model'

interface ExtendedHouseholdResponse {
  id: string
  name: string
  address: string
  latitude: number
  longitude: number
  postalCode: string
  city: string
  owner: {
    id: string
    firstName: string
    lastName: string
    email: string
  }
  createdAt: string
  members: HouseholdMemberResponse[]
  guests: GuestResponse[]
  meetingPlaces?: Array<{
    id: string
    name: string
    address: string
    latitude: number
    longitude: number
    description?: string
    type: 'primary' | 'secondary'
    targetDays: number
  }>
  inventoryItems?: Array<{
    name: string
    expiryDate: string
    amount: number
    productType: {
      name: string
      unit: string
    }
  }>
}



const authStore = useAuthStore()
const { toast } = useToast()
const queryClient = useQueryClient()

// Dialog states
const isEditDialogOpen = ref(false)
const isAddMemberDialogOpen = ref(false)
const isMeetingMapDialogOpen = ref(false)
const isChangeHouseholdDialogOpen = ref(false)
const isPreparednessInfoDialogOpen = ref(false)

// Get households data
const {
  data: allHouseholds,
  isLoading: isLoadingAllHouseholds,
  refetch: refetchAllHouseholds,
} = useGetAllUserHouseholds({
  query: {
    enabled: authStore.isAuthenticated,
  },
})

// Get household data
const {
  data: household,
  isError: isErrorHousehold,
  error: errorHousehold,
  isLoading: isLoadingHousehold,
  refetch: refetchHousehold,
} = useGetActiveHousehold<ExtendedHouseholdResponse>({
  query: {
    retry: 0,
    enabled: authStore.isAuthenticated,
  },
})



// Add error handling watchEffect
watchEffect(() => {
  if (isErrorHousehold.value) {
    const err = errorHousehold.value as { response?: { status: number } }
    if (err?.response?.status === 404) {
      router.push('/bli-med-eller-opprett-husstand')
    }
  }
})





// Mutations
const { mutate: leaveHousehold } = useLeaveHousehold({
  mutation: {
    onSuccess: () => {
      router.push('/husstand')
    },
  },
})

const { mutate: deleteHousehold } = useDeleteHousehold({
  mutation: {
    onSuccess: () => {
      router.push('/husstand')
    },
  },
})

const { mutate: createInvite } = useCreateInvite({
  mutation: {
    onSuccess: () => {
      toast({
        title: 'Invitasjon sendt',
        description: 'Invitasjonen har blitt sendt til brukeren.',
      })
      isAddMemberDialogOpen.value = false
      refetchHouseholdInvites()
    },
    onError: (error) => {
      const err = error as Error
      toast({
        title: 'Feil',
        description: err.message || 'Kunne ikke sende invitasjon',
        variant: 'destructive',
      })
    },
  },
})

const { mutate: addGuest, isPending: isAddingGuest } = useAddGuestToHousehold({
  mutation: {
    onSuccess: () => {
      toast({
        title: 'Gjest lagt til',
        description: 'Gjesten har blitt lagt til i husstanden.',
      })
      refetchHousehold()
      queryClient.invalidateQueries({ queryKey: getGetInventorySummaryQueryKey() })
      isAddMemberDialogOpen.value = false
    },
    onError: (error: unknown) => {
      const errorMessage =
        (error as { response?: { data?: { message?: string } } })?.response?.data?.message ||
        (error as Error)?.message ||
        'Kunne ikke legge til gjest.'
      toast({
        title: 'Feil ved tillegging av gjest',
        description: errorMessage,
        variant: 'destructive',
      })
    },
  },
})

const { mutate: removeGuest, isPending: isRemovingGuest } = useRemoveGuestFromHousehold({
  mutation: {
    onSuccess: () => {
      toast({
        title: 'Gjest fjernet',
        description: 'Gjesten er fjernet fra husstanden.',
      })
      refetchHousehold()
      queryClient.invalidateQueries({ queryKey: getGetInventorySummaryQueryKey() })
    },
    onError: (error: unknown) => {
      const errorMessage =
        (error as { response?: { data?: { message?: string } } })?.response?.data?.message ||
        (error as Error)?.message ||
        'Kunne ikke fjerne gjest.'
      toast({
        title: 'Feil ved fjerning av gjest',
        description: errorMessage,
        variant: 'destructive',
      })
    },
  },
})

const { mutateAsync: updateActiveHousehold } = useUpdateActiveHousehold({
  mutation: {
    onSuccess: (updatedHouseholdData) => {
      queryClient.setQueryData(getGetActiveHouseholdQueryKey(), updatedHouseholdData)
      isEditDialogOpen.value = false
      toast({
        title: 'Husstand oppdatert',
        description: 'Husstandsinformasjonen ble oppdatert.',
      })
    },
    onError: (error) => {
      toast({
        title: 'Feil',
        description: (error as unknown as Error)?.message || 'Kunne ikke oppdatere husstand.',
        variant: 'destructive',
      })
    },
  },
})

const { mutate: setActiveHousehold, isPending: isSettingActiveHousehold } = useSetActiveHousehold({
  mutation: {
    onSuccess: () => {
      refetchHousehold()
      refetchAllHouseholds()
      toast({
        title: 'Husstand oppdatert',
        description: 'Aktiv husstand er endret',
      })
      isChangeHouseholdDialogOpen.value = false
    },
    onError: (error: unknown) => {
      toast({
        title: 'Feil',
        description: (error as Error)?.message || 'Kunne ikke sette aktiv husstand',
        variant: 'destructive',
      })
    },
  },
})

// Pending invites logic
const { data: pendingInvites, refetch: refetchInvites } = useGetPendingInvitesForUser()
const { mutate: acceptInvite } = useAcceptInvite({
  mutation: {
    onSuccess: () => refetchInvites(),
  },
})
const { mutate: declineInvite } = useDeclineInvite({
  mutation: {
    onSuccess: () => refetchInvites(),
  },
})

// Fetch pending invites for this household
const householdId = computed(() => household.value?.id ?? '')
const { data: householdPendingInvites, refetch: refetchHouseholdInvites } =
  useGetPendingInvitesForHousehold(householdId, {
    query: {
      enabled: computed(() => !!householdId.value),
      refetchOnMount: true,
      refetchOnWindowFocus: true,
    },
  })

// Event handlers
function handleHouseholdSubmit(formData: Record<string, unknown>) {
  const householdData: CreateHouseholdRequest = {
    name: formData.name as string,
    address: formData.address as string,
    postalCode: formData.postalCode as string,
    city: formData.city as string,
    latitude: household.value?.latitude ?? 0,
    longitude: household.value?.longitude ?? 0,
  }
  updateActiveHousehold({ data: householdData })
}

function handleMemberSubmit(values: any) {
  if (!household.value?.id) return

  if (values.email) {
    createInvite({
      data: {
        householdId: household.value.id,
        invitedEmail: values.email,
      },
    })
  } else {
    if (!values.name || values.consumptionFactor === undefined || values.consumptionFactor === null) {
      toast({
        title: 'Feil',
        description: 'Navn og forbruksfaktor er påkrevd for å legge til gjest.',
        variant: 'destructive',
      })
      return
    }
    addGuest({
      data: {
        name: values.name,
        icon: 'default_guest_icon.png',
        consumptionMultiplier: values.consumptionFactor,
      },
    })
  }
}

function handleRemoveMember(userId: string) {
  if (!household.value?.id) return
  leaveHousehold({
    data: {
      householdId: household.value.id,
    },
  })
}

function handleRemoveGuest(guestId: string) {
  if (confirm('Er du sikker på at du vil fjerne denne gjesten?')) {
    removeGuest({ guestId })
  }
}

function handleLeaveHousehold() {
  if (!household.value) return
  if (confirm('Er du sikker på at du vil forlate denne husstanden?')) {
    leaveHousehold({
      data: {
        householdId: household.value.id ?? '',
      },
    })
  }
}

function handleDeleteHousehold() {
  if (!household.value) return
  if (
    confirm('Er du sikker på at du vil slette denne husstanden? Dette kan ikke angres.') &&
    household.value.id
  ) {
    deleteHousehold({
      id: household.value.id,
    })
  }
}

function navigateToInventory() {
  router.push('/husstand/beredskapslager')
}

function handleMeetingPlaceSelected(place: any) {
  // Implementation can be added if needed
  console.log('Meeting place selected:', place)
}

function viewMeetingPlace(placeId: string) {
  isMeetingMapDialogOpen.value = true
  // Additional implementation can be added if needed
}

watchEffect(() => {
  if (household.value) {
    console.log(
      '[HouseholdDetailsView] household data changed (from watchEffect):',
      JSON.parse(JSON.stringify(household.value)),
    )
  }
})
</script>

<template>
  <div class="bg-gray-50 min-h-screen">
    <div class="max-w-8xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <!-- Pending Invites Section -->
      <div v-if="pendingInvites && pendingInvites.length" class="mb-6">
        <h2 class="text-xl font-semibold mb-2">Ventende invitasjoner</h2>
        <div
          v-for="invite in pendingInvites"
          :key="invite.id"
          class="bg-yellow-50 border border-yellow-200 rounded-lg p-4 mb-2 flex items-center justify-between"
        >
          <div>
            <div><b>Husstand:</b> {{ invite.household?.name ?? 'Ukjent' }}</div>
            <div>
              <b>Invitert av:</b> {{ invite.createdBy?.firstName }} {{ invite.createdBy?.lastName }}
            </div>
          </div>
          <div class="flex gap-2">
            <button
              class="bg-green-500 text-white px-3 py-1 rounded"
              @click="acceptInvite({ inviteId: invite.id ?? '' })"
            >
              Godta
            </button>
            <button
              class="bg-red-500 text-white px-3 py-1 rounded"
              @click="declineInvite({ inviteId: invite.id ?? '' })"
            >
              Avslå
            </button>
          </div>
        </div>
      </div>

      <div v-if="isLoadingHousehold" class="flex justify-center items-center h-64">
        <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500"></div>
      </div>

      <div v-else-if="household">
        <!-- Household Header -->
        <HouseholdHeader
          :household="household"
          :all-households-count="allHouseholds?.length ?? 0"
          @edit="isEditDialogOpen = true"
          @change-household="isChangeHouseholdDialogOpen = true"
          @show-location="isMeetingMapDialogOpen = true"
        />

        <!-- Main content grid -->
        <div class="grid grid-cols-1 lg:grid-cols-12 gap-6">
          <div class="lg:col-span-8">
            <!-- Members Section -->
            <HouseholdMembers
              :household-id="household.id"
              :members="household.members"
              :guests="household.guests"
              :current-user-id="authStore.currentUser?.id ?? ''"
              @add-member="isAddMemberDialogOpen = true"
              @refresh="refetchHousehold"
            />

            <!-- Emergency Supplies Section -->
            <HouseholdEmergencySuppliesCard
              :household-id="household.id"
              :inventory-items="household.inventoryItems ?? []"
              @show-info="isPreparednessInfoDialogOpen = true"
            />
          </div>

          <!-- Sidebar content -->
          <div class="lg:col-span-4 space-y-6">
            <!-- Meeting Places Section -->
            <HouseholdMeetingPlaces
              :meeting-places="household.meetingPlaces ?? []"
              :household-latitude="household.latitude"
              :household-longitude="household.longitude"
              @meeting-place-selected="handleMeetingPlaceSelected"
            />

            <!-- Household Actions -->
            <HouseholdActions
              @leave="handleLeaveHousehold"
              @delete="handleDeleteHousehold"
            />

            <!-- Invited (pending and declined) section -->
            <InvitedPendingList
              :invites="(householdPendingInvites || [])
                .filter((i) => i.status === 'PENDING')
                .map(invite => ({
                  id: invite.id ?? '',
                  status: 'PENDING',
                  household: invite.household ? {
                    name: invite.household.name ?? ''
                  } : undefined,
                  createdBy: invite.createdBy ? {
                    firstName: invite.createdBy.firstName ?? '',
                    lastName: invite.createdBy.lastName ?? ''
                  } : undefined
                }))"
              :declined-invites="(householdPendingInvites || [])
                .filter((i) => i.status === 'DECLINED')
                .map(invite => ({
                  id: invite.id ?? '',
                  status: 'DECLINED',
                  household: invite.household ? {
                    name: invite.household.name ?? ''
                  } : undefined,
                  createdBy: invite.createdBy ? {
                    firstName: invite.createdBy.firstName ?? '',
                    lastName: invite.createdBy.lastName ?? ''
                  } : undefined
                }))"
            />
          </div>
        </div>

        <!-- All Dialogs -->
        <HouseholdDialogs
          v-model:is-edit-dialog-open="isEditDialogOpen"
          v-model:is-add-member-dialog-open="isAddMemberDialogOpen"
          v-model:is-meeting-map-dialog-open="isMeetingMapDialogOpen"
          v-model:is-change-household-dialog-open="isChangeHouseholdDialogOpen"
          v-model:is-preparedness-info-dialog-open="isPreparednessInfoDialogOpen"
          @household-updated="refetchHousehold"
          @member-added="refetchHousehold"
          @active-household-changed="() => {
            refetchHousehold()
            refetchAllHouseholds()
          }"
          @meeting-place-selected="handleMeetingPlaceSelected"
        />
      </div>

      <div v-else class="text-center py-12">
        <p class="text-gray-500">Kunne ikke laste husstandsdata. Vennligst prøv igjen senere.</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
:deep(.meeting-map-dialog) {
  max-width: 1250px;
  width: 95vw;
}
</style>
