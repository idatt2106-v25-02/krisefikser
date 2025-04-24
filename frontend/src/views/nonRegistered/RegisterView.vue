<script setup lang="ts">
import { ref } from 'vue'
import { useForm } from 'vee-validate'
import { toTypedSchema } from '@vee-validate/zod'
import * as z from 'zod'
import VueTurnstile from 'vue-turnstile'
import axios from 'axios'
import { User, Mail, Home, Shield } from 'lucide-vue-next'

import { Button } from '@/components/ui/button'
import { FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form'
import { Input } from '@/components/ui/input'
import PasswordInput from '@/components/auth/PasswordInput.vue'

// Schema for the registration form
const rawSchema = z
  .object({
    firstName: z.string().min(2, 'Fornavn for kort'),
    lastName: z.string().min(2, 'Etternavn for kort'),
    email: z.string().email('Ugyldig e-post').min(5, 'E-post for kort'),
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
    turnstileToken: z.string().min(1, 'Vennligst fullfør CAPTCHA-verifiseringen')
  })
  .refine((data) => data.password === data.confirmPassword, {
    message: "Passordene stemmer ikke overens",
    path: ['confirmPassword'],
  })

// Set up consts for submit button deactivation
const { handleSubmit, meta, setFieldValue } = useForm({
  validationSchema: toTypedSchema(rawSchema),
})

// Store the Turnstile token
const turnstileToken = ref('')

// Handle Turnstile verification success
const onTurnstileSuccess = (token: string) => {
  turnstileToken.value = token
  setFieldValue('turnstileToken', token)
}

// Handle Turnstile verification error
const onTurnstileError = () => {
  turnstileToken.value = ''
  setFieldValue('turnstileToken', '')
}

// Handle Turnstile verification expiry
const onTurnstileExpire = () => {
  turnstileToken.value = ''
  setFieldValue('turnstileToken', '')
}

const onSubmit = handleSubmit(async (values) => {
  try {
    // TODO: Change website link to the actual server
    // Send the form data along with the Turnstile token to the server
    const response = await axios.post('http://localhost:3000/verify-turnstile', {
      token: turnstileToken.value
    })

    const data = response.data

    if (data.success) {
      // If Turnstile verification succeeds, proceed with registration
      console.log('Registreringsskjema sendt inn', values)
      // Here you would typically call your registration API
    } else {
      console.error('Turnstile-verifisering mislyktes', data.error)
      // Reset the Turnstile widget
      turnstileToken.value = ''
      setFieldValue('turnstileToken', '')
    }
  } catch (error) {
    console.error('Feil under innsending av skjema:', error)
  }
})
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-white py-12">
    <form
      @submit="onSubmit"
      class="w-full max-w-sm p-8 border border-gray-200 rounded-xl shadow-sm bg-white space-y-5"
    >
      <h1 class="text-3xl font-bold text-center">Registrering</h1>

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

      <!-- Household Code field -->
      <FormField v-slot="{ componentField }" name="householdCode">
        <FormItem>
          <FormLabel class="block text-sm font-medium text-gray-700 mb-1">Husholdningskode</FormLabel>
          <FormControl>
            <div class="relative">
              <Home class="absolute left-2 top-1/2 transform -translate-y-1/2 text-gray-500 h-4 w-4" />
              <Input
                type="text"
                placeholder="ABCDE"
                maxlength="5"
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
      <FormField v-slot="{ componentField }" name="turnstileToken">
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
      </FormField>

      <!-- Submit button -->
      <Button
        type="submit"
        :disabled="!meta.valid"
        class="w-full bg-blue-600 hover:bg-blue-700 disabled:bg-gray-400 disabled:cursor-not-allowed text-white py-2 rounded-md text-sm font-medium"
      >
        Registrer
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
