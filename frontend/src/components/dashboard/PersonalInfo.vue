<script setup lang="ts">
import { ref } from 'vue'
import { Button } from '@/components/ui/button'

const props = defineProps<{
  user: {
    id: string
    firstName: string
    lastName: string
    email: string
  }
}>()

const emit = defineEmits(['update:user'])

const isEditing = ref(false)
const editedUser = ref({ ...props.user })

const toggleEdit = () => {
  isEditing.value = !isEditing.value
  if (isEditing.value) {
    editedUser.value = { ...props.user }
  }
}

const saveProfile = () => {
  emit('update:user', editedUser.value)
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
          <label for="firstName" class="block text-sm font-medium text-gray-700 mb-1">Fornavn</label>
          <input
            type="text"
            id="firstName"
            v-model="editedUser.firstName"
            :disabled="!isEditing"
            class="w-full px-4 py-2 border rounded-md focus:ring-blue-500 focus:border-blue-500"
          />
        </div>

        <!-- Last Name field -->
        <div>
          <label for="lastName" class="block text-sm font-medium text-gray-700 mb-1">Etternavn</label>
          <input
            type="text"
            id="lastName"
            v-model="editedUser.lastName"
            :disabled="!isEditing"
            class="w-full px-4 py-2 border rounded-md focus:ring-blue-500 focus:border-blue-500"
          />
        </div>

        <!-- Email field -->
        <div>
          <label for="email" class="block text-sm font-medium text-gray-700 mb-1">E-post</label>
          <input
            type="email"
            id="email"
            v-model="editedUser.email"
            :disabled="!isEditing"
            class="w-full px-4 py-2 border rounded-md focus:ring-blue-500 focus:border-blue-500"
          />
        </div>

        <!-- Save button (only shown when editing) -->
        <div v-if="isEditing" class="mt-6">
          <Button type="submit" class="w-full">Lagre endringer</Button>
        </div>
      </div>
    </form>
  </div>
</template>
