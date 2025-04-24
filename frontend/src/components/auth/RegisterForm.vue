<script setup lang="ts">
import { useForm } from 'vee-validate'
import { toTypedSchema } from '@vee-validate/zod'
import * as z from 'zod'

import { Button } from '@/components/ui/button'
import { FormField } from '@/components/ui/form'
import { Input } from '@/components/ui/input'
import PasswordInput from '@/components/auth/PasswordInput.vue'

// Schema for the registration form
const rawSchema = z
  .object({
    firstName: z.string().min(2, 'First name too short'),
    lastName: z.string().min(2, 'Last name too short'),
    email: z.string().email('Invalid email').min(5, 'Email too short'),
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
    path: ['confirmPassword'], // This will attach the error to the confirmPassword field
  })

// Set up consts for submit button deactivation
const { handleSubmit, meta } = useForm({
  validationSchema: toTypedSchema(rawSchema),
})

const onSubmit = handleSubmit((values) => {
  console.log(values)
})
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

      <!-- Password field using the component -->
      <FormField v-slot="{ componentField }" name="password">
        <PasswordInput
          name="password"
          label="Password"
          placeholder="********"
          :componentField="componentField"
          :showToggle="true"
        />
      </FormField>

      <!-- Confirm Password using the component -->
      <FormField v-slot="{ componentField }" name="confirmPassword">
        <PasswordInput
          name="confirmPassword"
          label="Confirm Password"
          placeholder="********"
          :componentField="componentField"
          :showToggle="true"
        />
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
