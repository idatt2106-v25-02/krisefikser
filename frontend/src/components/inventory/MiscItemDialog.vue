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
import { Checkbox } from '@/components/ui/checkbox'

// Define interfaces for our data structures
interface PredefinedItem {
  name: string;
  description: string;
}

interface ChecklistItem {
  id: string;
  name: string;
  isChecked: boolean;
  type: 'predefined' | 'custom';
  amount?: number;
  unit?: string;
}

interface FormValues {
  items: Array<{
    name: string;
    isChecked: boolean;
  }>;
  customName?: string;
  customAmount?: number;
  customUnit?: string;
}

const _props = defineProps<{
  isOpen: boolean
  categoryId?: string
  categoryName?: string
}>()

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'addItem', items: ChecklistItem[]): void
}>()

// Predefined checklist items for diverse
const predefinedItems: PredefinedItem[] = [
  { name: 'Kontanter', description: 'Nok kontanter til å klare seg i noen dager' },
  { name: 'Kopier av viktige dokumenter', description: 'Pass, ID, forsikringsdokumenter, etc.' },
  { name: 'Kopier av nøkkler', description: 'Til hus, bil, hytte, etc.' },
  { name: 'Varmt tøy', description: 'Varme klær og ulltepper' },
  { name: 'Regntøy', description: 'Regnjakke og regnbukser' },
  { name: 'Gode sko', description: 'For å kunne gå lengre distanser' },
  { name: 'Verktøy', description: 'Grunnleggende verktøy for reparasjoner' },
  { name: 'Kart', description: 'Papirkart over området' },
  { name: 'Kompass', description: 'For orientering' },
  { name: 'Vekter', description: 'For å kunne veie mat og vann' },
]

// Form validation schema
const schema = toTypedSchema(
  z.object({
    // For predefined checklist items
    items: z.array(
      z.object({
        name: z.string(),
        isChecked: z.boolean().default(false),
      })
    ),
    // For custom items
    customName: z.string().optional(),
    customAmount: z.number().min(0.1, 'Må være større enn 0').optional(),
    customUnit: z.string().optional(),
  }),
)

// Form handling
const { handleSubmit } = useForm<FormValues>({
  validationSchema: schema,
  initialValues: {
    items: predefinedItems.map(item => ({
      name: item.name,
      isChecked: false,
    })),
    customName: '',
    customAmount: 1,
    customUnit: 'stk',
  },
})

function onSubmit(values: FormValues): void {
  const items: ChecklistItem[] = []

  // Add checked predefined items
  const checkedItems = values.items
    .filter((item) => item.isChecked)
    .map((item) => ({
      id: crypto.randomUUID(),
      name: item.name,
      isChecked: true,
      type: 'predefined' as const,
    }))

  items.push(...checkedItems)

  // Add custom item if provided
  if (values.customName && values.customAmount) {
    items.push({
      id: crypto.randomUUID(),
      name: values.customName,
      isChecked: true, // Add the missing isChecked property
      amount: values.customAmount,
      unit: values.customUnit || 'stk',
      type: 'custom' as const,
    })
  }

  emit('addItem', items)
  emit('close')
}
</script>

<template>
  <BaseItemDialog
    :is-open="isOpen"
    :category-id="categoryId"
    :category-name="categoryName"
    title="Legg til diverse"
    @close="emit('close')"
  >
    <form @submit.prevent="handleSubmit(onSubmit)" class="space-y-4">
      <!-- Predefined checklist items -->
      <div>
        <h3 class="text-base font-semibold text-gray-900 mb-2">Anbefalte gjenstander</h3>
        <div class="grid grid-cols-1 sm:grid-cols-2 gap-x-4 gap-y-2 max-h-64 overflow-y-auto pr-2">
          <div v-for="(item, index) in predefinedItems" :key="item.name" class="flex items-start space-x-2">
            <FormField :name="`items.${index}.isChecked`">
              <FormItem class="flex items-start space-x-2 space-y-0">
                <FormControl>
                  <Checkbox />
                </FormControl>
              </FormItem>
            </FormField>
            <div class="flex-1">
              <div class="font-medium text-gray-900 text-sm">{{ item.name }}</div>
              <div class="text-xs text-gray-500 leading-tight">{{ item.description }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- Custom item input -->
      <div class="border-t pt-3">
        <h3 class="text-base font-semibold text-gray-900 mb-2">Legg til annet</h3>
        <div class="space-y-2">
          <FormField name="customName">
            <FormItem>
              <FormLabel class="text-gray-700 text-sm">Navn</FormLabel>
              <FormControl>
                <Input placeholder="Navn på gjenstand" />
              </FormControl>
            </FormItem>
          </FormField>
          <FormField name="customAmount">
            <FormItem>
              <FormLabel class="text-gray-700 text-sm">Antall</FormLabel>
              <FormControl>
                <Input type="number" min="0.1" step="0.1" />
              </FormControl>
            </FormItem>
          </FormField>
        </div>
      </div>

      <CardFooter class="px-0 pt-3">
        <Button type="submit" class="w-full bg-blue-600 hover:bg-blue-700">
          <Plus class="mr-2 h-4 w-4" /> Legg til valgte gjenstander
        </Button>
      </CardFooter>
    </form>
  </BaseItemDialog>
</template>
