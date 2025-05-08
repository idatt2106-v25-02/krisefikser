<script setup lang="ts">
import { ref } from 'vue'
import { useForm } from 'vee-validate'
import { toTypedSchema } from '@vee-validate/zod'
import * as z from 'zod'
import { useRouter } from 'vue-router'
import { KeyRound } from 'lucide-vue-next'
import { useAuthStore } from '@/stores/auth/useAuthStore'

import { Button } from '@/components/ui/button'
import { FormField } from '@/components/ui/form'
import PasswordInput from '@/components/auth/PasswordInput.vue'

// Schema for the update password form with password requirements
const updatePasswordSchema = z
  .object({
    oldPassword: z.string().min(1, 'Nåværende passord er påkrevd'),
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
  validationSchema: toTypedSchema(updatePasswordSchema),
})

// Track form states
const isLoading = ref(false)
const isSuccessful = ref(false)
const errorMessage = ref('')

const router = useRouter()
const authStore = useAuthStore()

const onSubmit = handleSubmit(async (values) => {
  isLoading.value = true
  errorMessage.value = ''

  try {
    await authStore.updatePassword(values.oldPassword, values.password)
    isSuccessful.value = true
  } catch (error: any) {
    errorMessage.value = error.response?.data?.message || 'En feil oppstod ved oppdatering av passord'
  } finally {
    isLoading.value = false
  }
})

const goToProfile = () => {
  router.push('/profil')
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
        <h1 class="text-3xl font-bold">Oppdater Passord</h1>
        <p class="text-sm text-gray-500 mt-2" v-if="!isSuccessful">
          Oppdater passordet ditt
        </p>
      </div>

      <!-- Success message -->
      <div v-if="isSuccessful" class="text-center">
        <p class="text-green-600 font-medium">Passordet ditt har blitt oppdatert.</p>
        <p class="text-gray-600 mt-2">Du kan nå bruke ditt nye passord ved neste innlogging.</p>

        <Button class="w-full mt-4 bg-blue-600 text-white hover:bg-blue-700" @click="goToProfile">
          Gå til profil
        </Button>
      </div>

      <!-- Error message -->
      <div v-if="errorMessage" class="text-red-500 text-sm text-center">
        {{ errorMessage }}
      </div>

      <!-- Form fields -->
      <div v-else class="space-y-5">
        <!-- Current Password field -->
        <FormField v-slot="{ componentField }" name="oldPassword">
          <PasswordInput
            name="oldPassword"
            label="Nåværende Passord"
            placeholder="********"
            :componentField="componentField"
            :showToggle="true"
            :showIcon="true"
          />
        </FormField>

        <!-- New Password field -->
        <FormField v-slot="{ componentField }" name="password">
          <PasswordInput
            name="password"
            label="Nytt Passord"
            placeholder="********"
            :componentField="componentField"
            :showToggle="true"
            :showIcon="true"
            :showComplexityRequirements="true"
          />
        </FormField>

        <!-- Confirm Password field -->
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

        <Button
          type="submit"
          class="w-full bg-blue-600 hover:bg-blue-700 disabled:bg-gray-400 disabled:cursor-not-allowed text-white py-2 rounded-md text-sm font-medium"
          :disabled="!meta.valid || isLoading"
        >
          <span v-if="!isLoading">Oppdater Passord</span>
          <span v-else class="flex items-center justify-center">
            <svg class="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
            Behandler...
          </span>
        </Button>

        <!-- Back to profile link -->
        <div class="text-sm text-center">
          <span class="text-gray-600">Vil du ikke endre passord?</span>
          <a href="/profil" class="ml-1 text-blue-600 hover:underline">Tilbake til profil</a>
        </div>
      </div>
    </form>
  </div>
</template>
