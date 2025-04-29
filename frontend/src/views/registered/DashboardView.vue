<script setup lang="ts">
import { ref, computed } from 'vue'
import PageLayout from '@/components/layout/PageLayout.vue'
import { User } from 'lucide-vue-next'
import { useAuthStore } from '@/stores/useAuthStore'
import { useMe } from '@/api/generated/authentication/authentication'
import { useUpdateUser } from '@/api/generated/user/user'
import type { CreateUserDto } from '@/api/generated/model'

// Import new components
import PersonalInfo from '@/components/dashboard/PersonalInfo.vue'
import Settings from '@/components/dashboard/Settings.vue'
import Households from '@/components/dashboard/Households.vue'
import Security from '@/components/dashboard/Security.vue'

// Define user interface
interface Household {
  id: string;
  name: string;
}

interface UserData {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  households: Household[];
  notifications: boolean;
  emailUpdates: boolean;
  locationSharing: boolean;
}

// Get auth store
const authStore = useAuthStore()

// Get current user data
const { data: currentUser, isLoading: isLoadingUser, refetch: refetchUser } = useMe({
  query: {
    enabled: authStore.isAuthenticated,
    refetchOnMount: true,
    refetchOnWindowFocus: true
  }
})

// Update user mutation
const { mutate: updateUserProfile } = useUpdateUser({
  mutation: {
    onSuccess: (data) => {
      console.log('User updated successfully:', data)
    },
    onError: (error) => {
      console.error('Failed to update user:', error)
    }
  }
})

// Transform API user data to match our component interface
const user = computed(() => {
  if (!currentUser.value) return null

  return {
    id: currentUser.value.id || '',
    firstName: currentUser.value.firstName || '',
    lastName: currentUser.value.lastName || '',
    email: currentUser.value.email || '',
    households: [],
    notifications: currentUser.value.notifications || false,
    emailUpdates: currentUser.value.emailUpdates || false,
    locationSharing: currentUser.value.locationSharing || false,
  }
})

const updateUserInfo = (updatedUser: Partial<UserData>, p0: unknown): void => {
  if (!user.value || !currentUser.value) return

  console.log('Updating user info:', updatedUser) // Debug log

  // Optimistically update local state
  if (user.value) {
    if (updatedUser.notifications !== undefined) user.value.notifications = updatedUser.notifications
    if (updatedUser.emailUpdates !== undefined) user.value.emailUpdates = updatedUser.emailUpdates
    if (updatedUser.locationSharing !== undefined) user.value.locationSharing = updatedUser.locationSharing
  }

  // Call the mutation
  updateUserProfile({
    userId: currentUser.value.id || '',
    data: {
      firstName: currentUser.value.firstName,
      lastName: currentUser.value.lastName,
      email: currentUser.value.email,
      notifications: updatedUser.notifications ?? currentUser.value.notifications,
      emailUpdates: updatedUser.emailUpdates ?? currentUser.value.emailUpdates,
      locationSharing: updatedUser.locationSharing ?? currentUser.value.locationSharing,
    } as CreateUserDto
  })
}

// Add individual update functions for each setting
const updateNotificationSetting = (value: boolean) => {
  console.log('Received notification update:', value) // Debug log
  updateUserInfo({ notifications: value }, { onSuccess: () => refetchUser() })
}

const updateEmailSetting = (value: boolean) => {
  console.log('Received email update:', value) // Debug log
  updateUserInfo({ emailUpdates: value }, { onSuccess: () => refetchUser() })
}

const updateLocationSetting = (value: boolean) => {
  console.log('Received location update:', value) // Debug log
  updateUserInfo({ locationSharing: value }, { onSuccess: () => refetchUser() })
}
</script>

<template>
  <PageLayout pageTitle="Min profil" sectionName="Bruker" :iconComponent="User" iconBgColor="blue">
    <div v-if="isLoadingUser" class="flex justify-center items-center h-64">
      <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500"></div>
    </div>

    <div v-else-if="user" class="grid grid-cols-1 md:grid-cols-3 gap-8">
      <!-- Main profile information -->
      <div class="md:col-span-2">
        <PersonalInfo :user="user" @update:user="updateUserInfo" />

        <!-- User preferences section -->
        <div class="mt-6">
          <Settings
            :notifications="user.notifications"
            :emailUpdates="user.emailUpdates"
            :locationSharing="user.locationSharing"
            @update:notifications="updateNotificationSetting"
            @update:emailUpdates="updateEmailSetting"
            @update:locationSharing="updateLocationSetting"
          />
        </div>
      </div>

      <!-- Sidebar information -->
      <div class="md:col-span-1">
        <!-- User households -->
        <Households :households="user.households" />

        <!-- Password change card -->
        <Security />
      </div>
    </div>

    <div v-else class="text-center py-12">
      <p class="text-gray-500">Kunne ikke laste brukerdata. Vennligst prøv igjen senere.</p>
    </div>
  </PageLayout>
</template>
