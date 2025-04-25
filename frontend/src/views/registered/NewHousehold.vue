<script setup lang="ts">
import { ref } from 'vue'
import AddressForm from '@/components/layout/AddressForm.vue'

// Props
defineProps({
  colorTheme: {
    type: String,
    default: 'blue',
    validator: (value: string) => ['blue', 'yellow', 'green'].includes(value),
  },
})

// Emits
const emit = defineEmits(['submit', 'cancel'])

// Form handling
// eslint-disable-next-line @typescript-eslint/no-explicit-any
const onSubmit = (values: any) => {
  emit('submit', values)
}

const isFormVisible = ref(true)
</script>

<template>
  <div class="new-household-container">
    <transition name="fade">
      <AddressForm
        v-if="isFormVisible"
        :colorTheme="colorTheme"
        :includeHouseholdName="true"
        title="Opprett ny husstand"
        description="Fyll inn detaljene for din nye husstand"
        submitButtonText="Opprett husstand"
        @submit="onSubmit"
        @cancel="emit('cancel')"
      />
    </transition>
  </div>
</template>
