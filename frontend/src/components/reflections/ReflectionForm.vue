<template>
  <form @submit.prevent="submitReflection" class="space-y-6">
    <div>
      <Label for="reflectionTitle" class="block text-sm font-medium text-gray-700">Tittel</Label>
      <Input 
        type="text" 
        id="reflectionTitle" 
        v-model="reflectionData.title" 
        required 
        class="mt-1 w-full" 
        placeholder="Gi refleksjonen en tittel"
      />
    </div>

    <div>
      <Label for="reflectionContent" class="block text-sm font-medium text-gray-700">Innhold</Label>
      <RichTextEditor 
        id="reflectionContent" 
        v-model="reflectionData.content" 
        required 
        placeholder="Skriv din refleksjon her..."
        class="mt-1"
      />
    </div>

    <div>
      <Label for="reflectionVisibility" class="block text-sm font-medium text-gray-700">Synlighet</Label>
      <Select v-model="reflectionData.visibility" id="reflectionVisibility" required>
        <SelectTrigger class="mt-1 w-full">
          <SelectValue placeholder="Velg synlighet" />
        </SelectTrigger>
        <SelectContent>
          <SelectItem :value="VisibilityEnum.PUBLIC">Offentlig</SelectItem>
          <SelectItem :value="VisibilityEnum.HOUSEHOLD">Husstand</SelectItem>
          <SelectItem :value="VisibilityEnum.PRIVATE">Privat</SelectItem>
        </SelectContent>
      </Select>
    </div>
    
    <!-- Optional Household ID input - keeping it simple for now -->
    <!-- <div v-if="reflectionData.visibility === VisibilityEnum.HOUSEHOLD">
      <label for="reflectionHouseholdId" class="block text-sm font-medium text-gray-700">Husstand ID (Valgfritt)</label>
      <input type="text" id="reflectionHouseholdId" v-model="reflectionData.householdId" class="..." />
    </div> -->

    <div v-if="mutation.isError.value || updateMutation.isError.value" class="text-red-600 text-sm">
      <p><strong>Noe gikk galt:</strong></p>
      <p>{{ determineErrorMessage() }}</p>
    </div>

    <div class="flex justify-end space-x-3 pt-4 border-t mt-6">
        <Button type="button" variant="outline" @click="emits('cancel')">
            Avbryt
        </Button>
        <Button type="submit" :disabled="mutation.isPending.value || updateMutation.isPending.value">
            {{ buttonText }}
        </Button>
    </div>
  </form>
</template>

<script lang="ts">
import { defineComponent, reactive, type PropType, watch, computed } from 'vue';
import {
  useCreateReflection,
  useUpdateReflection,
  getGetReflectionsByEventIdQueryKey
} from '@/api/generated/reflection/reflection';
import {
  CreateReflectionRequestVisibility,
  type CreateReflectionRequest,
  type ReflectionResponse,
  type UpdateReflectionRequest,
  type ReflectionResponseVisibility
} from '@/api/generated/model';
import { useQueryClient } from '@tanstack/vue-query';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { RichTextEditor } from '@/components/ui/rich-text-editor';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';

export default defineComponent({
  name: 'ReflectionForm',
  components: { Button, Input, Label, RichTextEditor, Select, SelectContent, SelectItem, SelectTrigger, SelectValue },
  props: {
    eventId: { type: Number as PropType<number>, required: true },
    initialData: { type: Object as PropType<ReflectionResponse | null>, default: null },
  },
  emits: ['submitted', 'cancel'],
  setup(props, { emit }) {
    const queryClient = useQueryClient();
    const VisibilityEnum = CreateReflectionRequestVisibility;

    const isEditing = computed(() => !!props.initialData);

    const reflectionData = reactive<CreateReflectionRequest>({
      title: '',
      content: '',
      visibility: VisibilityEnum.PRIVATE,
      eventId: props.eventId,
      householdId: undefined,
    });

    watch(() => props.initialData, (newData) => {
      if (newData) {
        reflectionData.title = newData.title || '';
        reflectionData.content = newData.content || '';
        reflectionData.visibility = newData.visibility as CreateReflectionRequestVisibility || VisibilityEnum.PRIVATE;
        reflectionData.eventId = newData.eventId !== undefined ? newData.eventId : props.eventId;
      } else {
        reflectionData.title = '';
        reflectionData.content = '';
        reflectionData.visibility = VisibilityEnum.PRIVATE;
        reflectionData.eventId = props.eventId;
      }
    }, { immediate: true });

    const mutation = useCreateReflection();
    const updateMutation = useUpdateReflection();

    const buttonText = computed(() => {
      if (isEditing.value) {
        return updateMutation.isPending.value ? 'Lagrer...' : 'Lagre Endringer';
      }
      return mutation.isPending.value ? 'Lagrer...' : 'Lagre Refleksjon';
    });

    const determineErrorMessage = () => {
      const errorObj = isEditing.value ? updateMutation.error.value : mutation.error.value;
      if (errorObj instanceof Error) return errorObj.message;
      return 'Kunne ikke lagre refleksjon. Prøv igjen.';
    };

    const submitReflection = async () => {
      if (!reflectionData.content || reflectionData.content === '<p></p>') {
        alert('Innhold kan ikke være tomt.');
        return;
      }
      
      const commonPayload = {
        title: reflectionData.title,
        content: reflectionData.content,
        visibility: reflectionData.visibility,
        eventId: reflectionData.eventId,
        householdId: reflectionData.householdId,
      };

      try {
        if (isEditing.value && props.initialData && props.initialData.id) {
          await updateMutation.mutateAsync({ 
            id: props.initialData.id, 
            data: commonPayload as UpdateReflectionRequest
          });
          emit('submitted');
        } else {
          await mutation.mutateAsync({ data: commonPayload as CreateReflectionRequest });
          emit('submitted');
        }
        queryClient.invalidateQueries({
          queryKey: getGetReflectionsByEventIdQueryKey(props.eventId)
        });
      } catch (error) {
        console.error("Failed to save reflection:", error);
      }
    };

    return {
      reflectionData,
      submitReflection,
      VisibilityEnum,
      mutation,
      updateMutation,
      buttonText,
      determineErrorMessage,
      emits: emit,
      isEditing
    };
  },
});
</script> 