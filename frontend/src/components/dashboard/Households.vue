<script lang="ts" setup>
import {
  useGetActiveHousehold,
  useGetAllUserHouseholds,
  useJoinHousehold,
  useLeaveHousehold,
  useSetActiveHousehold,
} from '@/api/generated/household/household'
import { ref } from 'vue'
import type { HouseholdResponse } from '@/api/generated/model'
import {
  useAcceptInvite,
  useDeclineInvite,
  useGetPendingInvitesForUser,
} from '@/api/generated/household-invite-controller/household-invite-controller'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth/useAuthStore'
import { Button } from '@/components/ui/button'

const emit = defineEmits<{
  (e: 'refresh'): void
}>()

const { mutate: leaveHousehold } = useLeaveHousehold({
  mutation: {
    onSuccess: () => {
      emit('refresh')
      refetchHouseholds()
      refetchActiveHousehold()
    },
  },
})

const { mutate: setActiveHousehold } = useSetActiveHousehold({
  mutation: {
    onSuccess: () => {
      emit('refresh')
      refetchHouseholds()
      refetchActiveHousehold()
    },
  },
})

const authStore = useAuthStore()

const handleLeaveHousehold = (household: HouseholdResponse) => {
  // Check if the user is the owner
  if (household.owner.id === authStore.currentUser?.id) {
    alert(
      'Du kan ikke forlate husstanden fordi du er eier. Du må enten overføre eierskapet til et annet medlem eller slette husstanden.',
    )
    return
  }

  if (confirm('Er du sikker på at du vil forlate denne husstanden?')) {
    leaveHousehold({
      data: {
        householdId: household.id,
      },
    })
  }
}

const handleSetActiveHousehold = (householdId: string) => {
  setActiveHousehold({
    data: {
      householdId,
    },
  })
}

const { mutate: JoinHousehold, isPending: isJoinHouseholdPending } = useJoinHousehold({
  mutation: {
    onSuccess: () => {
      refetchHouseholds()
      householdCode.value = ''
      errorMessage.value = ''
      showJoinModal.value = false
    },
    onError: (error) => {
      console.error('Failed to join household:', error)
      errorMessage.value = 'Kunne ikke bli med i husstanden'
    },
  },
})

const {
  data: householdsData,
  isLoading: isLoadingHouseholds,
  refetch: refetchHouseholds,
} = useGetAllUserHouseholds()

const { data: activeHouseholdData, refetch: refetchActiveHousehold } = useGetActiveHousehold()

const showJoinModal = ref(false)
const householdCode = ref('')
const errorMessage = ref('')

const joinHousehold = () => {
  JoinHousehold({
    data: {
      householdId: householdCode.value,
    },
  })
}

const closeModal = () => {
  householdCode.value = ''
  errorMessage.value = ''
  showJoinModal.value = false
}

// Pending invites logic
const { data: pendingInvites, refetch: refetchInvites } = useGetPendingInvitesForUser()
const { mutate: acceptInvite } = useAcceptInvite({
  mutation: {
    onSuccess: () => {
      refetchInvites()
      refetchHouseholds()
      refetchActiveHousehold()
    },
  },
})
const { mutate: declineInvite } = useDeclineInvite({
  mutation: {
    onSuccess: () => {
      refetchInvites()
    },
  },
})

const router = useRouter()
</script>

