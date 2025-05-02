<!-- EditHouseholdDialog.vue -->
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
import type { HouseholdResponse, CreateHouseholdRequest } from '@/api/generated/model';

const props = defineProps<{
  household: HouseholdResponse | null;
  open: boolean;
}>();

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void;
  (e: 'save', data: { id: string; data: CreateHouseholdRequest }): void;
}>();

const updateData = ref<CreateHouseholdRequest>({
  name: props.household?.name || '',
  latitude: props.household?.latitude || 0,
  longitude: props.household?.longitude || 0,
  address: props.household?.address || '',
  city: '',
  postalCode: ''
});

const handleSave = () => {
  if (!props.household?.id) return;
  emit('save', {
    id: props.household.id,
    data: updateData.value
  });
  emit('update:open', false);
};
</script>

<template>
  <Dialog :open="open" @update:open="val => emit('update:open', val)">
    <DialogContent>
      <DialogHeader>
        <DialogTitle>Rediger husholdning</DialogTitle>
        <DialogDescription>
          Oppdater informasjonen om husholdningen
        </DialogDescription>
      </DialogHeader>
      <div class="space-y-4">
        <div class="space-y-2">
          <label class="text-sm font-medium">Navn</label>
          <input
            v-model="updateData.name"
            class="w-full p-2 border rounded-md"
            type="text"
          />
        </div>
        <div class="space-y-2">
          <label class="text-sm font-medium">Adresse</label>
          <input
            v-model="updateData.address"
            class="w-full p-2 border rounded-md"
            type="text"
          />
        </div>
        <div class="grid grid-cols-2 gap-4">
          <div class="space-y-2">
            <label class="text-sm font-medium">Breddegrad</label>
            <input
              v-model="updateData.latitude"
              class="w-full p-2 border rounded-md"
              type="number"
              step="0.0001"
            />
          </div>
          <div class="space-y-2">
            <label class="text-sm font-medium">Lengdegrad</label>
            <input
              v-model="updateData.longitude"
              class="w-full p-2 border rounded-md"
              type="number"
              step="0.0001"
            />
          </div>
        </div>
      </div>
      <DialogFooter>
        <Button variant="outline" @click="emit('update:open', false)">Avbryt</Button>
        <Button @click="handleSave">Lagre endringer</Button>
      </DialogFooter>
    </DialogContent>
  </Dialog>
</template>
