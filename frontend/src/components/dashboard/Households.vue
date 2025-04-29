<script setup lang="ts">
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

// Import Button component
import { Button } from '@/components/ui/button'

defineProps<{
  households: Array<{
    id: string
    name: string
  }>
}>()

const showJoinModal = ref(false)
const householdCode = ref('')
const errorMessage = ref('')

const joinHousehold = () => {
  // TODO: Implement joining household with the code
  console.log('Joining household with code:', householdCode.value)
  // Reset form and close modal after successful join
  householdCode.value = ''
  errorMessage.value = ''
  showJoinModal.value = false
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
    <ul class="space-y-3">
      <li v-for="(household, index) in households" :key="household.id" class="flex items-center">
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
          <span
            v-if="index === 0"
            class="inline-flex items-center rounded-full bg-green-100 px-2.5 py-0.5 text-xs font-medium text-green-800"
          >
            Aktiv
          </span>
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
  <Dialog :open="showJoinModal" @update:open="(val) => !val && closeModal()">
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
        <Button variant="default" @click="joinHousehold" class="flex-1"> Bli med </Button>
      </DialogFooter>
    </DialogContent>
  </Dialog>
</template>
