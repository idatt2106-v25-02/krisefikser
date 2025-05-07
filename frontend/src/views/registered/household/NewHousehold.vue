<script setup lang="ts">
import { ref } from 'vue'
import AddressForm from '@/components/layout/AddressForm.vue'
import { useCreateHousehold } from '@/api/generated/household/household.ts'
import type { CreateHouseholdRequest, HouseholdResponse } from '@/api/generated/model'
import router from '@/router'

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

// Mutation
const {
  mutate: createHousehold,
  isPending,
  isError,
  error,
} = useCreateHousehold({
  mutation: {
    onSuccess: (data: HouseholdResponse) => {
      emit('submit', data)
    },
    onError: (error: Error) => {
      console.error('Failed to create household:', error)
    },
  },
})

// Form handling
const onSubmit = (values: CreateHouseholdRequest) => {
  createHousehold({ data: values })
  router.push({ name: 'household' })
}

const isFormVisible = ref(true)
</script>

<template>
  <div class="new-household-container mt-50">
    <transition name="fade">
      <AddressForm
        v-if="isFormVisible"
        :colorTheme="colorTheme"
        :includeHouseholdName="true"
        title="Opprett ny husstand"
        description="Fyll inn detaljene for din nye husstand"
        :submitButtonText="isPending ? 'Oppretter...' : 'Opprett husstand'"
        :isLoading="isPending"
        :error="isError ? error?.message : undefined"
        @submit="onSubmit"
        @cancel="emit('cancel')"
      />
    </transition>
  </div>
</template>
