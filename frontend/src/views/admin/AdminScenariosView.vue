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
import ScenarioForm from '@/components/admin/scenario/ScenarioForm.vue';

// Import scenario API hooks
import {
  useGetAllScenarios,
  useCreateScenario,
  useUpdateScenario,
  useDeleteScenario
} from '@/api/generated/scenario/scenario.ts';

interface Scenario {
  id: string;
  title: string;
  content: string;
}
// Fetch all scenarios
const { data: scenarios, refetch } = useGetAllScenarios();

// State for the form dialog
const showForm = ref(false);
const isEditing = ref(false);
const currentScenario = ref({
  id: '',
  title: '',
  content: ''
});

const openAddForm = () => {
  currentScenario.value = {
    id: '',
    title: '',
    content: ''
  };
  isEditing.value = false;
  showForm.value = true;
};

const openEditForm = (scenario: Scenario) => {
  currentScenario.value = { ...scenario };
  isEditing.value = true;
  showForm.value = true;
};

const closeForm = () => {
  showForm.value = false;
};

// Mutations
const { mutate: createScenario } = useCreateScenario({ mutation: { onSuccess: () => { refetch(); closeForm(); } } });
const { mutate: updateScenario } = useUpdateScenario({ mutation: { onSuccess: () => { refetch(); closeForm(); } } });
const { mutate: deleteScenario } = useDeleteScenario({
  mutation: {
    onSuccess: () => {
      refetch();
    },
  },
});

const saveScenario = (scenarioData: Scenario) => {
  const payload = {
    title: scenarioData.title,
    content: scenarioData.content,
  };
  if (isEditing.value) {
    updateScenario({ id: scenarioData.id, data: payload });
  } else {
    createScenario({ data: payload });
  }
};

const deleteItem = (id: string) => {
  if (confirm('Er du sikker på at du vil slette dette scenarioet?')) {
    deleteScenario({ id });
  }
};

// Get a preview of content
const getContentPreview = (content: string) => {
  if (!content) return '';

  // Clean up any HTML tags
  const cleanContent = content.replace(/<[^>]*>/g, '');

  return cleanContent.length > 120
    ? cleanContent.substring(0, 120) + '...'
    : cleanContent;
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

      <div class="grid grid-cols-1 md:grid-cols-3 gap-6 max-h-[750px] overflow-y-auto pr-2">
        <div
          v-for="scenario in scenarios"
          :key="scenario.id"
          class="bg-white rounded-lg shadow overflow-hidden h-[180px] flex flex-col"
        >
          <div class="p-4 flex flex-col h-full">
            <h3 class="text-lg font-medium text-gray-800 mb-2 line-clamp-1">{{ scenario.title }}</h3>
            <p class="text-gray-600 mb-2 line-clamp-3 flex-grow">
              {{ getContentPreview(scenario.content) }}
            </p>
            <div class="flex justify-end space-x-2 mt-auto">
              <Button
                variant="ghost"
                size="icon"
                class="text-blue-600 hover:text-blue-800 p-2 h-auto"
                @click.stop="openEditForm(scenario)"
              >
                <Edit class="h-5 w-5" />
              </Button>
              <Button
                variant="ghost"
                size="icon"
                class="text-red-600 hover:text-red-800 p-2 h-auto"
                @click.stop="deleteItem(scenario.id)"
              >
                <Trash2 class="h-5 w-5" />
              </Button>
            </div>
          </div>
        </div>

        <div
          class="bg-gray-50 rounded-lg border-2 border-dashed border-gray-300 flex items-center justify-center p-8 cursor-pointer h-[180px]"
          @click="openAddForm"
        >
          <div class="text-blue-600 hover:text-blue-800 flex flex-col items-center">
            <PlusCircle class="h-10 w-10" />
            <span class="mt-2 font-medium">Legg til nytt scenario</span>
          </div>
        </div>
      </div>

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
