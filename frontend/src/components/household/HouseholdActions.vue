<script setup lang="ts">
import { ref } from 'vue'
import { ConfirmationDialog } from '@/components/ui/confirmation-dialog'
import { Button } from '@/components/ui/button'

const _props = defineProps<{
  householdId: string
}>()

const emit = defineEmits<{
  (e: 'leave'): void
  (e: 'delete'): void
}>()

const showLeaveDialog = ref(false)
const showDeleteDialog = ref(false)

const handleLeaveHousehold = () => {
  showLeaveDialog.value = true
}

const handleDeleteHousehold = () => {
  showDeleteDialog.value = true
}

const confirmLeave = () => {
  emit('leave')
  showLeaveDialog.value = false
}

const confirmDelete = () => {
  emit('delete')
  showDeleteDialog.value = false
}
</script>

<template>
  <div class="flex gap-2">
    <Button variant="outline" @click="handleLeaveHousehold"> Forlat husstand </Button>
    <Button variant="destructive" @click="handleDeleteHousehold"> Slett husstand </Button>

    <!-- Leave Household Confirmation -->
    <ConfirmationDialog
      :is-open="showLeaveDialog"
      title="Forlate husstand"
      description="Er du sikker på at du vil forlate denne husstanden?"
      confirm-text="Forlat"
      @confirm="confirmLeave"
      @cancel="showLeaveDialog = false"
    />

    <!-- Delete Household Confirmation -->
    <ConfirmationDialog
      :is-open="showDeleteDialog"
      title="Slett husstand"
      description="Er du sikker på at du vil slette denne husstanden? Dette kan ikke angres."
      confirm-text="Slett"
      variant="destructive"
      @confirm="confirmDelete"
      @cancel="showDeleteDialog = false"
    />
  </div>
</template>
