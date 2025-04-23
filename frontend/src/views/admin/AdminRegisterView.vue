<script setup lang="ts">
import { ref } from 'vue'
import { useForm } from 'vee-validate'
import { toTypedSchema } from '@vee-validate/zod'
import * as z from 'zod'

// UI components
import { Button } from '@/components/ui/button'
import { FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form'
import { Input } from '@/components/ui/input'

// Simulated username value – can be replaced with route param or props
const username = 'admin_username_example'

// === Schema definition for password and confirmation ===
const rawSchema = z
  .object({
    password: z
      .string()
      .min(8, 'Password must be at least 8 characters')
      .max(50, 'Password must be at most 50 characters')
      .regex(/[A-Z]/, 'Must include an uppercase letter')
      .regex(/[a-z]/, 'Must include a lowercase letter')
      .regex(/[0-9]/, 'Must include a number')
      .regex(/[^A-Za-z0-9]/, 'Must include a special character'),
    confirmPassword: z.string(),
  })
  .refine((data) => data.password === data.confirmPassword, {
    message: "Passwords don't match",
    path: ['confirmPassword'],
  })

// === Form setup with validation ===
const { handleSubmit, meta } = useForm({
  validationSchema: toTypedSchema(rawSchema),
})

// === Form submission logic ===
const onSubmit = handleSubmit((values) => {
  console.log('Admin registration submitted:', values)
})

// === UI state for password visibility toggles ===
const showPassword = ref(false)
function toggleShowPassword() {
  showPassword.value = !showPassword.value
}

const showConfirmPassword = ref(false)
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
      <h1 class="text-3xl font-bold text-center">Admin Registration</h1>

      <!-- Display read-only username -->
      <div class="space-y-1">
        <label class="block text-sm font-medium text-gray-700">Username</label>
        <input
          type="text"
          :value="username"
          readonly
          class="w-full px-3 py-2 bg-gray-100 border border-gray-300 rounded-md shadow-sm"
        />
      </div>

      <!-- Password Field -->
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

      <!-- Confirm Password Field -->
      <FormField v-slot="{ componentField }" name="confirmPassword">
        <FormItem>
          <FormLabel class="block text-sm font-medium text-gray-700 mb-1">Confirm Password</FormLabel>
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

      <!-- Submit Button: disabled unless form is valid and touched -->
      <Button
        type="submit"
        :disabled="!meta.valid || !meta.dirty"
        class="w-full bg-blue-600 hover:bg-blue-700 disabled:bg-gray-400 disabled:cursor-not-allowed text-white py-2 rounded-md text-sm font-medium"
      >
        Register
      </Button>
    </form>
  </div>
</template>
