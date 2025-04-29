<script setup lang="ts">
import { ref } from 'vue'
import { useForm } from 'vee-validate'
import { toTypedSchema } from '@vee-validate/zod'
import * as z from 'zod'
import { User, Mail, Home, Lock, Eye, EyeOff } from 'lucide-vue-next'
import { useAuthStore } from '@/stores/useAuthStore'
import { useToast } from '@/components/ui/toast/use-toast'

import { Button } from '@/components/ui/button'
import { FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form'
import { Input } from '@/components/ui/input'
import PasswordInput from '@/components/auth/PasswordInput.vue'

// Schema for the registration form
const rawSchema = z
  .object({
    firstName: z.string().min(2, 'Fornavn må være minst 2 tegn'),
    lastName: z.string().min(2, 'Etternavn må være minst 2 tegn'),
    email: z.string().email('Ugyldig e-post').min(5, 'E-post må være minst 5 tegn'),
    householdCode: z.string().refine(
      (val) => val === '' || (val.length === 5 && /^[a-zA-Z]+$/.test(val)),
      {
        message: 'Husholdningskode må være nøyaktig 5 bokstaver (ingen tall)',
      }
    ).optional(),
    password: z
      .string()
      .min(8, 'Passord må være minst 8 tegn')
      .max(50, 'Passord kan være maks 50 tegn')
      .regex(/[A-Z]/, 'Må inneholde minst én stor bokstav')
      .regex(/[a-z]/, 'Må inneholde minst én liten bokstav')
      .regex(/[0-9]/, 'Må inneholde minst ett tall')
      .regex(/[^A-Za-z0-9]/, 'Må inneholde minst ett spesialtegn'),
    confirmPassword: z.string(),
    //turnstileToken: z.string().min(1, 'Vennligst fullfør CAPTCHA-verifiseringen')
  })
  .refine((data) => data.password === data.confirmPassword, {
    message: "Passordene stemmer ikke overens",
    path: ['confirmPassword'],
  })

// Set up consts for submit button deactivation
const { handleSubmit, meta, setFieldValue } = useForm({
  validationSchema: toTypedSchema(rawSchema),
})

// Get auth store and toast
const authStore = useAuthStore()
const { toast } = useToast()

// Loading state
const isLoading = ref(false)

const onSubmit = handleSubmit(async (values) => {
  if (isLoading.value) return

  isLoading.value = true
  try {
    const { confirmPassword, ...registrationData } = values
    await authStore.register(registrationData)
    toast({
      title: 'Suksess',
      description: 'Kontoen din er opprettet og du er nå logget inn',
      variant: 'default',
    })
  } catch (error: any) {
    toast({
      title: 'Feil',
      description: error?.response?.data?.message || 'Kunne ikke registrere. Vennligst prøv igjen.',
      variant: 'destructive',
    })
  } finally {
    isLoading.value = false
  }
})

// Show/hide password
const showPassword = ref(false)

// Toggle the visibility of the password
function toggleShowPassword() {
  showPassword.value = !showPassword.value
}

// Show/hide confirm password
const showConfirmPassword = ref(false)

// Toggle the visibility of the confirm password field
function toggleShowConfirmPassword() {
  showConfirmPassword.value = !showConfirmPassword.value
}
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-white py-12">
    <form
      @submit="onSubmit"
      class="w-full max-w-sm p-8 border border-gray-200 rounded-xl shadow-sm bg-white space-y-5"
    >
      <h1 class="text-3xl font-bold text-center">Registrer deg</h1>

      <!-- First Name -->
      <FormField v-slot="{ componentField }" name="firstName">
        <FormItem>
          <FormLabel class="block text-sm font-medium text-gray-700 mb-1">Fornavn</FormLabel>
          <FormControl>
            <div class="relative">
              <User class="absolute left-2 top-1/2 transform -translate-y-1/2 text-gray-500 h-4 w-4" />
              <Input
                type="text"
                placeholder="Ola"
                class="w-full px-3 py-2 pl-8 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                v-bind="componentField"
              />
            </div>
          </FormControl>
          <FormMessage class="text-sm text-red-500" />
        </FormItem>
      </FormField>

      <!-- Last Name -->
      <FormField v-slot="{ componentField }" name="lastName">
        <FormItem>
          <FormLabel class="block text-sm font-medium text-gray-700 mb-1">Etternavn</FormLabel>
          <FormControl>
            <div class="relative">
              <User class="absolute left-2 top-1/2 transform -translate-y-1/2 text-gray-500 h-4 w-4" />
              <Input
                type="text"
                placeholder="Nordmann"
                class="w-full px-3 py-2 pl-8 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                v-bind="componentField"
              />
            </div>
          </FormControl>
          <FormMessage class="text-sm text-red-500" />
        </FormItem>
      </FormField>

      <!-- Email -->
      <FormField v-slot="{ componentField }" name="email">
        <FormItem>
          <FormLabel class="block text-sm font-medium text-gray-700 mb-1">E-post</FormLabel>
          <FormControl>
            <div class="relative">
              <Mail class="absolute left-2 top-1/2 transform -translate-y-1/2 text-gray-500 h-4 w-4" />
              <Input
                type="email"
                placeholder="navn@eksempel.no"
                class="w-full px-3 py-2 pl-8 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                v-bind="componentField"
              />
            </div>
          </FormControl>
          <FormMessage class="text-sm text-red-500" />
        </FormItem>
      </FormField>

      <!-- Password field using component -->
      <FormField v-slot="{ componentField }" name="password">
        <PasswordInput
          name="password"
          label="Passord"
          placeholder="********"
          :componentField="componentField"
          :showToggle="true"
          :showIcon="true"
        />
      </FormField>

      <!-- Confirm Password using component -->
      <FormField v-slot="{ componentField }" name="confirmPassword">
        <PasswordInput
          name="confirmPassword"
          label="Bekreft passord"
          placeholder="********"
          :componentField="componentField"
          :showToggle="true"
          :showIcon="true"
        />
      </FormField>

      <!-- Cloudflare Turnstile -->
      <!-- <FormField v-slot="{ componentField }" name="turnstileToken">
        <FormItem>
          <FormLabel class="block text-sm font-medium text-gray-700 mb-1">Bekreft at du er et menneske</FormLabel>
          <FormControl>
            <div class="flex justify-center">
              <div class="relative w-full flex justify-center">
                <Shield class="absolute left-2 top-0 text-gray-500 h-4 w-4" />
                <VueTurnstile
                  site-key="0x4AAAAAABSTiPNZwrBLQkgr"
                  v-model="turnstileToken"
                  theme="light"
                  @success="onTurnstileSuccess"
                  @error="onTurnstileError"
                  @expire="onTurnstileExpire"
                />
              </div>
            </div>
          </FormControl>
          <FormMessage class="text-sm text-red-500" />
        </FormItem>
      </FormField> -->

      <!-- Submit button -->
      <Button
        type="submit"
        :disabled="!meta.valid || isLoading"
        class="w-full bg-blue-600 hover:bg-blue-700 disabled:bg-gray-400 disabled:cursor-not-allowed text-white py-2 rounded-md text-sm font-medium"
      >
        <template v-if="isLoading">Oppretter konto...</template>
        <template v-else>Registrer deg</template>
      </Button>

      <!-- Conditional CTAs below -->
      <div class="text-sm text-center space-y-2">
        <div>
          <span class="text-gray-600">Har du allerede en konto?</span>
          <a href="/logg-inn" class="ml-1 text-blue-600 hover:underline">Logg inn</a>
        </div>
      </div>
    </form>
  </div>
</template>
