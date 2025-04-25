<script setup lang="ts">
import { ref } from 'vue';
import { Button } from '@/components/ui/button';

const props = defineProps<{
  user: {
    id: string;
    name: string;
    email: string;
    phone: string;
    address: string;
  }
}>();

const emit = defineEmits(['update:user']);

const isEditing = ref(false);

const toggleEdit = () => {
  isEditing.value = !isEditing.value;
};

const saveProfile = () => {
  // In a real app, you would send the updated user data to your API
  isEditing.value = false;
  emit('update:user', props.user);
  // Show success message or notification
};
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
        <!-- Name field -->
        <div>
          <label for="name" class="block text-sm font-medium text-gray-700 mb-1">Navn</label>
          <input
            type="text"
            id="name"
            v-model="user.name"
            :disabled="!isEditing"
            class="w-full px-4 py-2 border rounded-md focus:ring-blue-500 focus:border-blue-500"
          >
        </div>

        <!-- Email field -->
        <div>
          <label for="email" class="block text-sm font-medium text-gray-700 mb-1">E-post</label>
          <input
            type="email"
            id="email"
            v-model="user.email"
            :disabled="!isEditing"
            class="w-full px-4 py-2 border rounded-md focus:ring-blue-500 focus:border-blue-500"
          >
        </div>

        <!-- Phone field -->
        <div>
          <label for="phone" class="block text-sm font-medium text-gray-700 mb-1">Telefon</label>
          <input
            type="tel"
            id="phone"
            v-model="user.phone"
            :disabled="!isEditing"
            class="w-full px-4 py-2 border rounded-md focus:ring-blue-500 focus:border-blue-500"
          >
        </div>

        <!-- Address field -->
        <div>
          <label for="address" class="block text-sm font-medium text-gray-700 mb-1">Adresse</label>
          <input
            type="text"
            id="address"
            v-model="user.address"
            :disabled="!isEditing"
            class="w-full px-4 py-2 border rounded-md focus:ring-blue-500 focus:border-blue-500"
          >
        </div>

        <!-- Save button (only shown when editing) -->
        <div v-if="isEditing" class="mt-6">
          <Button type="submit" class="w-full">Lagre endringer</Button>
        </div>
      </div>
    </form>
  </div>
</template>
