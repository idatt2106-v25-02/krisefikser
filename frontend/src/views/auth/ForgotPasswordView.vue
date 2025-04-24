<script setup lang="ts">
import { ref } from 'vue'
import { useForm } from 'vee-validate'
import { toTypedSchema } from '@vee-validate/zod'
import * as z from 'zod'
import { Mail, ArrowRight, CheckCircle } from 'lucide-vue-next'

import { Button } from '@/components/ui/button'
import { FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form'
import { Input } from '@/components/ui/input'
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/card'

// Schema for the forgot password form
const forgotPasswordSchema = z.object({
  email: z.string().email('Please enter a valid email address')
})

// Set up form validation
const { handleSubmit, meta } = useForm({
  validationSchema: toTypedSchema(forgotPasswordSchema),
})

// Track form submission state
const isSubmitted = ref(false)
const isLoading = ref(false)
const userEmail = ref('')

const onSubmit = handleSubmit((values) => {
  isLoading.value = true

  // Simulate API call
  setTimeout(() => {
    userEmail.value = values.email
    isSubmitted.value = true
    isLoading.value = false
  }, 1500)
})
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-white p-4">
    <Card class="w-full max-w-md border border-gray-200 shadow-sm">
      <CardHeader class="space-y-1">
        <CardTitle class="text-2xl font-bold text-center">Forgot Password</CardTitle>
        <CardDescription class="text-center text-gray-500" v-if="!isSubmitted">
          Enter your email address and we'll send you a link to reset your password.
        </CardDescription>
      </CardHeader>

      <CardContent>
        <!-- Success state -->
        <div v-if="isSubmitted" class="space-y-4">
          <Alert class="bg-green-50 border-green-200">
            <CheckCircle class="h-5 w-5 text-green-500" />
            <AlertDescription class="mt-2">
              We've sent a password reset link to <span class="font-medium">{{ userEmail }}</span>.
              Please check your email and follow the instructions.
            </AlertDescription>
          </Alert>

        <Button class="w-full mt-4 bg-blue-600 text-white hover:bg-blue-700" @click="isSubmitted = false">
            Try another email
          </Button>
        </div>

        <!-- Form state -->
        <form v-else @submit="onSubmit" class="space-y-4">
          <FormField v-slot="{ componentField }" name="email">
            <FormItem>
              <FormLabel class="block text-sm font-medium text-gray-700">Email address</FormLabel>
              <FormControl>
                <div class="relative">
                  <Mail class="absolute left-3 top-1/2 -translate-y-1/2 h-5 w-5 text-gray-400" />
                  <Input
                    type="email"
                    placeholder="name@example.com"
                    class="w-full pl-10 pr-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                    v-bind="componentField"
                  />
                </div>
              </FormControl>
              <FormMessage class="text-sm text-red-500" />
            </FormItem>
          </FormField>

          <Button
            type="submit"
            class="w-full bg-blue-600 hover:bg-blue-700 disabled:bg-gray-400 disabled:cursor-not-allowed text-white py-2 rounded-md text-sm font-medium"
            :disabled="!meta.valid || isLoading"
          >
            <span v-if="!isLoading">Reset Password</span>
            <span v-else class="flex items-center justify-center">
              <svg class="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
              Processing...
            </span>
            <ArrowRight class="ml-2 h-4 w-4" v-if="!isLoading" />
          </Button>
        </form>
      </CardContent>

      <CardFooter class="flex justify-center">
        <div class="text-sm text-center">
          <span class="text-gray-600">Remember your password?</span>
          <a href="/logg-inn" class="ml-1 text-blue-600 hover:underline">Back to login</a>
        </div>
      </CardFooter>
    </Card>
  </div>
</template>
