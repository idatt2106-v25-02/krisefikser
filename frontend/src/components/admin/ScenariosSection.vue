<!-- ScenariosSection.vue -->
<script setup lang="ts">
import { ref } from 'vue';
import {
  PlusCircle,
  Edit,
  Trash2
} from 'lucide-vue-next';

// Mock data for scenarios
const scenarios = ref([
  { id: '1', title: 'Strømbrudd', description: 'Forberedelser ved langvarig strømbrudd' },
  { id: '2', title: 'Flom', description: 'Håndtering av flom og oversvømmelse' },
  { id: '3', title: 'Pandemi', description: 'Forebyggende tiltak ved pandemi' }
]);

const deleteItem = (id) => {
  console.log(`Deleting scenario with ID: ${id}`);
  // Implementation would connect to actual backend
};
</script>

<template>
  <div class="p-6">
    <div class="flex justify-between items-center mb-6">
      <h2 class="text-2xl font-bold text-gray-800">Krisescenarioer</h2>
      <button class="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg flex items-center">
        <PlusCircle class="h-4 w-4 mr-1" />
        Nytt scenario
      </button>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <div v-for="scenario in scenarios" :key="scenario.id" class="bg-white rounded-lg shadow overflow-hidden">
        <div class="p-4">
          <h3 class="text-lg font-medium text-gray-800 mb-2">{{ scenario.title }}</h3>
          <p class="text-gray-600 mb-4">{{ scenario.description }}</p>
          <div class="flex justify-end space-x-2">
            <button class="text-blue-600 hover:text-blue-800 p-2">
              <Edit class="h-5 w-5" />
            </button>
            <button class="text-red-600 hover:text-red-800 p-2" @click="deleteItem(scenario.id)">
              <Trash2 class="h-5 w-5" />
            </button>
          </div>
        </div>
      </div>

      <div class="bg-gray-50 rounded-lg border-2 border-dashed border-gray-300 flex items-center justify-center p-8">
        <button class="text-blue-600 hover:text-blue-800 flex flex-col items-center">
          <PlusCircle class="h-10 w-10" />
          <span class="mt-2 font-medium">Legg til nytt scenario</span>
        </button>
      </div>
    </div>
  </div>
</template>
