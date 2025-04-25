<script setup lang="ts">
import { ref, computed } from 'vue'
import { X, Plus } from 'lucide-vue-next'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle, CardFooter } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue
} from '@/components/ui/select'
import { useForm } from 'vee-validate'
import { toTypedSchema } from '@vee-validate/zod'
import * as z from 'zod'
import { Form, FormControl, FormField, FormItem, FormLabel } from '@/components/ui/form'

interface ProductUnit {
  value: string
  label: string
}

const props = defineProps<{
  isOpen: boolean
  categoryId?: string
  categoryName?: string
}>()

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'addItem', item: any): void
}>()

// Available units based on category
const unitsByCategory = {
  food: [
    { value: 'kg', label: 'kg' },
    { value: 'stk', label: 'stk' },
    { value: 'pakke', label: 'pakke' },
    { value: 'boks', label: 'boks' },
    { value: 'liter', label: 'liter' }
  ],
  water: [
    { value: 'L', label: 'L' },
    { value: 'flaske', label: 'flaske' }
  ],
  health: [
    { value: 'stk', label: 'stk' },
    { value: 'pakke', label: 'pakke' },
    { value: 'rull', label: 'rull' }
  ],
  power: [
    { value: 'stk', label: 'stk' },
    { value: 'pakke', label: 'pakke' }
  ],
  comm: [
    { value: 'stk', label: 'stk' },
  ],
  misc: [
    { value: 'stk', label: 'stk' },
    { value: 'eske', label: 'eske' },
    { value: 'kr', label: 'kr' }
  ]
}

// Get available units for current category
const availableUnits = computed<ProductUnit[]>(() => {
  if (props.categoryId && props.categoryId in unitsByCategory) {
    return unitsByCategory[props.categoryId as keyof typeof unitsByCategory]
  }
  return unitsByCategory.food // Default to food units
})

// Form validation schema
const schema = toTypedSchema(z.object({
  name: z.string().min(1, 'Navn er påkrevd'),
  amount: z.number().min(0.1, 'Må være større enn 0'),
  unit: z.string().min(1, 'Enhet er påkrevd'),
  hasExpiryDate: z.boolean().default(true),
  expiryDate: z.string().optional()
}))

// Form handling
const { handleSubmit } = useForm({
  validationSchema: schema,
  initialValues: {
    name: '',
    amount: 1,
    unit: availableUnits.value[0]?.value || 'stk',
    hasExpiryDate: true,
    expiryDate: new Date().toISOString().split('T')[0]
  }
})

const hasExpiryDate = ref(true)

const onSubmit = handleSubmit((values) => {
  const newItem = {
    id: crypto.randomUUID(),
    name: values.name,
    amount: values.amount,
    unit: values.unit,
    expiryDate: values.hasExpiryDate ? values.expiryDate : null
  }

  emit('addItem', newItem)
  emit('close')
})

function closeDialog() {
  emit('close')
}
</script>

<template>
  <div v-if="isOpen" class="fixed inset-0 bg-black/25 z-50 flex items-center justify-center p-4" @click.self="closeDialog">
    <Card class="w-full max-w-md">
      <CardHeader class="flex flex-row items-center justify-between space-y-0 pb-2">
        <CardTitle class="text-xl font-semibold">Legg til vare</CardTitle>
        <Button variant="ghost" size="icon" @click="closeDialog">
          <X class="h-5 w-5" />
        </Button>
      </CardHeader>

      <CardContent>
 <!-- <Form @submit.prevent="onSubmit" class="space-y-4 pt-4">    -->
  <FormField name="name">
            <FormItem>
              <FormLabel class="text-gray-700">Navn</FormLabel>
              <FormControl>
                <Input placeholder="Tørrvare" />
              </FormControl>
            </FormItem>
          </FormField>

          <!-- Amount and Unit fields -->
          <div class="flex gap-4">
            <FormField name="amount" class="flex-1">
              <FormItem>
                <FormLabel class="text-gray-700">Mengde</FormLabel>
                <FormControl>
                  <Input type="number" min="0.1" step="0.1" />
                </FormControl>
              </FormItem>
            </FormField>

            <FormField name="unit" class="flex-1">
              <FormItem>
                <FormLabel class="text-gray-700">Enhet</FormLabel>
                <Select>
                  <FormControl>
                    <SelectTrigger>
                      <SelectValue placeholder="Velg enhet" />
                    </SelectTrigger>
                  </FormControl>
                  <SelectContent>
                    <SelectItem
                      v-for="unit in availableUnits"
                      :key="unit.value"
                      :value="unit.value"
                    >
                      {{ unit.label }}
                    </SelectItem>
                  </SelectContent>
                </Select>
              </FormItem>
            </FormField>
          </div>

          <!-- Expiry date field -->
          <FormField name="hasExpiryDate">
            <FormItem class="flex flex-row items-start space-x-3 space-y-0 mb-1">
              <FormControl>
                <Checkbox v-model:checked="hasExpiryDate" />
              </FormControl>
              <FormLabel class="text-gray-700">Dato</FormLabel>
            </FormItem>
          </FormField>

          <FormField v-if="hasExpiryDate" name="expiryDate">
            <FormItem>
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
       <!-- </Form> -->
      </CardContent>
    </Card>
  </div>
</template>
