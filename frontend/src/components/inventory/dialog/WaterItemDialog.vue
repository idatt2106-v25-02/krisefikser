<script setup lang="ts">
import { Plus, Minus } from 'lucide-vue-next'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { FormControl, FormField, FormItem, FormLabel } from '@/components/ui/form'
import { useForm } from 'vee-validate'
import { toTypedSchema } from '@vee-validate/zod'
import * as z from 'zod'
import BaseItemDialog from './BaseItemDialog.vue'
import { computed, ref } from 'vue'

type WaterItem = {
  amount: number;
}

const props = defineProps<{
  isOpen: boolean;
  categoryId?: string;
  categoryName?: string;
  currentAmount?: number;
}>()

const emit = defineEmits<{
  (e: 'close'): void;
  (e: 'add-item', item: WaterItem): void;
}>()

// Track operation type (add or remove)
const operationType = ref<'add' | 'remove'>('add')

// Form validation schema
const schema = toTypedSchema(
  z.object({
    amount: z.number().min(0.1, 'Mengde må være større enn 0'),
  }),
)

// Form handling
const { handleSubmit, values } = useForm({
  validationSchema: schema,
  initialValues: {
    amount: 1,
  },
})

const currentWaterAmount = computed(() => props.currentAmount || 0)

// Switch between add and remove operations
function setOperation(type: 'add' | 'remove') {
  operationType.value = type
}

function onSubmit(values: { amount: number }) {
  const finalAmount = operationType.value === 'add'
    ? values.amount
    : -values.amount // Negative for removal

  const newItem = {
    amount: finalAmount,
  }

  emit('add-item', newItem)
  emit('close')
}
</script>

<template>
  <BaseItemDialog
    :is-open="isOpen"
    :category-id="categoryId"
    :category-name="categoryName"
    title="Oppdater vannmengde"
    @close="emit('close')"
  >
    <div class="mb-6 p-4 bg-blue-50 rounded-lg">
      <div class="text-lg font-medium text-blue-700 mb-2">Nåværende vannmengde</div>
      <div class="text-2xl font-bold text-blue-800">{{ currentWaterAmount }} liter</div>
    </div>

    <div class="mb-6">
      <div class="flex space-x-2">
        <Button
          class="flex-1"
          :class="operationType === 'add' ? 'bg-blue-600' : 'bg-gray-200 text-gray-700'"
          @click="setOperation('add')"
        >
          <Plus class="mr-2 h-4 w-4" /> Legg til vann
        </Button>
        <Button
          class="flex-1"
          :class="operationType === 'remove' ? 'bg-blue-600' : 'bg-gray-200 text-gray-700'"
          @click="setOperation('remove')"
        >
          <Minus class="mr-2 h-4 w-4" /> Trekk fra vann
        </Button>
      </div>
    </div>

    <form @submit.prevent="handleSubmit(onSubmit)" class="space-y-4">
      <FormField v-slot="{ field }" name="amount">
        <FormItem>
          <FormLabel class="text-gray-700">
            {{ operationType === 'add' ? 'Antall liter å legge til' : 'Antall liter å trekke fra' }}
          </FormLabel>
          <FormControl>
            <Input v-bind="field" type="number" min="0.1" step="0.1" />
          </FormControl>
        </FormItem>
      </FormField>

      <div class="mt-8">
  <div v-if="values.amount && values.amount > 0" class="p-4 bg-gray-50 rounded-lg mb-4">
    <div class="text-lg font-medium mb-2">Oppsummering:</div>
    <div class="text-xl">
      <span v-if="operationType === 'add'">{{ currentWaterAmount }} + {{ values.amount ?? 0 }} = <strong>{{ currentWaterAmount + (values.amount ?? 0) }}</strong> liter</span>
      <span v-else>{{ currentWaterAmount }} - {{ values.amount ?? 0 }} = <strong>{{ Math.max(0, currentWaterAmount - (values.amount ?? 0)) }}</strong> liter</span>
    </div>
  </div>
        <Button type="submit" class="w-full bg-blue-600 hover:bg-blue-700 py-3 text-lg">
          <span v-if="operationType === 'add'">
            <Plus class="mr-2 h-5 w-5 inline-block" /> Legg til vann
          </span>
          <span v-else>
            <Minus class="mr-2 h-5 w-5 inline-block" /> Trekk fra vann
          </span>
        </Button>
      </div>
    </form>
  </BaseItemDialog>
</template>
