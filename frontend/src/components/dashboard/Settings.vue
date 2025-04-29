<script setup lang="ts">
import { ref, computed } from 'vue'
import { Switch } from '@/components/ui/switch'
import { useMe } from '@/api/generated/authentication/authentication'
import { useUpdateUser } from '@/api/generated/user/user'
import type { CreateUserDto } from '@/api/generated/model'
import { useAuthStore } from '@/stores/useAuthStore'

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
      console.log('User updated successfully:', data)
      refetchUser()
    },
    onError: (error) => {
      console.error('Failed to update user:', error)
    },
  },
})

// Transform API user data to match our component interface
const user = computed(() => {
  if (!currentUser.value) return null

  return {
    notifications: currentUser.value.notifications || false,
    emailUpdates: currentUser.value.emailUpdates || false,
    locationSharing: currentUser.value.locationSharing || false,
  }
})

const notificationsRef = ref(user.value?.notifications ?? false)
const emailUpdatesRef = ref(user.value?.emailUpdates ?? false)
const locationSharingRef = ref(user.value?.locationSharing ?? false)

const handleToggle = (field: 'notifications' | 'emailUpdates' | 'locationSharing', value: boolean) => {
  if (!currentUser.value) return

  console.log(`Toggling ${field} to ${value}`)

  // Update local state
  if (field === 'notifications') {
    notificationsRef.value = value
  } else if (field === 'emailUpdates') {
    emailUpdatesRef.value = value
  } else if (field === 'locationSharing') {
    locationSharingRef.value = value
  }

  // Call the mutation
  updateUserProfile({
    userId: currentUser.value.id || '',
    data: {
      firstName: currentUser.value.firstName,
      lastName: currentUser.value.lastName,
      email: currentUser.value.email,
      notifications: field === 'notifications' ? value : currentUser.value.notifications,
      emailUpdates: field === 'emailUpdates' ? value : currentUser.value.emailUpdates,
      locationSharing: field === 'locationSharing' ? value : currentUser.value.locationSharing,
    } as CreateUserDto,
  })
}
</script>

<template>
  <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
    <h2 class="text-xl font-semibold text-gray-800 mb-6">Innstillinger</h2>

    <div v-if="isLoadingUser" class="flex justify-center items-center h-32">
      <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500"></div>
    </div>

    <div v-else class="space-y-4">
      <!-- Notifications toggle -->
      <div class="flex items-center justify-between">
        <div>
          <h3 class="text-sm font-medium text-gray-700">Varslinger</h3>
          <p class="text-sm text-gray-500">Motta varslinger om viktige hendelser</p>
        </div>
        <Switch
          :model-value="notificationsRef"
          @update:model-value="(value: boolean) => handleToggle('notifications', value)"
        />
      </div>

      <!-- Email updates toggle -->
      <div class="flex items-center justify-between">
        <div>
          <h3 class="text-sm font-medium text-gray-700">E-postoppdateringer</h3>
          <p class="text-sm text-gray-500">Motta ukentlige oppdateringer på e-post</p>
        </div>
        <Switch
          :model-value="emailUpdatesRef"
          @update:model-value="(value: boolean) => handleToggle('emailUpdates', value)"
        />
      </div>

      <!-- Location sharing toggle -->
      <div class="flex items-center justify-between">
        <div>
          <h3 class="text-sm font-medium text-gray-700">Del lokasjon</h3>
          <p class="text-sm text-gray-500">Tillat appen å bruke din lokasjon</p>
        </div>
        <Switch
          :model-value="locationSharingRef"
          @update:model-value="(value: boolean) => handleToggle('locationSharing', value)"
        />
      </div>
    </div>
  </div>
</template>
