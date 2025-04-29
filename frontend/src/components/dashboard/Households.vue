<script setup lang="ts">
import { useLeaveHousehold, useSetActiveHousehold } from '@/api/generated/household/household'
import { useAuthStore } from '@/stores/useAuthStore'

const props = defineProps<{
  households: Array<{
    id: string
    name: string
  }>
  activeHouseholdId?: string
}>()

const emit = defineEmits<{
  (e: 'refresh'): void
}>()

const authStore = useAuthStore()

// Leave household mutation
const { mutate: leaveHousehold } = useLeaveHousehold({
  mutation: {
    onSuccess: () => {
      emit('refresh')
    }
  }
})

// Set active household mutation
const { mutate: setActiveHousehold } = useSetActiveHousehold({
  mutation: {
    onSuccess: () => {
      emit('refresh')
    }
  }
})

const handleLeaveHousehold = (householdId: string) => {
  if (confirm('Er du sikker på at du vil forlate denne husstanden?')) {
    leaveHousehold({
      data: {
        householdId
      }
    })
  }
}

const handleSetActiveHousehold = (householdId: string) => {
  setActiveHousehold({
    data: {
      householdId
    }
  })
}
</script>

<template>
  <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mb-6">
    <router-link to="/husstand" class="inline-block mb-4">
      <h2 class="text-lg font-semibold text-gray-800">Mine husstander</h2>
    </router-link>
    <ul class="space-y-3">
      <li v-for="household in households" :key="household.id" class="flex items-center">
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
              v-if="household.id === activeHouseholdId"
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
    <div class="mt-4">
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
    </div>
  </div>
</template>
