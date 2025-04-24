<!-- EventsSection.vue -->
<script setup lang="ts">
import { ref } from 'vue';
import {
  PlusCircle,
  Search,
  Edit,
  Trash2
} from 'lucide-vue-next';

// Mock data for events
const events = ref([
  {
    id: '1',
    title: 'Flom i Trondheim',
    description: 'Økt vannstand i Nidelva',
    level: 'rød',
    location: 'Trondheim sentrum',
    timestamp: '2025-04-22T10:30:00',
    active: true
  },
  {
    id: '2',
    title: 'Strømbrudd',
    description: 'Midlertidig strømbrudd grunnet vedlikehold',
    level: 'gul',
    location: 'Oslo vest',
    timestamp: '2025-04-23T08:00:00',
    active: true
  },
  {
    id: '3',
    title: 'Snøskred varsel',
    description: 'Fare for snøskred i fjellområdet',
    level: 'gul',
    location: 'Jotunheimen',
    timestamp: '2025-04-20T14:45:00',
    active: false
  }
]);

const deleteItem = (id) => {
  console.log(`Deleting event with ID: ${id}`);
  // Implementation would connect to actual backend
};

const getLevelClass = (level) => {
  switch(level.toLowerCase()) {
    case 'rød': return 'bg-red-100 text-red-800 border-red-200';
    case 'gul': return 'bg-yellow-100 text-yellow-800 border-yellow-200';
    case 'grønn': return 'bg-green-100 text-green-800 border-green-200';
    default: return 'bg-gray-100 text-gray-800 border-gray-200';
  }
};
</script>

<template>
  <div class="p-6">
    <div class="flex justify-between items-center mb-6">
      <h2 class="text-2xl font-bold text-gray-800">Hendelser</h2>
      <button class="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg flex items-center">
        <PlusCircle class="h-4 w-4 mr-1" />
        Ny hendelse
      </button>
    </div>

    <div class="bg-white rounded-lg shadow overflow-hidden">
      <div class="p-4 flex justify-between border-b">
        <div class="flex items-center rounded-lg bg-gray-100 px-3 py-2 w-64">
          <Search class="h-4 w-4 text-gray-500" />
          <input type="text" placeholder="Søk hendelser..." class="bg-transparent border-0 outline-none ml-2 text-gray-700 w-full" />
        </div>
        <div class="flex space-x-2">
          <select class="border rounded-lg px-3 py-2 text-gray-700 bg-white">
            <option value="">Alle nivåer</option>
            <option value="rød">Rød</option>
            <option value="gul">Gul</option>
            <option value="grønn">Grønn</option>
          </select>
          <select class="border rounded-lg px-3 py-2 text-gray-700 bg-white">
            <option value="">Alle statuser</option>
            <option value="true">Aktiv</option>
            <option value="false">Inaktiv</option>
          </select>
        </div>
      </div>

      <table class="w-full">
        <thead class="bg-gray-50 text-xs text-gray-700 uppercase">
        <tr>
          <th class="px-4 py-3 text-left">Tittel</th>
          <th class="px-4 py-3 text-left">Nivå</th>
          <th class="px-4 py-3 text-left">Plassering</th>
          <th class="px-4 py-3 text-left">Tidspunkt</th>
          <th class="px-4 py-3 text-left">Status</th>
          <th class="px-4 py-3 text-center">Handlinger</th>
        </tr>
        </thead>
        <tbody class="divide-y">
        <tr v-for="event in events" :key="event.id" class="hover:bg-gray-50">
          <td class="px-4 py-3">
            <div>
              <p class="font-medium text-gray-800">{{ event.title }}</p>
              <p class="text-sm text-gray-600">{{ event.description }}</p>
            </div>
          </td>
          <td class="px-4 py-3">
              <span :class="`px-2 py-1 rounded-full text-xs font-medium ${getLevelClass(event.level)}`">
                {{ event.level.toUpperCase() }}
              </span>
          </td>
          <td class="px-4 py-3 text-sm text-gray-700">{{ event.location }}</td>
          <td class="px-4 py-3 text-sm text-gray-700">{{ new Date(event.timestamp).toLocaleString('no-NO') }}</td>
          <td class="px-4 py-3">
              <span :class="`px-2 py-1 rounded-full text-xs font-medium ${event.active ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-800'}`">
                {{ event.active ? 'Aktiv' : 'Inaktiv' }}
              </span>
          </td>
          <td class="px-4 py-3">
            <div class="flex justify-center space-x-2">
              <button class="p-1 text-blue-600 hover:text-blue-800">
                <Edit class="h-4 w-4" />
              </button>
              <button class="p-1 text-red-600 hover:text-red-800" @click="deleteItem(event.id)">
                <Trash2 class="h-4 w-4" />
              </button>
            </div>
          </td>
        </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>
