<!-- GamificationSection.vue -->
<script setup lang="ts">
import { ref } from 'vue';
import {
  PlusCircle,
  Edit,
  Trash2
} from 'lucide-vue-next';

// Import shadcn Button component
import { Button } from '@/components/ui/button';

// Mock data for gamification activities
const activities = ref([
  { id: '1', title: 'Nødpakke Quiz', category: 'Forberedelse', points: 100 },
  { id: '2', title: 'Evakueringsrute', category: 'Sikkerhet', points: 150 },
  { id: '3', title: 'Førstehjelpskurs', category: 'Medisinsk', points: 200 }
]);

const deleteItem = (id: string) => {
  console.log(`Deleting activity with ID: ${id}`);
  // Implementation would connect to actual backend
};
</script>

<template>
  <div class="p-6">
    <div class="flex justify-between items-center mb-6">
      <h2 class="text-2xl font-bold text-gray-800">Gamification aktiviteter</h2>
      <Button class="flex items-center bg-blue-600 hover:bg-blue-700 text-white">
        <PlusCircle class="h-4 w-4 mr-1 " />
        Ny aktivitet
      </Button>
    </div>

    <div class="bg-white rounded-lg shadow overflow-hidden">
      <table class="w-full">
        <thead class="bg-gray-50 text-xs text-gray-700 uppercase">
        <tr>
          <th class="px-4 py-3 text-left">Aktivitet</th>
          <th class="px-4 py-3 text-left">Kategori</th>
          <th class="px-4 py-3 text-left">Poeng</th>
          <th class="px-4 py-3 text-center">Handlinger</th>
        </tr>
        </thead>
        <tbody class="divide-y">
        <tr v-for="activity in activities" :key="activity.id" class="hover:bg-gray-50">
          <td class="px-4 py-3 font-medium text-gray-800">{{ activity.title }}</td>
          <td class="px-4 py-3 text-gray-700">{{ activity.category }}</td>
          <td class="px-4 py-3 text-gray-700">{{ activity.points }}</td>
          <td class="px-4 py-3">
            <div class="flex justify-center space-x-2">
              <Button variant="ghost" size="icon" class="text-blue-600 hover:text-blue-800 p-1 h-auto">
                <Edit class="h-4 w-4" />
              </Button>
              <Button
                variant="ghost"
                size="icon"
                class="text-red-600 hover:text-red-800 p-1 h-auto"
                @click="deleteItem(activity.id)"
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
