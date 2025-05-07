<script setup lang="ts">
import { computed } from 'vue'
import Switch from '@/components/ui/switch/Switch.vue'
import { useMe } from '@/api/generated/authentication/authentication'
import { useUpdateUser } from '@/api/generated/user/user'
import type { CreateUser } from '@/api/generated/model'
import { useAuthStore } from '@/stores/auth/useAuthStore.ts'

// Get auth store
const authStore = useAuthStore()

// Get current user data
const {
  data: currentUser,
  isLoading: isLoadingUser,
  refetch: refetchUser,
} = useMe({
  query: {
    enabled: authStore.isAuthenticated,
    refetchOnMount: true,
    refetchOnWindowFocus: true,
  },
})

// Update user mutation
const { mutate: updateUserProfile } = useUpdateUser({
  mutation: {
    onSuccess: (data) => {
      console.log('User settings updated successfully:', data)
      refetchUser()
    },
    onError: (error) => {
      console.error('Failed to update user settings:', error)
    },
  },
})

// Computed properties for switch states with getter and setter
const notificationsSwitch = computed({
  get: () => currentUser.value?.notifications ?? false,
  set: (value: boolean) => handleToggle('notifications', value),
})

const emailUpdatesSwitch = computed({
  get: () => currentUser.value?.emailUpdates ?? false,
  set: (value: boolean) => handleToggle('emailUpdates', value),
})

const locationSharingSwitch = computed({
  get: () => currentUser.value?.locationSharing ?? false,
  set: (value: boolean) => handleToggle('locationSharing', value),
})

const handleToggle = (
  field: 'notifications' | 'emailUpdates' | 'locationSharing',
  value: boolean,
) => {
  if (!currentUser.value || !currentUser.value.id) {
    console.error('Current user or user ID is not available. Cannot update settings.')
    return
  }

  console.log(`Attempting to toggle ${field} to ${value} via API for user ${currentUser.value.id}`)

  // Prepare the data payload for the mutation
  // It's crucial that this matches the backend's expected CreateUser DTO structure,
  // especially regarding optional/required fields and password handling.
  const payload: CreateUser = {
    firstName: currentUser.value.firstName || '',
    lastName: currentUser.value.lastName || '',
    email: currentUser.value.email || '',
    password: '', // Add empty string for password to satisfy DTO, backend will ignore if empty

    notifications: field === 'notifications' ? value : currentUser.value.notifications,
    emailUpdates: field === 'emailUpdates' ? value : currentUser.value.emailUpdates,
    locationSharing: field === 'locationSharing' ? value : currentUser.value.locationSharing,
  }

  updateUserProfile({
    userId: currentUser.value.id,
    data: payload,
  })
}
</script>

<template>
  <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
    <h2 class="text-xl font-semibold text-gray-800 mb-6">Innstillinger</h2>

    <div v-if="isLoadingUser" class="flex justify-center items-center h-32">
      <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500"></div>
    </div>

    <div v-else-if="currentUser" class="space-y-4">
      <!-- Notifications toggle -->
      <div class="flex items-center justify-between">
        <div>
          <h3 class="text-sm font-medium text-gray-700">Varslinger</h3>
          <p class="text-sm text-gray-500">Motta varslinger om viktige hendelser</p>
        </div>
        <Switch
          :model-value="notificationsSwitch"
          @update:model-value="notificationsSwitch = $event"
        />
      </div>

      <!-- Email updates toggle -->
      <div class="flex items-center justify-between">
        <div>
          <h3 class="text-sm font-medium text-gray-700">E-postoppdateringer</h3>
          <p class="text-sm text-gray-500">Motta ukentlige oppdateringer på e-post</p>
        </div>
        <Switch
          :model-value="emailUpdatesSwitch"
          @update:model-value="emailUpdatesSwitch = $event"
        />
      </div>

      <!-- Location sharing toggle -->
      <div class="flex items-center justify-between">
        <div>
          <h3 class="text-sm font-medium text-gray-700">Del lokasjon</h3>
          <p class="text-sm text-gray-500">Tillat appen å bruke din lokasjon</p>
        </div>
        <Switch
          :model-value="locationSharingSwitch"
          @update:model-value="locationSharingSwitch = $event"
        />
      </div>
    </div>
    <div v-else class="text-center text-gray-500 py-4">
      <p>Brukerdata er ikke tilgjengelig for å vise innstillinger.</p>
    </div>
  </div>
</template>
