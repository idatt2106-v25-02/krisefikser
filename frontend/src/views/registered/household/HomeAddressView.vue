<script setup lang="ts">
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
</script>

<template>
  <div class="py-8 my-10">
    <AddressForm
      :colorTheme="colorTheme"
      :includeHouseholdName="false"
      title="Adresse"
      description="Fyll inn adresseinformasjon"
      submitButtonText="Lagre adresse"
      @submit="onSubmit"
      @cancel="emit('cancel')"
    />
  </div>
</template>
