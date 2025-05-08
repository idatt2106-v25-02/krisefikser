<script lang="ts" setup>
import { computed, ref } from 'vue'
import { useForm } from 'vee-validate'
import * as z from 'zod'
import { toTypedSchema } from '@vee-validate/zod'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'
import { FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form'
import { Building, Home, Mail, MapPin } from 'lucide-vue-next'

// Props
const props = defineProps({
  includeHouseholdName: {
    type: Boolean,
    default: false,
  },
  colorTheme: {
    type: String,
    default: 'blue',
    validator: (value: string) => ['blue', 'yellow', 'green'].includes(value),
  },
  title: {
    type: String,
    default: 'Adresse',
  },
  description: {
    type: String,
    default: 'Fyll inn adresseinformasjon',
  },
  submitButtonText: {
    type: String,
    default: 'Lagre',
  },
  initialValues: {
    type: Object,
    default: () => ({
      name: '',
      address: '',
      postalCode: '',
      city: '',
    }),
  },
})

// Emits
const emit = defineEmits(['submit', 'cancel'])

// Create the appropriate schema based on whether household name is included
const formSchema = computed(() => {
  const baseSchema = {
    address: z
      .string()
      .min(5, 'Adressen må være minst 5 tegn')
      .max(100, 'Adressen må være mindre enn 100 tegn'),
    postalCode: z
      .string()
      .min(4, 'Postnummer må være minst 4 tegn')
      .max(5, 'Postnummer kan ikke være mer enn 5 tegn')
      .regex(/^\d+$/, 'Postnummer må være et tall'),
    city: z
      .string()
      .min(2, 'By/sted må være minst 2 tegn')
      .max(50, 'By/sted må være mindre enn 50 tegn'),
    country: z
      .string()
      .min(2, 'Land må være minst 2 tegn')
      .max(50, 'Land må være mindre enn 50 tegn')
      .default('Norge')
      .optional(),
  }

  if (props.includeHouseholdName) {
    return toTypedSchema(
      z.object({
        name: z
          .string()
          .min(2, 'Husstandsnavnet må være minst 2 tegn')
          .max(50, 'Husstandsnavnet må være mindre enn 50 tegn'),
        ...baseSchema,
      }),
    )
  }

  return toTypedSchema(z.object(baseSchema))
})

// Initialize form
const { handleSubmit, meta } = useForm({
  validationSchema: formSchema,
  initialValues: props.initialValues,
  validateOnMount: false,
})

// Input restrictions
const onPostalCodeInput = (e: Event) => {
  const t = e.target as HTMLInputElement
  t.value = t.value.replace(/\D/g, '')
}
const onPostalCodeKeyPressStrict = (e: KeyboardEvent) => {
  if (!/\d/.test(e.key)) e.preventDefault()
}
const onOnlyLetters = (e: KeyboardEvent) => {
  if (!/^[A-Za-zÆØÅæøå\s-]$/.test(e.key)) e.preventDefault()
}

// Geocode address
const geocodeAddress = async (
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  values: any,
): Promise<{ latitude: number; longitude: number } | null> => {
  try {
    const fullAddress = `${values.address}, ${values.postalCode} ${values.city}`
    const encodedQuery = encodeURIComponent(fullAddress)
    const url = `https://nominatim.openstreetmap.org/search?format=json&q=${encodedQuery}&limit=1`

    // Make API request with appropriate headers
    const response = await fetch(url, {
      headers: {
        Accept: 'application/json',
        'User-Agent': 'KrisefikserApp/1.0',
      },
    })

    if (!response.ok) {
      throw new Error('Network response was not ok')
    }

    const data = await response.json()

    // Return coordinates if results found
    if (data && data.length > 0) {
      return {
        latitude: parseFloat(data[0].lat),
        longitude: parseFloat(data[0].lon),
      }
    }

    return null
  } catch (error) {
    console.error('Error geocoding address:', error)
    return null
  }
}

// Submission
const loading = ref(false)
const onSubmit = handleSubmit(async (values) => {
  loading.value = true

  try {
    // Try to geocode the address
    const location = await geocodeAddress(values)

    if (location) {
      // Continue with form submission
      setTimeout(() => {
        loading.value = false
        emit('submit', { ...values, ...location })
      }, 500)
    } else {
      // Show alert if address not found
      alert(
        'Kunne ikke finne koordinater for denne adressen. Vennligst sjekk adressen og prøv igjen.',
      )
      loading.value = false
    }
  } catch (error) {
    console.error('Error during geocoding:', error)
    alert('En feil oppstod under geokoding av adressen. Vennligst prøv igjen senere.')
    loading.value = false
  }
})

const buttonColorClass = computed(() => {
  const colors: Record<string, string> = {
    blue: 'bg-blue-600 hover:bg-blue-700',
    yellow: 'bg-yellow-600 hover:bg-yellow-700',
    green: 'bg-green-600 hover:bg-green-700',
  }
  return colors[props.colorTheme]
})
</script>

<template>
  <div class="address-form-container">
    <Card class="w-full max-w-lg mx-auto shadow-lg">
      <CardHeader>
        <CardTitle class="text-2xl font-bold text-center">{{ title }}</CardTitle>
        <CardDescription class="text-center">{{ description }}</CardDescription>
      </CardHeader>

      <CardContent>
        <form class="space-y-6" novalidate @submit.prevent="onSubmit">
          <!-- Household Name (optional) -->
          <FormField v-if="includeHouseholdName" v-slot="{ field, meta }" name="name">
            <FormItem>
              <FormLabel>Husstandsnavn</FormLabel>
              <FormControl>
                <div class="relative">
                  <div class="absolute inset-y-0 left-0 flex items-center pl-3 pointer-events-none">
                    <Home class="h-5 w-5 text-gray-400" />
                  </div>
                  <Input
                    id="name"
                    class="pl-10"
                    placeholder="Skriv inn husstandsnavn"
                    v-bind="field"
                  />
                </div>
              </FormControl>
              <FormMessage v-slot="{ message }">
                <p v-if="meta.touched && field.value">{{ message }}</p>
              </FormMessage>
            </FormItem>
          </FormField>

          <FormField v-slot="{ field, meta }" name="address">
            <FormItem>
              <FormLabel>Adresse</FormLabel>
              <FormControl>
                <div class="relative">
                  <div class="absolute inset-y-0 left-0 flex items-center pl-3 pointer-events-none">
                    <MapPin class="h-5 w-5 text-gray-400" />
                  </div>
                  <Input
                    id="address"
                    class="pl-10"
                    placeholder="Skriv inn adresse"
                    v-bind="field"
                  />
                </div>
              </FormControl>
              <FormMessage v-slot="{ message }">
                <p v-if="meta.touched && field.value">{{ message }}</p>
              </FormMessage>
            </FormItem>
          </FormField>

          <div class="grid grid-cols-2 gap-4">
            <FormField v-slot="{ field, meta }" name="postalCode">
              <FormItem>
                <FormLabel>Postnummer</FormLabel>
                <FormControl>
                  <div class="relative">
                    <div
                      class="absolute inset-y-0 left-0 flex items-center pl-3 pointer-events-none"
                    >
                      <Mail class="h-5 w-5 text-gray-400" />
                    </div>
                    <Input
                      id="postalCode"
                      class="pl-10"
                      placeholder="0000"
                      v-bind="field"
                      @input="onPostalCodeInput"
                      @keypress="onPostalCodeKeyPressStrict"
                    />
                  </div>
                </FormControl>
                <FormMessage v-slot="{ message }">
                  <p v-if="meta.touched && field.value">{{ message }}</p>
                </FormMessage>
              </FormItem>
            </FormField>

            <FormField v-slot="{ field, meta }" name="city">
              <FormItem>
                <FormLabel>By/sted</FormLabel>
                <FormControl>
                  <div class="relative">
                    <div
                      class="absolute inset-y-0 left-0 flex items-center pl-3 pointer-events-none"
                    >
                      <Building class="h-5 w-5 text-gray-400" />
                    </div>
                    <Input
                      id="city"
                      class="pl-10"
                      placeholder="Skriv inn by/sted"
                      v-bind="field"
                      @keypress="onOnlyLetters"
                    />
                  </div>
                </FormControl>
                <FormMessage v-slot="{ message }">
                  <p v-if="meta.touched && field.value">{{ message }}</p>
                </FormMessage>
              </FormItem>
            </FormField>
          </div>
        </form>
      </CardContent>

      <CardFooter class="flex justify-end space-x-3">
        <Button type="button" variant="outline" @click="emit('cancel')">Avbryt</Button>
        <Button :class="buttonColorClass" :disabled="!meta.valid" type="submit" @click="onSubmit">
          <span v-if="loading" class="mr-2">
            <svg
              class="animate-spin h-4 w-4 text-white"
              fill="none"
              viewBox="0 0 24 24"
              xmlns="http://www.w3.org/2000/svg"
            >
              <circle
                class="opacity-25"
                cx="12"
                cy="12"
                r="10"
                stroke="currentColor"
                stroke-width="4"
              />
              <path
                class="opacity-75"
                d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                fill="currentColor"
              />
            </svg>
          </span>
          {{ loading ? 'Lagrer...' : submitButtonText }}
        </Button>
      </CardFooter>
    </Card>
  </div>
</template>

<style scoped>
.address-form-container {
  width: 100%;
}
</style>
