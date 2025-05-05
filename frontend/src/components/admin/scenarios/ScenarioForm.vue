<!-- ScenarioForm.vue -->
<script setup lang="ts">
import { ref } from 'vue';
import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';
import { Input } from '@/components/ui/input';
import { RichTextEditor } from '@/components/ui/rich-text-editor';

const props = defineProps({
  scenario: {
    type: Object,
    required: true,
    default: () => ({
      id: '',
      title: '',
      content: ''
    })
  },
  isEditing: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['save', 'cancel']);

const formData = ref({
  id: props.scenario.id || '',
  title: props.scenario.title || '',
  content: props.scenario.content || ''
});

const submitForm = () => {
  if (!formData.value.title || !formData.value.content) {
    alert('Vennligst fyll ut alle obligatoriske felt');
    return;
  }

  emit('save', {
    id: formData.value.id,
    title: formData.value.title,
    content: formData.value.content
  });
};
</script>

<template>
  <form @submit.prevent="submitForm" class="space-y-4">
    <!-- Title -->
    <div>
      <Label for="title" class="block text-sm font-medium text-gray-700">Tittel</Label>
      <Input
        id="title"
        v-model="formData.title"
        type="text"
        class="mt-1 w-full"
        required
        placeholder="F.eks. Strømbrudd, Flom, etc."
      />
    </div>

    <!-- Content -->
    <div>
      <Label for="content" class="block text-sm font-medium text-gray-700">Innhold</Label>
      <RichTextEditor
        id="content"
        v-model="formData.content"
        placeholder="Detaljert informasjon om scenarioet"
      />
    </div>

    <!-- Form Actions -->
    <div class="flex justify-end space-x-3 pt-4 border-t">
      <Button type="button" variant="outline" @click="$emit('cancel')">
        Avbryt
      </Button>
      <Button type="submit" class="bg-blue-600 hover:bg-blue-700 text-white">
        {{ isEditing ? 'Lagre endringer' : 'Opprett scenario' }}
      </Button>
    </div>
  </form>
</template>
