<!-- EventFormComponent.vue -->
<script setup lang="ts">
import { ref } from 'vue';
import { AlertTriangle } from 'lucide-vue-next';

// Import our UI components
import { Button } from '@/components/ui/button'

const props = defineProps({
  editMode: {
    type: Boolean,
    default: false
  },
  eventData: {
    type: Object,
    default: () => ({
      title: '',
      description: '',
      level: 'gul',
      location: '',
      timestamp: '',
      active: true
    })
  }
});

const emit = defineEmits(['save', 'cancel']);

// Create a local copy of the data
const formData = ref({...props.eventData});

const levels = [
  { value: 'rød', label: 'RØD', description: 'Kritisk hendelse' },
  { value: 'gul', label: 'GUL', description: 'Alvorlig hendelse' },
  { value: 'grønn', label: 'GRØNN', description: 'Mindre hendelse' }
];

const getCurrentDateTime = () => {
  const now = new Date();
  return now.toISOString().slice(0, 16); // Format: "YYYY-MM-DDThh:mm"
};

// Initialize timestamp if not provided
if (!formData.value.timestamp) {
  formData.value.timestamp = getCurrentDateTime();
}

const saveEvent = () => {
  emit('save', formData.value);
};

const cancelForm = () => {
  emit('cancel');
};

const getLevelClass = (level: string) => {
  switch(level.toLowerCase()) {
    case 'rød': return 'bg-red-100 text-red-800 border-red-200';
    case 'gul': return 'bg-yellow-100 text-yellow-800 border-yellow-200';
    case 'grønn': return 'bg-green-100 text-green-800 border-green-200';
    default: return 'bg-gray-100 text-gray-800 border-gray-200';
  }
};
</script>

<template>
  <div class="bg-white rounded-lg shadow p-6">
    <div class="flex items-center mb-6">
      <AlertTriangle class="h-6 w-6 text-orange-500 mr-2" />
      <h3 class="text-lg font-medium">{{ editMode ? 'Rediger hendelse' : 'Legg til ny hendelse' }}</h3>
    </div>

    <form @submit.prevent="saveEvent">
      <div class="space-y-4">
        <!-- Title -->
        <div>
          <label for="title" class="block text-sm font-medium text-gray-700 mb-1">Tittel</label>
          <input
            id="title"
            v-model="formData.title"
            type="text"
            class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
            required
          />
        </div>

        <!-- Description -->
        <div>
          <label for="description" class="block text-sm font-medium text-gray-700 mb-1">Beskrivelse</label>
          <textarea
            id="description"
            v-model="formData.description"
            rows="3"
            class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
          ></textarea>
        </div>

        <!-- Level -->
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Nivå</label>
          <div class="flex space-x-4">
            <div v-for="level in levels" :key="level.value" class="flex">
              <label :class="['flex items-center p-2 border rounded cursor-pointer',
                             formData.level === level.value ? getLevelClass(level.value) + ' border-2' : 'border-gray-300']">
                <input
                  type="radio"
                  :value="level.value"
                  v-model="formData.level"
                  class="sr-only"
                />
                <span class="ml-2 font-medium">{{ level.label }}</span>
                <span class="ml-2 text-xs text-gray-500">{{ level.description }}</span>
              </label>
            </div>
          </div>
        </div>

        <!-- Location -->
        <div>
          <label for="location" class="block text-sm font-medium text-gray-700 mb-1">Plassering</label>
          <input
            id="location"
            v-model="formData.location"
            type="text"
            class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
            required
          />
        </div>

        <!-- Timestamp -->
        <div>
          <label for="timestamp" class="block text-sm font-medium text-gray-700 mb-1">Tidspunkt</label>
          <input
            id="timestamp"
            v-model="formData.timestamp"
            type="datetime-local"
            class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
            required
          />
        </div>

        <!-- Active status -->
        <div class="flex items-center">
          <input
            id="active"
            v-model="formData.active"
            type="checkbox"
            class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
          />
          <label for="active" class="ml-2 block text-sm text-gray-700">Aktiv hendelse</label>
        </div>
      </div>

      <!-- Form buttons -->
      <div class="mt-6 flex space-x-3 justify-end">
        <Button
          variant="outline"
          @click="cancelForm"
          type="button"
        >
          Avbryt
        </Button>
        <Button
          type="submit"
        >
          {{ editMode ? 'Lagre endringer' : 'Opprett hendelse' }}
        </Button>
      </div>
    </form>
  </div>
</template>
