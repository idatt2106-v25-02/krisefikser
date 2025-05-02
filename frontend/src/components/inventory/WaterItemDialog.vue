<script setup lang="ts">
import { Plus } from 'lucide-vue-next'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { CardFooter } from '@/components/ui/card'
import { FormControl, FormField, FormItem, FormLabel } from '@/components/ui/form'
import { useForm } from 'vee-validate'
import { toTypedSchema } from '@vee-validate/zod'
import * as z from 'zod'
import BaseItemDialog from './BaseItemDialog.vue'

type WaterItem = {
  name: string
  amount: number
}

const _props = defineProps<{
  isOpen: boolean
  categoryId?: string
  categoryName?: string
}>()

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'addItem', item: WaterItem): void
}>()

// Form validation schema
const schema = toTypedSchema(
  z.object({
    name: z.string().min(1, 'Navn er påkrevd'),
    amount: z.number().min(0.1, 'Må være større enn 0'),
  }),
)

// Form handling
const { handleSubmit } = useForm({
  validationSchema: schema,
  initialValues: {
    name: '',
    amount: 1,
  },
})

function onSubmit(values: WaterItem) {
  const newItem = {
    id: crypto.randomUUID(),
    name: values.name,
    amount: values.amount,
    unit: 'L', // Fixed unit for water
  }
  emit('addItem', newItem)
  emit('close')
}
</script>

<template>
  <BaseItemDialog
    :is-open="isOpen"
    :category-id="categoryId"
    :category-name="categoryName"
    title="Legg til vann"
    @close="emit('close')"
  >
    <form @submit.prevent="handleSubmit(onSubmit)" class="space-y-4">
      <FormField name="name">
        <FormItem>
          <FormLabel class="text-gray-700">Navn</FormLabel>
          <FormControl>
            <Input placeholder="Vann" />
          </FormControl>
        </FormItem>
      </FormField>

      <FormField name="amount">
        <FormItem>
          <FormLabel class="text-gray-700">Mengde (liter)</FormLabel>
          <FormControl>
            <Input type="number" min="0.1" step="0.1" />
          </FormControl>
        </FormItem>
      </FormField>

      <CardFooter class="px-0 pt-4">
        <Button type="submit" class="w-full bg-blue-600 hover:bg-blue-700">
          <Plus class="mr-2 h-4 w-4" /> Legg til
        </Button>
      </CardFooter>
    </form>
  </BaseItemDialog>
</template>
