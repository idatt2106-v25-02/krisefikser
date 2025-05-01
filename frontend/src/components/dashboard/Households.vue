<script setup lang="ts">
import { useLeaveHousehold, useSetActiveHousehold } from '@/api/generated/household/household'

const emit = defineEmits<{
  (e: 'refresh'): void
}>()

// Leave household mutation
const { mutate: leaveHousehold } = useLeaveHousehold({
  mutation: {
    onSuccess: () => {
      emit('refresh')
    },
  },
})

// Set active household mutation
const { mutate: setActiveHousehold } = useSetActiveHousehold({
  mutation: {
    onSuccess: () => {
      emit('refresh')
      refetchHouseholds()
      refetchActiveHousehold()
    },
  },
})

const handleLeaveHousehold = (householdId: string) => {
  if (confirm('Er du sikker på at du vil forlate denne husstanden?')) {
    leaveHousehold({
      data: {
        householdId,
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
import { ref } from 'vue'

// Import Dialog components
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'

import { useJoinHousehold } from '@/api/generated/household/household'
import { useGetAllHouseholds } from '@/api/generated/household/household'
import { useGetActiveHousehold } from '@/api/generated/household/household'

const { mutate: JoinHousehold, isPending: isJoinHouseholdPending } = useJoinHousehold({
  mutation: {
    onSuccess: () => {
      console.log('Successfully joined household')
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
} = useGetAllHouseholds()

const { data: activeHouseholdData, refetch: refetchActiveHousehold } = useGetActiveHousehold()

// Import Button component
import { Button } from '@/components/ui/button'

const showJoinModal = ref(false)
const householdCode = ref('')
const errorMessage = ref('')

const joinHousehold = () => {
  console.log('Joining household with code:', householdCode.value)

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
</script>

<template>
  <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mb-6">
    <router-link to="/husstand" class="inline-block mb-4">
      <h2 class="text-lg font-semibold text-gray-800">Mine husstander</h2>
    </router-link>
    <!-- Display loading state -->
    <div v-if="isLoadingHouseholds" class="p-4 text-center text-gray-500">Laster husstander...</div>

    <ul v-else class="space-y-3">
      <li v-for="household in householdsData" :key="household.id" class="flex items-center">
        <div class="bg-blue-100 p-2 rounded-full mr-3">
          <svg
            xmlns="http://www.w3.org/2000/svg"
            class="h-5 w-5 text-blue-600"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"
            />
          </svg>
        </div>
        <div class="flex flex-1 items-center justify-between">
          <router-link
            :to="`/husstand/${household.id}`"
            class="text-gray-800 hover:text-blue-600 font-medium"
          >
            {{ household.name }}
          </router-link>
          <div class="flex items-center space-x-2">
            <span
              v-if="activeHouseholdData?.id === household.id"
              class="inline-flex items-center rounded-full bg-green-100 px-2.5 py-0.5 text-xs font-medium text-green-800"
            >
              Aktiv
            </span>
            <button
              v-else
              @click="handleSetActiveHousehold(household.id)"
              class="text-blue-600 hover:text-blue-800 text-sm"
            >
              Gjør aktiv
            </button>
            <button
              @click="handleLeaveHousehold(household.id)"
              class="text-red-600 hover:text-red-800 text-sm"
            >
              Forlat
            </button>
          </div>
        </div>
      </li>
    </ul>
    <div class="mt-4 flex items-center space-x-4">
      <router-link
        to="/husstand/opprett"
        class="inline-flex items-center text-sm text-blue-600 hover:text-blue-800"
      >
        <svg
          xmlns="http://www.w3.org/2000/svg"
          class="h-4 w-4 mr-1"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
        >
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M12 4v16m8-8H4"
          />
        </svg>
        Opprett ny husstand
      </router-link>

      <button
        @click="showJoinModal = true"
        class="inline-flex items-center text-sm text-blue-600 hover:text-blue-800"
      >
        <svg
          xmlns="http://www.w3.org/2000/svg"
          class="h-4 w-4 mr-1"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
        >
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z"
          />
        </svg>
        Bli med i husstand
      </button>
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
            xmlns="http://www.w3.org/2000/svg"
            class="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-500 h-4 w-4"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"
            />
          </svg>
          <input
            v-model="householdCode"
            type="text"
            placeholder="f.eks. 0e0beb57-..."
            class="w-full px-3 py-2 pl-10 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>
        <p v-if="errorMessage" class="mt-1 text-sm text-red-500">{{ errorMessage }}</p>
      </div>

      <DialogFooter class="flex gap-2 mt-4">
        <Button variant="outline" @click="closeModal" class="flex-1"> Avbryt </Button>
        <Button
          variant="default"
          @click="joinHousehold"
          class="flex-1"
          :disabled="isJoinHouseholdPending"
        >
          {{ isJoinHouseholdPending ? 'Blir med...' : 'Bli med' }}
        </Button>
      </DialogFooter>
    </DialogContent>
  </Dialog>
</template>
