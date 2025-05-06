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

// Define interfaces for our data structures
interface FoodItem {
  id: string;
  name: string;
  kcal: number;
  expiryDate: string;
}

interface FormValues {
  name: string;
  kcal: number;
  expiryDate: string;
}

const _props = defineProps<{
  isOpen: boolean
  categoryId?: string
  categoryName?: string
}>()

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'addItem', item: FoodItem): void
}>()

// Form validation schema
const schema = toTypedSchema(
  z.object({
    name: z.string().min(1, 'Navn er påkrevd'),
    kcal: z.number().min(0, 'Kalorier må være 0 eller mer'),
    expiryDate: z.string().min(1, 'Utløpsdato er påkrevd'),
  }),
)

// Form handling
const { handleSubmit } = useForm<FormValues>({
  validationSchema: schema,
  initialValues: {
    name: '',
    kcal: 0,
    expiryDate: new Date().toISOString().split('T')[0],
  },
})

function onSubmit(values: FormValues): void {
  const newItem: FoodItem = {
    id: crypto.randomUUID(),
    name: values.name,
    kcal: values.kcal,
    expiryDate: values.expiryDate,
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
    title="Legg til matvare"
    @close="emit('close')"
  >
    <form @submit.prevent="handleSubmit(onSubmit)" class="space-y-4">
      <FormField name="name">
        <FormItem>
          <FormLabel class="text-gray-700">Navn</FormLabel>
          <FormControl>
            <Input placeholder="Matvare" />
          </FormControl>
        </FormItem>
      </FormField>

      <FormField name="kcal">
        <FormItem>
          <FormLabel class="text-gray-700">Kalorier (kcal)</FormLabel>
          <FormControl>
            <Input type="number" min="0" step="1" />
          </FormControl>
        </FormItem>
      </FormField>

      <FormField name="expiryDate">
        <FormItem>
          <FormLabel class="text-gray-700">Utløpsdato</FormLabel>
          <FormControl>
            <Input type="date" />
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
