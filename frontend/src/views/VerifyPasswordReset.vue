<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useForm } from 'vee-validate'
import { toTypedSchema } from '@vee-validate/zod'
import * as z from 'zod'
import { useRoute, useRouter } from 'vue-router'
import { KeyRound } from 'lucide-vue-next'
import axios from 'axios'

import { Button } from '@/components/ui/button'
import { FormField } from '@/components/ui/form'
import { Input } from '@/components/ui/input'
import PasswordInput from '@/components/auth/PasswordInput.vue'

// Schema for the reset password form with password requirements
const resetPasswordSchema = z
  .object({
    password: z
      .string()
      .min(8, 'Passord må være minst 8 tegn')
      .max(50, 'Passord kan være maks 50 tegn')
      .regex(/[A-Z]/, 'Må inneholde minst én stor bokstav')
      .regex(/[a-z]/, 'Må inneholde minst én liten bokstav')
      .regex(/[0-9]/, 'Må inneholde minst ett tall')
      .regex(/[^A-Za-z0-9]/, 'Må inneholde minst ett spesialtegn'),
    confirmPassword: z.string(),
  })
  .refine((data) => data.password === data.confirmPassword, {
    message: "Passordene stemmer ikke overens",
    path: ['confirmPassword'],
  })

// Set up form validation
const { handleSubmit, meta } = useForm({
  validationSchema: toTypedSchema(resetPasswordSchema),
})

// Track form states
const isLoading = ref(false)
const isSuccessful = ref(false)
const isTokenValid = ref(true)

// Get token from URL
const route = useRoute()
const router = useRouter()
const token = ref('')

onMounted(() => {
  token.value = route.query.token as string
  if (!token.value) {
    isTokenValid.value = false
  }
})

const onSubmit = handleSubmit(async (values) => {
  isLoading.value = true

  try {
    await axios.post('http://localhost:8080/api/auth/complete-password-reset', {
      token: token.value,
      newPassword: values.password
    })

    isSuccessful.value = true
  } catch (error) {
    console.error('Failed to reset password:', error)
  } finally {
    isLoading.value = false
  }
})

const goToLogin = () => {
  router.push('/logg-inn')
}
</script>
<template>
  <div class="min-h-screen flex items-center justify-center bg-white">
    <form
      @submit="onSubmit"
      class="w-full max-w-sm p-8 border border-gray-200 rounded-xl shadow-sm bg-white space-y-5"
    >
      <div class="text-center">
        <div class="mx-auto bg-blue-100 p-2 rounded-full w-12 h-12 flex items-center justify-center mb-4">
          <KeyRound class="h-6 w-6 text-blue-600" />
        </div>
        <h1 class="text-3xl font-bold">Tilbakestill Passord</h1>
        <p class="text-sm text-gray-500 mt-2" v-if="!isSuccessful">
          Opprett et nytt passord for kontoen din
        </p>
      </div>

      <!-- Success message -->
      <div v-if="isSuccessful" class="text-center">
        <p class="text-green-600 font-medium">Passordet ditt har blitt tilbakestilt.</p>
        <p class="text-gray-600 mt-2">Du kan nå logge inn med ditt nye passord.</p>

        <Button class="w-full mt-4 bg-blue-600 text-white hover:bg-blue-700" @click="goToLogin">
          Gå til innlogging
        </Button>
      </div>

      <!-- Form fields -->
      <div v-else class="space-y-5">
        <!-- Password field using the component -->
        <FormField v-slot="{ componentField }" name="password">
          <PasswordInput
            name="password"
            label="Nytt Passord"
            placeholder="********"
            :componentField="componentField"
            :showToggle="true"
            :showIcon="true"
          />
        </FormField>

        <!-- Confirm Password using the component -->
        <FormField v-slot="{ componentField }" name="confirmPassword">
          <PasswordInput
            name="confirmPassword"
            label="Bekreft Nytt Passord"
            placeholder="********"
            :componentField="componentField"
            :showToggle="true"
            :showIcon="true"
          />
        </FormField>

        <!-- Password requirements info -->
        <div class="text-xs text-gray-500 space-y-1">
          <p class="font-medium">Passordkrav:</p>
          <ul class="space-y-1">
            <li>• Minimum 8 tegn</li>
            <li>• Minst én stor bokstav</li>
            <li>• Minst én liten bokstav</li>
            <li>• Minst ett tall</li>
            <li>• Minst ett spesialtegn</li>
          </ul>
        </div>

        <Button
          type="submit"
          class="w-full bg-blue-600 hover:bg-blue-700 disabled:bg-gray-400 disabled:cursor-not-allowed text-white py-2 rounded-md text-sm font-medium"
          :disabled="!meta.valid || isLoading"
        >
          <span v-if="!isLoading">Tilbakestill Passord</span>
          <span v-else class="flex items-center justify-center">
            <svg class="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
            Behandler...
          </span>
        </Button>

        <!-- Login link -->
        <div class="text-sm text-center">
          <span class="text-gray-600">Husker du passordet ditt?</span>
          <a href="/logg-inn" class="ml-1 text-blue-600 hover:underline">Tilbake til innlogging</a>
        </div>
      </div>
    </form>
  </div>
</template>

