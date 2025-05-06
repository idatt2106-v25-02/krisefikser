<!-- AddMemberDialog.vue -->
<script setup lang="ts">
import { ref } from 'vue';
import { Button } from '@/components/ui/button';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import UserSelect from './UserSelect.vue';
import type { HouseholdResponse } from '@/api/generated/model';

const props = defineProps<{
  household: HouseholdResponse;
  open: boolean;
}>();

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void;
  (e: 'add', data: { householdId: string; userId: string }): void;
}>();

const selectedUserId = ref('');

const handleAdd = () => {
  if (!selectedUserId.value) return;

  emit('add', {
    householdId: props.household.id,
    userId: selectedUserId.value
  });

  // Reset and close
  selectedUserId.value = '';
  emit('update:open', false);
};
</script>

<template>
  <Dialog :open="open" @update:open="val => emit('update:open', val)">
    <DialogContent class="sm:max-w-md">
      <DialogHeader>
        <DialogTitle>Legg til medlem</DialogTitle>
        <DialogDescription>
          Velg en bruker å legge til i husstanden {{ household.name }}:
        </DialogDescription>
      </DialogHeader>
      <div class="mt-4">
        <UserSelect v-model="selectedUserId" />
      </div>
      <DialogFooter class="flex gap-2 mt-4">
        <Button
          variant="outline"
          @click="emit('update:open', false)"
          class="flex-1"
        >
          Avbryt
        </Button>
        <Button
          variant="default"
          @click="handleAdd"
          class="flex-1"
          :disabled="!selectedUserId"
        >
          Legg til
        </Button>
      </DialogFooter>
    </DialogContent>
  </Dialog>
</template>
