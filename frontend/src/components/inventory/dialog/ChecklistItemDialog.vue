<script setup lang="ts">
import { useForm } from 'vee-validate'
import { toTypedSchema } from '@vee-validate/zod'
import * as z from 'zod'
import BaseItemDialog from './BaseItemDialog.vue'
import { Checkbox } from '@/components/ui/checkbox'
import { computed } from 'vue'
import { Plus } from 'lucide-vue-next'
import { Button } from '@/components/ui/button'
import { CardFooter } from '@/components/ui/card'
import { FormControl, FormField, FormItem } from '@/components/ui/form'

// Define interfaces for our data structures
interface PredefinedItem {
  name: string;
  description: string;
}

interface ChecklistItem {
  id: string;
  name: string;
  isChecked: boolean;
}

interface FormValues {
  items: Array<{
    name: string;
    isChecked: boolean;
  }>;
}

const props = defineProps<{
  isOpen: boolean
  categoryId?: string
  categoryName?: string
}>()

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'add-item', item: any): void
}>()

// Predefined checklist items based on DSB's recommendations
const predefinedItems: Record<string, PredefinedItem[]> = {
  power: [
    { name: 'Batterier (AA/AAA)', description: 'Nok batterier til radio og lommelykter' },
    { name: 'Lommelykt', description: 'Med ekstra batterier' },
    { name: 'Stearinlys', description: 'Nok lys til flere dager' },
    { name: 'Fyrstikker', description: 'I tett beholdere' },
    { name: 'Batteridrevet radio', description: 'For å følge med på nyheter' },
  ],
  comm: [
    { name: 'DAB-radio', description: 'Med batterier' },
    { name: 'Mobiltelefon', description: 'Med lader og powerbank' },
    { name: 'Ladere', description: 'For alle elektroniske enheter' },
    { name: 'Powerbank', description: 'For å lade mobiltelefon' },
  ],
  health: [
    { name: 'Førstehjelpssett', description: 'Grunnleggende førstehjelp' },
    { name: 'Våtservietter', description: 'For hygiene' },
    { name: 'Toalettpapir', description: 'Nok til flere dager' },
    { name: 'Håndsprit', description: 'For håndhygiene' },
    { name: 'Såpe', description: 'For personlig hygiene' },
    { name: 'Tannbørste og tannkrem', description: 'For munnhygiene' },
  ],
}

// Get items for current category
const categoryItems = computed(() => {
  if (props.categoryId && props.categoryId in predefinedItems) {
    return predefinedItems[props.categoryId as keyof typeof predefinedItems]
  }
  return [] as PredefinedItem[]
})

// Form validation schema
const schema = toTypedSchema(
  z.object({
    items: z.array(
      z.object({
        name: z.string(),
        isChecked: z.boolean().default(false),
      })
    ),
  }),
)

// Form handling
const { handleSubmit } = useForm<FormValues>({
  validationSchema: schema,
  initialValues: {
    items: categoryItems.value.map(item => ({
      name: item.name,
      isChecked: false,
    })),
  },
})

function onSubmit(values: FormValues): void {
  // Emit an add-item event for each checked item
  values.items.filter(item => item.isChecked).forEach(item => {
    const newItem = {
      id: crypto.randomUUID(),
      name: item.name,
      checked: false,
    }
    emit('add-item', newItem)
  })
  emit('close')
}
</script>

<template>
  <BaseItemDialog
    :is-open="isOpen"
    :category-id="categoryId"
    :category-name="categoryName"
    title="Velg beredskapsgjenstander"
    @close="emit('close')"
  >
    <form @submit.prevent="handleSubmit(onSubmit)" class="space-y-4">
      <div class="space-y-3">
        <div v-for="(item, index) in categoryItems" :key="item.name" class="flex items-start space-x-3">
          <FormField :name="`items.${index}.isChecked`">
            <FormItem class="flex items-start space-x-3 space-y-0">
              <FormControl>
                <Checkbox />
              </FormControl>
            </FormItem>
          </FormField>
          <div class="flex-1">
            <div class="font-medium text-gray-900">{{ item.name }}</div>
            <div class="text-sm text-gray-500">{{ item.description }}</div>
          </div>
        </div>
      </div>

      <CardFooter class="px-0 pt-4">
        <Button type="submit" class="w-full bg-blue-600 hover:bg-blue-700">
          <Plus class="mr-2 h-4 w-4" /> Legg til valgte gjenstander
        </Button>
      </CardFooter>
    </form>
  </BaseItemDialog>
</template>
