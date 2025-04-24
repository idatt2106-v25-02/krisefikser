<!-- AdminSection.vue -->
<script setup lang="ts">
import { ref } from 'vue';
import {
  Mail,
  Edit,
  Trash2,
  ShieldCheck,
  Key
} from 'lucide-vue-next';

// Import shadcn Button component
import { Button } from '@/components/ui/button';

const props = defineProps({
  isSuperAdmin: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['navigate']);

// Mock data for admins
const admins = ref([
  { id: '1', name: 'Ola Nordmann', email: 'ola@example.no', role: 'Super Admin', lastLogin: '2025-04-23' },
  { id: '2', name: 'Kari Hansen', email: 'kari@example.no', role: 'Admin', lastLogin: '2025-04-22' },
  { id: '3', name: 'Per Jensen', email: 'per@example.no', role: 'Admin', lastLogin: '2025-04-20' }
]);

const deleteItem = (id: string) => {
  console.log(`Deleting admin with ID: ${id}`);
  // Implementation would connect to actual backend
};

const sendPasswordResetLink = () => {
  emit('navigate', '/admin/reset-passord-link');
};
</script>

<template>
  <div class="p-6">
    <div class="flex justify-between items-center mb-6">
      <h2 class="text-2xl font-bold text-gray-800">Admin brukere</h2>

      <!-- Only Super Admin can invite new admins -->
      <div v-if="!isSuperAdmin" class="text-sm text-gray-500 italic flex items-center">
        <ShieldCheck class="h-4 w-4 mr-1 text-gray-400" />
        Kun Super Admin kan administrere brukere
      </div>

      <div v-else class="flex space-x-3">
        <Button variant="default" class="flex items-center bg-blue-600 hover:bg-blue-700 text-white">
          <Mail class="h-4 w-4 mr-1" />
          Inviter ny admin
        </Button>
        <Button
          variant="default"
          class="flex items-center bg-green-600 hover:bg-green-700"
          @click="sendPasswordResetLink"
        >
          <Key class="h-4 w-4 mr-1" />
          Send passord-link
        </Button>
      </div>
    </div>

    <!-- Super Admin privileges notice -->
    <div v-if="isSuperAdmin" class="bg-blue-50 border-l-4 border-blue-500 p-4 mb-6">
      <div class="flex">
        <ShieldCheck class="h-5 w-5 text-blue-500 mr-2" />
        <div>
          <p class="font-medium text-blue-700">Super Admin rettigheter aktivert</p>
          <p class="text-sm text-blue-600">Du har tilgang til å invitere nye admin-brukere og sende ut passord-tilbakestillingslinker.</p>
        </div>
      </div>
    </div>

    <div class="bg-white rounded-lg shadow overflow-hidden">
      <table class="w-full">
        <thead class="bg-gray-50 text-xs text-gray-700 uppercase">
        <tr>
          <th class="px-4 py-3 text-left">Navn</th>
          <th class="px-4 py-3 text-left">E-post</th>
          <th class="px-4 py-3 text-left">Rolle</th>
          <th class="px-4 py-3 text-left">Sist innlogget</th>
          <th class="px-4 py-3 text-center">Handlinger</th>
        </tr>
        </thead>
        <tbody class="divide-y">
        <tr v-for="admin in admins" :key="admin.id" class="hover:bg-gray-50">
          <td class="px-4 py-3 font-medium text-gray-800">{{ admin.name }}</td>
          <td class="px-4 py-3 text-gray-700">{{ admin.email }}</td>
          <td class="px-4 py-3">
              <span :class="`px-2 py-1 rounded-full text-xs font-medium ${admin.role === 'Super Admin' ? 'bg-purple-100 text-purple-800' : 'bg-blue-100 text-blue-800'}`">
                {{ admin.role }}
              </span>
          </td>
          <td class="px-4 py-3 text-gray-700">{{ admin.lastLogin }}</td>
          <td class="px-4 py-3">
            <div class="flex justify-center space-x-2">
              <!-- All admins can see details -->
              <Button variant="ghost" size="icon" class="text-gray-600 hover:text-gray-800 p-1 h-auto">
                <Edit class="h-4 w-4" />
              </Button>

              <!-- Only Super Admin can send reset links and delete users -->
              <Button
                v-if="isSuperAdmin"
                variant="ghost"
                size="icon"
                class="text-blue-600 hover:text-blue-800 p-1 h-auto"
              >
                <Mail class="h-4 w-4" />
              </Button>
              <Button
                v-if="isSuperAdmin && admin.role !== 'Super Admin'"
                variant="ghost"
                size="icon"
                class="text-red-600 hover:text-red-800 p-1 h-auto"
                @click="deleteItem(admin.id)"
              >
                <Trash2 class="h-4 w-4" />
              </Button>
            </div>
          </td>
        </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>
