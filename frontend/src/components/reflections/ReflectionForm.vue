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
} from '@/api/generated/model';
import { useQueryClient } from '@tanstack/vue-query';
import { Button as BaseButton } from '@/components/ui/button';
import { Input as BaseInput } from '@/components/ui/input';
import { Label as BaseLabel } from '@/components/ui/label';
import { RichTextEditor } from '@/components/ui/rich-text-editor';
import { Select as BaseSelect, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';

export default defineComponent({
  name: 'ReflectionForm',
  components: { BaseButton, BaseInput, BaseLabel, RichTextEditor, BaseSelect, SelectContent, SelectItem, SelectTrigger, SelectValue },
  props: {
    eventId: { type: Number as PropType<number>, required: true },
    initialData: { type: Object as PropType<ReflectionResponse | null>, default: null },
  },
  emits: ['submitted', 'cancel', 'created', 'updated' ],
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

    const mutation = useCreateReflection({
      mutation: {
        onSuccess: () => {
          emit('created');
        }
      }
    });
    const updateMutation = useUpdateReflection({
      mutation: {
        onSuccess: () => {
          emit('updated');
        }
      }
    });

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
<template>
  <form @submit.prevent="submitReflection" class="flex flex-col h-full">
    <div class="flex-1 overflow-y-auto space-y-6 pr-2">
      <div>
        <BaseLabel for="reflectionTitle" class="block text-sm font-medium text-gray-700">Tittel</BaseLabel>
        <BaseInput
          type="text"
          id="reflectionTitle"
          v-model="reflectionData.title"
          required
          class="mt-1 w-full"
          placeholder="Gi refleksjonen en tittel"
        />
      </div>

      <div>
        <BaseLabel for="reflectionContent" class="block text-sm font-medium text-gray-700">Innhold</BaseLabel>
        <RichTextEditor
          id="reflectionContent"
          v-model="reflectionData.content"
          required
          placeholder="Skriv din refleksjon her..."
          class="mt-1"
        />
      </div>

      <div>
        <BaseLabel for="reflectionVisibility" class="block text-sm font-medium text-gray-700">Synlighet</BaseLabel>
        <BaseSelect v-model="reflectionData.visibility" id="reflectionVisibility" required>
          <SelectTrigger class="mt-1 w-full">
            <SelectValue placeholder="Velg synlighet" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem :value="VisibilityEnum.PUBLIC">Offentlig</SelectItem>
            <SelectItem :value="VisibilityEnum.HOUSEHOLD">Husstand</SelectItem>
            <SelectItem :value="VisibilityEnum.PRIVATE">Privat</SelectItem>
          </SelectContent>
        </BaseSelect>
      </div>

      <div v-if="mutation.isError.value || updateMutation.isError.value" class="text-red-600 text-sm">
        <p><strong>Noe gikk galt:</strong></p>
        <p>{{ determineErrorMessage() }}</p>
      </div>
    </div>

    <div class="flex justify-end space-x-3 pt-4 border-t mt-6">
        <BaseButton type="button" variant="outline" @click="emits('cancel')">
            Avbryt
        </BaseButton>
        <BaseButton type="submit" :disabled="mutation.isPending.value || updateMutation.isPending.value">
            {{ buttonText }}
        </BaseButton>
    </div>
  </form>
</template>
