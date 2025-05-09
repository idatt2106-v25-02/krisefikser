<script lang="ts" setup>
import { ref, watch } from 'vue'
import { toast } from 'vue-sonner'
import { Button } from '@/components/ui/button'
import { useMe } from '@/api/generated/authentication/authentication'
import { useUpdateUser } from '@/api/generated/user/user'
import type { CreateUser } from '@/api/generated/model'
import { useAuthStore } from '@/stores/auth/useAuthStore'

// Get auth store
const authStore = useAuthStore()

// Form state
const isEditing = ref(false)
const formErrors = ref<Record<string, string>>({})
const editedUser = ref({
  firstName: '',
  lastName: '',
  email: '',
})

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

// Watch for changes in currentUser and update form
watch(currentUser, (newUser) => {
  if (newUser) {
    editedUser.value = {
      firstName: newUser.firstName || '',
      lastName: newUser.lastName || '',
      email: newUser.email || '',
    }
  }
}, { immediate: true })

// Update user mutation
const { mutate: updateUserProfile, isPending: isUpdating } = useUpdateUser({
  mutation: {
    onSuccess: async () => {
      await refetchUser()
      isEditing.value = false
      formErrors.value = {}
      toast.success('Profil oppdatert')
    },
    onError: (error) => {
      console.error('Failed to update user:', error)
      formErrors.value = {
        submit: 'Det oppstod en feil under oppdatering av profilen. Vennligst prøv igjen.',
      }
      toast.error('Kunne ikke oppdatere profil')
    },
  },
})

const validateForm = () => {
  const errors: Record<string, string> = {}

  if (!editedUser.value.firstName.trim()) {
    errors.firstName = 'Fornavn er påkrevd'
  }

  if (!editedUser.value.lastName.trim()) {
    errors.lastName = 'Etternavn er påkrevd'
  }

  if (!editedUser.value.email.trim()) {
    errors.email = 'E-post er påkrevd'
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(editedUser.value.email)) {
    errors.email = 'Vennligst skriv inn en gyldig e-postadresse'
  }

  formErrors.value = errors
  return Object.keys(errors).length === 0
}

const toggleEdit = () => {
  isEditing.value = !isEditing.value
  if (!isEditing.value) {
    formErrors.value = {}
    if (currentUser.value) {
      editedUser.value = {
        firstName: currentUser.value.firstName || '',
        lastName: currentUser.value.lastName || '',
        email: currentUser.value.email || '',
      }
    }
  }
}

const saveProfile = async () => {
  if (!currentUser.value?.id || !validateForm()) {
    return
  }

  const payload: CreateUser = {
    firstName: editedUser.value.firstName.trim(),
    lastName: editedUser.value.lastName.trim(),
    email: editedUser.value.email.trim(),
    password: '',
  }

  updateUserProfile({
    userId: currentUser.value.id,
    data: payload,
  })
}
</script>

<template>
  <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
    <div class="flex justify-between items-center mb-6">
      <h2 class="text-xl font-semibold text-gray-800">Personlig informasjon</h2>
      <Button variant="outline" @click="toggleEdit" :disabled="isUpdating">
        {{ isEditing ? 'Avbryt' : 'Rediger' }}
      </Button>
    </div>

    <div v-if="isLoadingUser" class="flex justify-center items-center h-32">
      <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500"></div>
    </div>

    <form v-else-if="currentUser" @submit.prevent="saveProfile" class="space-y-4">
      <!-- First Name field -->
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1" for="firstName">Fornavn</label>
        <input
          id="firstName"
          v-model="editedUser.firstName"
          :disabled="!isEditing || isUpdating"
          class="w-full px-4 py-2 border rounded-md focus:ring-blue-500 focus:border-blue-500"
          :class="{ 'border-red-500': formErrors.firstName }"
          type="text"
        />
        <p v-if="formErrors.firstName" class="mt-1 text-sm text-red-600">
          {{ formErrors.firstName }}
        </p>
      </div>

      <!-- Last Name field -->
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1" for="lastName">Etternavn</label>
        <input
          id="lastName"
          v-model="editedUser.lastName"
          :disabled="!isEditing || isUpdating"
          class="w-full px-4 py-2 border rounded-md focus:ring-blue-500 focus:border-blue-500"
          :class="{ 'border-red-500': formErrors.lastName }"
          type="text"
        />
        <p v-if="formErrors.lastName" class="mt-1 text-sm text-red-600">
          {{ formErrors.lastName }}
        </p>
      </div>

      <!-- Email field -->
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1" for="email">E-post</label>
        <input
          id="email"
          v-model="editedUser.email"
          :disabled="!isEditing || isUpdating"
          class="w-full px-4 py-2 border rounded-md focus:ring-blue-500 focus:border-blue-500"
          :class="{ 'border-red-500': formErrors.email }"
          type="email"
        />
        <p v-if="formErrors.email" class="mt-1 text-sm text-red-600">
          {{ formErrors.email }}
        </p>
      </div>

      <!-- General error message -->
      <p v-if="formErrors.submit" class="mt-2 text-sm text-red-600">
        {{ formErrors.submit }}
      </p>

      <!-- Save button (only shown when editing) -->
      <div v-if="isEditing" class="mt-6">
        <Button
          class="w-full"
          type="submit"
          :disabled="isUpdating"
        >
          {{ isUpdating ? 'Lagrer...' : 'Lagre endringer' }}
        </Button>
      </div>
    </form>

    <div v-else class="text-center text-gray-500 py-4">
      <p>Brukerdata er ikke tilgjengelig for å vise profil.</p>
    </div>
  </div>
</template>
