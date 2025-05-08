<script lang="ts" setup>
import { computed, ref } from 'vue'
import { Button } from '@/components/ui/button'
import { useMe } from '@/api/generated/authentication/authentication'
import { useUpdateUser } from '@/api/generated/user/user'
import type { CreateUser } from '@/api/generated/model'
import { useAuthStore } from '@/stores/auth/useAuthStore.ts'

// Get auth store
const authStore = useAuthStore()

// Get current user data
const { data: currentUser, refetch: refetchUser } = useMe({
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
    id: currentUser.value.id || '',
    firstName: currentUser.value.firstName || '',
    lastName: currentUser.value.lastName || '',
    email: currentUser.value.email || '',
  }
})

const isEditing = ref(false)
const editedUser = ref({ ...user.value })

const toggleEdit = () => {
  isEditing.value = !isEditing.value
  if (isEditing.value) {
    editedUser.value = { ...user.value }
  }
}

const saveProfile = () => {
  if (!currentUser.value) return

  updateUserProfile({
    userId: currentUser.value.id || '',
    data: {
      firstName: editedUser.value.firstName,
      lastName: editedUser.value.lastName,
      email: editedUser.value.email,
    } as CreateUser,
  })

  isEditing.value = false
}
</script>

<template>
  <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
    <div class="flex justify-between items-center mb-6">
      <h2 class="text-xl font-semibold text-gray-800">Personlig informasjon</h2>
      <Button variant="outline" @click="toggleEdit">
        {{ isEditing ? 'Avbryt' : 'Rediger' }}
      </Button>
    </div>

    <form @submit.prevent="saveProfile">
      <div class="space-y-4">
        <!-- First Name field -->
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1" for="firstName"
            >Fornavn</label
          >
          <input
            id="firstName"
            v-model="editedUser.firstName"
            :disabled="!isEditing"
            class="w-full px-4 py-2 border rounded-md focus:ring-blue-500 focus:border-blue-500"
            type="text"
          />
        </div>

        <!-- Last Name field -->
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1" for="lastName"
            >Etternavn</label
          >
          <input
            id="lastName"
            v-model="editedUser.lastName"
            :disabled="!isEditing"
            class="w-full px-4 py-2 border rounded-md focus:ring-blue-500 focus:border-blue-500"
            type="text"
          />
        </div>

        <!-- Email field -->
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1" for="email">E-post</label>
          <input
            id="email"
            v-model="editedUser.email"
            :disabled="!isEditing"
            class="w-full px-4 py-2 border rounded-md focus:ring-blue-500 focus:border-blue-500"
            type="email"
          />
        </div>

        <!-- Save button (only shown when editing) -->
        <div v-if="isEditing" class="mt-6">
          <Button class="w-full" type="submit">Lagre endringer</Button>
        </div>
      </div>
    </form>
  </div>
</template>