<template>
  <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mb-6">
    <router-link class="inline-block mb-4" to="/husstand">
      <h2 class="text-lg font-semibold text-gray-800">Mine husstander</h2>
    </router-link>
    <!-- Pending Invites Section -->
    <div v-if="pendingInvites && pendingInvites.length" class="mb-6">
      <h2 class="text-lg font-semibold mb-2">Ventende invitasjoner</h2>
      <div
        v-for="invite in pendingInvites"
        :key="invite.id"
        class="bg-white border border-gray-200 rounded-lg shadow-sm p-4 mb-2 flex flex-col gap-2"
      >
        <div class="mb-2">
          <span class="font-semibold">Husstand:</span> {{ invite.household?.name ?? 'Ukjent'
          }}<br />
          <span class="font-semibold">Invitert av:</span> {{ invite.createdBy?.firstName }}
          {{ invite.createdBy?.lastName }}
        </div>
        <div class="flex gap-2">
          <button
            class="bg-green-500 hover:bg-green-600 text-white px-4 py-1 rounded transition"
            @click="acceptInvite({ inviteId: invite.id ?? '' })"
          >
            Godta
          </button>
          <button
            class="bg-red-500 hover:bg-red-600 text-white px-4 py-1 rounded transition"
            @click="declineInvite({ inviteId: invite.id ?? '' })"
          >
            Avslå
          </button>
        </div>
      </div>
    </div>
    <!-- Display loading state -->
    <div v-if="isLoadingHouseholds" class="p-4 text-center text-gray-500">Laster husstander...</div>

    <ul v-else class="space-y-3">
      <li v-for="household in householdsData" :key="household.id" class="flex items-center">
        <div class="bg-blue-100 p-2 rounded-full mr-3">
          <svg
            class="h-5 w-5 text-blue-600"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
            xmlns="http://www.w3.org/2000/svg"
          >
            <path
              d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
            />
          </svg>
        </div>
        <div class="flex flex-1 items-center justify-between">
          <div
            :class="[
              'font-medium',
              activeHouseholdData?.id === household.id
                ? 'text-blue-600 hover:text-blue-800 cursor-pointer'
                : 'text-gray-800 cursor-default',
            ]"
            @click="
              activeHouseholdData?.id === household.id ? router.push({ name: 'household' }) : null
            "
          >
            {{ household.name }}
          </div>
          <div class="flex items-center space-x-2">
            <span
              v-if="activeHouseholdData?.id === household.id"
              class="inline-flex items-center rounded-full bg-green-100 px-2.5 py-0.5 text-xs font-medium text-green-800"
            >
              Aktiv
            </span>
            <button
              v-else
              class="text-blue-600 hover:text-blue-800 text-sm"
              @click="handleSetActiveHousehold(household.id)"
            >
              Gjør aktiv
            </button>
            <button
              v-if="household.owner.id !== authStore.currentUser?.id"
              class="text-red-600 hover:text-red-800 text-sm"
              @click="handleLeaveHousehold(household)"
            >
              Forlat
            </button>
            <span v-else class="text-xs text-gray-500"> (Eier) </span>
          </div>
        </div>
      </li>
    </ul>
    <div class="mt-4 flex items-center space-x-4">
      <router-link
        class="inline-flex items-center text-sm text-blue-600 hover:text-blue-800"
        to="/husstand/opprett"
      >
        <svg
          class="h-4 w-4 mr-1"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
          xmlns="http://www.w3.org/2000/svg"
        >
          <path
            d="M12 4v16m8-8H4"
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
          />
        </svg>
        Opprett ny husstand
      </router-link>
    </div>
  </div>

  <!-- Join Household Dialog -->
  <Dialog :open="showJoinModal" @update:open="(val: any) => !val && closeModal()">
    <DialogContent class="sm:max-w-md">
      <DialogHeader>
        <DialogTitle>Bli med i husstand</DialogTitle>
        <DialogDescription>
          Skriv inn husholdningskoden for å bli med i en eksisterende husstand.
        </DialogDescription>
      </DialogHeader>

      <div class="mt-4">
        <label class="block text-sm font-medium text-gray-700 mb-1">Husholdningskode</label>
        <div class="relative">
          <svg
            class="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-500 h-4 w-4"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
            xmlns="http://www.w3.org/2000/svg"
          >
            <path
              d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
            />
          </svg>
          <input
            v-model="householdCode"
            class="w-full px-3 py-2 pl-10 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            placeholder="f.eks. 0e0beb57-..."
            type="text"
          />
        </div>
        <p v-if="errorMessage" class="mt-1 text-sm text-red-500">{{ errorMessage }}</p>
      </div>

      <DialogFooter class="flex gap-2 mt-4">
        <Button class="flex-1" variant="outline" @click="closeModal"> Avbryt</Button>
        <Button
          :disabled="isJoinHouseholdPending"
          class="flex-1"
          variant="default"
          @click="joinHousehold"
        >
          {{ isJoinHouseholdPending ? 'Blir med...' : 'Bli med' }}
        </Button>
      </DialogFooter>
    </DialogContent>
  </Dialog>
</template>
