<script setup lang="ts">
import { ref, computed } from 'vue'
import { useForm } from 'vee-validate'
import { toTypedSchema } from '@vee-validate/zod'
import * as z from 'zod'
import VueTurnstile from 'vue-turnstile'

import { Button } from '@/components/ui/button'
import { FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form'
import { Input } from '@/components/ui/input'

// Schema for the registeration form
const rawSchema = z
  .object({
    firstName: z.string().min(2, 'First name too short'),
    lastName: z.string().min(2, 'Last name too short'),
    email: z.string().email('Invalid email').min(5, 'Email too short'),
    householdCode: z.string().refine(
      (val) => val === '' || (val.length === 5 && /^[a-zA-Z]+$/.test(val)),
      {
        message: 'Household code must be exactly 5 letters (no numbers)',
      }
    ).optional(),
    password: z
      .string()
      .min(8, 'Password must be at least 8 characters')
      .max(50, 'Password must be at most 50 characters')
      .regex(/[A-Z]/, 'Must include an uppercase letter')
      .regex(/[a-z]/, 'Must include a lowercase letter')
      .regex(/[0-9]/, 'Must include a number')
      .regex(/[^A-Za-z0-9]/, 'Must include a special character'),
    confirmPassword: z.string(),
    turnstileToken: z.string().min(1, 'Please complete the CAPTCHA verification')
  })
  .refine((data) => data.password === data.confirmPassword, {
    message: "Passwords don't match",
    path: ['confirmPassword'], // This will attach the error to the confirmPassword field
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
    // Send the form data along with the Turnstile token to the server
    const response = await fetch('http://localhost:3000/verify-turnstile', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ token: turnstileToken.value }),
    })

    const data = await response.json()

    if (data.success) {
      // If Turnstile verification succeeds, proceed with registration
      console.log('Registration form submitted successfully', values)
      // Here you would typically call your registration API
    } else {
      console.error('Turnstile verification failed', data.error)
      // Reset the Turnstile widget
      turnstileToken.value = ''
      setFieldValue('turnstileToken', '')
    }
  } catch (error) {
    console.error('Error during form submission:', error)
  }
})

// Stores the show password state
const showPassword = ref(false)

// Toggle the visibility of the password
function toggleShowPassword() {
  showPassword.value = !showPassword.value
}

// Stores the show password state for confirmed password
const showConfirmPassword = ref(false)

// Toggle the visibility of the confirm password field
function toggleShowConfirmPassword() {
  showConfirmPassword.value = !showConfirmPassword.value
}
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-white">
    <form
      @submit="onSubmit"
      class="w-full max-w-sm p-8 border border-gray-200 rounded-xl shadow-sm bg-white space-y-5"
    >
      <h1 class="text-3xl font-bold text-center">Register</h1>

      <!-- First Name -->
      <FormField v-slot="{ componentField }" name="firstName">
        <FormItem>
          <FormLabel class="block text-sm font-medium text-gray-700 mb-1">First Name</FormLabel>
          <FormControl>
            <Input
              type="text"
              placeholder="John"
              class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
              v-bind="componentField"
            />
          </FormControl>
          <FormMessage class="text-sm text-red-500" />
        </FormItem>
      </FormField>

      <!-- Last Name -->
      <FormField v-slot="{ componentField }" name="lastName">
        <FormItem>
          <FormLabel class="block text-sm font-medium text-gray-700 mb-1">Last Name</FormLabel>
          <FormControl>
            <Input
              type="text"
              placeholder="Doe"
              class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
              v-bind="componentField"
            />
          </FormControl>
          <FormMessage class="text-sm text-red-500" />
        </FormItem>
      </FormField>

      <!-- Email -->
      <FormField v-slot="{ componentField }" name="email">
        <FormItem>
          <FormLabel class="block text-sm font-medium text-gray-700 mb-1"> Email </FormLabel>
          <FormControl>
            <Input
              type="email"
              placeholder="name@example.org"
              class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
              v-bind="componentField"
            />
          </FormControl>
          <FormMessage class="text-sm text-red-500" />
        </FormItem>
      </FormField>

      <!-- Household Code field -->
      <FormField v-slot="{ componentField }" name="householdCode">
        <FormItem>
          <FormLabel class="block text-sm font-medium text-gray-700 mb-1">Household Code</FormLabel>
          <FormControl>
            <Input
              type="text"
              placeholder="ABCDE"
              maxlength="5"
              class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
              v-bind="componentField"
            />
          </FormControl>
          <FormMessage class="text-sm text-red-500" />
        </FormItem>
      </FormField>

      <!-- Password field -->
      <FormField v-slot="{ componentField }" name="password">
        <FormItem>
          <FormLabel class="block text-sm font-medium text-gray-700 mb-1">Password</FormLabel>
          <FormControl>
            <div class="relative">
              <Input
                :type="showPassword ? 'text' : 'password'"
                placeholder="********"
                class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 pr-10"
                v-bind="componentField"
              />
              <button
                type="button"
                @click="toggleShowPassword"
                class="absolute inset-y-0 right-2 flex items-center text-sm text-gray-600 focus:outline-none"
                tabindex="-1"
              >
                {{ showPassword ? 'Hide' : 'Show' }}
              </button>
            </div>
          </FormControl>
          <FormMessage class="text-sm text-red-500" />
        </FormItem>
      </FormField>

      <!-- Confirm Password -->
      <FormField v-slot="{ componentField }" name="confirmPassword">
        <FormItem>
          <FormLabel class="block text-sm font-medium text-gray-700 mb-1"
            >Confirm Password</FormLabel
          >
          <FormControl>
            <div class="relative">
              <Input
                :type="showConfirmPassword ? 'text' : 'password'"
                placeholder="********"
                class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 pr-10"
                v-bind="componentField"
              />
              <button
                type="button"
                @click="toggleShowConfirmPassword"
                class="absolute inset-y-0 right-2 flex items-center text-sm text-gray-600 focus:outline-none"
                tabindex="-1"
              >
                {{ showConfirmPassword ? 'Hide' : 'Show' }}
              </button>
            </div>
          </FormControl>
          <FormMessage class="text-sm text-red-500" />
        </FormItem>
      </FormField>

      <!-- Cloudflare Turnstile -->
      <FormField v-slot="{ componentField }" name="turnstileToken">
        <FormItem>
          <FormLabel class="block text-sm font-medium text-gray-700 mb-1">Verify you're human</FormLabel>
          <FormControl>
            <div class="flex justify-center">
              <VueTurnstile
                site-key="1x00000000000000000000AA"
                v-model="turnstileToken"
                theme="light"
              />
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
        Register
      </Button>

      <!-- Conditional CTAs below -->
      <div class="text-sm text-center space-y-2">
        <div>
          <span class="text-gray-600">Already have an account?</span>
          <a href="/login" class="ml-1 text-blue-600 hover:underline">Login</a>
        </div>
      </div>
    </form>
  </div>
</template>
