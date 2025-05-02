<script setup lang="ts">
import { X } from 'lucide-vue-next'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
//import { useForm } from 'vee-validate'
//import { toTypedSchema } from '@vee-validate/zod'
//import * as z from 'zod'

// Define interfaces for our data structures
// interface FormValues {
//   name: string;
//   [key: string]: any;
// }
const _props = defineProps<{
  isOpen: boolean
  categoryId?: string
  categoryName?: string
  title?: string
}>()

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'addItem', item: Record<string, unknown>): void
}>()

// Base form validation schema
// const baseSchema = toTypedSchema(
//   z.object({
//     name: z.string().min(1, 'Navn er påkrevd'),
//   }),
// )

// Form handling

// const { handleSubmit } = useForm<FormValues>({
//   validationSchema: baseSchema,
//   initialValues: {
//     name: '',
//   },
// })

function closeDialog(): void {
  emit('close')
}
// function submitForm(values: FormValues): void {
//   emit('addItem', values)
//   emit('close')
// }
</script>

<template>
  <div
    v-if="isOpen"
    class="fixed inset-0 bg-black/25 z-50 flex items-center justify-center p-4"
    @click.self="closeDialog"
  >
    <Card class="w-full max-w-md">
      <CardHeader class="flex flex-row items-center justify-between space-y-0 pb-2">
        <CardTitle class="text-xl font-semibold">{{ title || 'Legg til vare' }}</CardTitle>
        <Button variant="ghost" size="icon" @click="closeDialog">
          <X class="h-5 w-5" />
        </Button>
      </CardHeader>

      <CardContent>
        <slot></slot>
      </CardContent>
    </Card>
  </div>
</template>
