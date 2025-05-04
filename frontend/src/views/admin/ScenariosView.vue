<!-- ScenariosSection.vue -->
<script setup lang="ts">
import { ref } from 'vue';
import {
  PlusCircle,
  Edit,
  Trash2,
  X
} from 'lucide-vue-next';

// Import components
import { Button } from '@/components/ui/button';
import AdminLayout from '@/components/admin/AdminLayout.vue';
import ScenarioForm from '@/components/admin/scenarios/ScenarioForm.vue';

// Mock data for scenarios
const scenarios = ref([
  { id: '1', title: 'Strømbrudd', description: 'Forberedelser ved langvarig strømbrudd', content: 'Informasjon om hva du bør gjøre før, under og etter et strømbrudd. Ha alltid lommelykt, stearinlys, fyrstikker og nødladere tilgjengelig.' },
  { id: '2', title: 'Flom', description: 'Håndtering av flom og oversvømmelse', content: 'Informasjon om hvordan du kan forberede deg på flom og hva du bør gjøre hvis det oppstår oversvømmelser i ditt område.' },
  { id: '3', title: 'Pandemi', description: 'Forebyggende tiltak ved pandemi', content: 'Informasjon om smittevern, hygiene og andre forholdsregler under en pandemi. Følg alltid myndighetenes anbefalinger.' }
]);

// State for the form dialog
const showForm = ref(false);
const isEditing = ref(false);
const currentScenario = ref({
  id: '',
  title: '',
  description: '',
  content: ''
});

const openAddForm = () => {
  currentScenario.value = {
    id: '',
    title: '',
    description: '',
    content: ''
  };
  isEditing.value = false;
  showForm.value = true;
};

const openEditForm = (scenario) => {
  currentScenario.value = { ...scenario };
  isEditing.value = true;
  showForm.value = true;
};

const closeForm = () => {
  showForm.value = false;
};

const saveScenario = (scenarioData) => {
  if (isEditing.value) {
    // Update existing scenario
    const index = scenarios.value.findIndex(s => s.id === scenarioData.id);
    if (index !== -1) {
      scenarios.value[index] = { ...scenarioData };
    }
  } else {
    // Add new scenario
    const newId = (Math.max(...scenarios.value.map(s => parseInt(s.id)), 0) + 1).toString();
    scenarios.value.push({
      ...scenarioData,
      id: newId
    });
  }

  closeForm();
};

const deleteItem = (id) => {
  if (confirm('Er du sikker på at du vil slette dette scenarioet?')) {
    console.log(`Deleting scenario with ID: ${id}`);
    scenarios.value = scenarios.value.filter(s => s.id !== id);
  }
};
</script>

<template>
  <AdminLayout>
    <div class="p-6">
      <div class="flex justify-between items-center mb-6">
        <h2 class="text-2xl font-bold text-gray-800">Krisescenarioer</h2>
        <Button
          class="flex items-center bg-blue-600 hover:bg-blue-700 text-white"
          @click="openAddForm"
        >
          <PlusCircle class="h-4 w-4 mr-1" />
          Nytt scenario
        </Button>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <div v-for="scenario in scenarios" :key="scenario.id" class="bg-white rounded-lg shadow overflow-hidden">
          <div class="p-4">
            <h3 class="text-lg font-medium text-gray-800 mb-2">{{ scenario.title }}</h3>
            <p class="text-gray-600 mb-4">{{ scenario.description }}</p>
            <div class="flex justify-end space-x-2">
              <Button
                variant="ghost"
                size="icon"
                class="text-blue-600 hover:text-blue-800 p-2 h-auto"
                @click="openEditForm(scenario)"
              >
                <Edit class="h-5 w-5" />
              </Button>
              <Button
                variant="ghost"
                size="icon"
                class="text-red-600 hover:text-red-800 p-2 h-auto"
                @click="deleteItem(scenario.id)"
              >
                <Trash2 class="h-5 w-5" />
              </Button>
            </div>
          </div>
        </div>

        <div
          class="bg-gray-50 rounded-lg border-2 border-dashed border-gray-300 flex items-center justify-center p-8 cursor-pointer"
          @click="openAddForm"
        >
          <Button variant="ghost" class="text-blue-600 hover:text-blue-800 flex flex-col items-center">
            <PlusCircle class="h-10 w-10" />
            <span class="mt-2 font-medium">Legg til nytt scenario</span>
          </Button>
        </div>
      </div>

      <!-- Form Dialog -->
      <div v-if="showForm" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
        <div class="bg-white rounded-lg shadow-lg w-full max-w-2xl max-h-[90vh] overflow-auto">
          <div class="flex justify-between items-center p-4 border-b">
            <h3 class="text-lg font-medium">
              {{ isEditing ? 'Rediger scenario' : 'Legg til nytt scenario' }}
            </h3>
            <Button variant="ghost" size="icon" @click="closeForm" class="h-auto p-1">
              <X class="h-5 w-5" />
            </Button>
          </div>

          <div class="p-4">
            <ScenarioForm
              :scenario="currentScenario"
              :is-editing="isEditing"
              @save="saveScenario"
              @cancel="closeForm"
            />
          </div>
        </div>
      </div>
    </div>
  </AdminLayout>
</template>
