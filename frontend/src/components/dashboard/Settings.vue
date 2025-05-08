<script lang="ts" setup>

import { toast } from 'vue-sonner'
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
    onSuccess: () => {
      refetchUser()
      toast.success('Innstillinger oppdatert')
    },
    onError: (error) => {
      console.error('Failed to update user settings:', error)
      toast.error('Kunne ikke oppdatere innstillinger')
    },
  },
})

const handleToggle = (
  field: 'notifications' | 'emailUpdates' | 'locationSharing',
  value: boolean,
) => {
  if (!currentUser.value || !currentUser.value.id) {
    toast.error('Brukerdata er ikke tilgjengelig')
    return
  }

  const payload: CreateUser = {
    firstName: currentUser.value.firstName || '',
    lastName: currentUser.value.lastName || '',
    email: currentUser.value.email || '',
    password: '',
    notifications: field === 'notifications' ? value : currentUser.value.notifications ?? false,
    emailUpdates: field === 'emailUpdates' ? value : currentUser.value.emailUpdates ?? false,
    locationSharing: field === 'locationSharing' ? value : currentUser.value.locationSharing ?? false,
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
          :model-value="currentUser.notifications ?? false"
          @update:model-value="(val) => handleToggle('notifications', val)"
        />
      </div>

      <!-- Email updates toggle -->
      <div class="flex items-center justify-between">
        <div>
          <h3 class="text-sm font-medium text-gray-700">E-postoppdateringer</h3>
          <p class="text-sm text-gray-500">Motta ukentlige oppdateringer på e-post</p>
        </div>
        <Switch
          :model-value="currentUser.emailUpdates ?? false"
          @update:model-value="(val) => handleToggle('emailUpdates', val)"
        />
      </div>

      <!-- Location sharing toggle -->
      <div class="flex items-center justify-between">
        <div>
          <h3 class="text-sm font-medium text-gray-700">Del lokasjon</h3>
          <p class="text-sm text-gray-500">Tillat appen å bruke din lokasjon</p>
        </div>
        <Switch
          :model-value="currentUser.locationSharing ?? false"
          @update:model-value="(val) => handleToggle('locationSharing', val)"
        />
      </div>
    </div>
    <div v-else class="text-center text-gray-500 py-4">
      <p>Brukerdata er ikke tilgjengelig for å vise innstillinger.</p>
    </div>
  </div>
</template>
