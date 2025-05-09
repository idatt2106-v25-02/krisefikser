<script setup lang="ts">
import { ref } from 'vue'
import { useForm } from 'vee-validate'
import { toTypedSchema } from '@vee-validate/zod'
import * as z from 'zod'
import { useRouter } from 'vue-router'
import { KeyRound } from 'lucide-vue-next'
import { useUpdatePassword } from '@/api/generated/authentication/authentication'
import { useAuthStore } from '@/stores/auth/useAuthStore'
import { Button } from '@/components/ui/button'
import { FormField } from '@/components/ui/form'
import PasswordInput from '@/components/auth/PasswordInput.vue'
import { createPasswordConfirmationSchema } from '@/utils/validation/passwordSchemas'
import { showSuccessToast, showErrorToast } from '@/utils/error/errorHandling'

// Schema for the update password form with password requirements
const passwordFields = createPasswordConfirmationSchema()
const updatePasswordSchema = z.object({
  oldPassword: z.string().min(1, 'Nåværende passord er påkrevd'),
  password: passwordFields.password,
  confirmPassword: passwordFields.confirmPassword,
}).refine((data) => data.password === data.confirmPassword, {
  message: "Passordene stemmer ikke overens",
  path: ['confirmPassword'],
})

type UpdatePasswordFormValues = z.infer<typeof updatePasswordSchema>

// Set up form validation
const { handleSubmit, meta } = useForm<UpdatePasswordFormValues>({
  validationSchema: toTypedSchema(updatePasswordSchema),
})

// Track form states
const isLoading = ref(false)
const isSuccessful = ref(false)
const errorMessage = ref<string>('')

const router = useRouter()
const authStore = useAuthStore()

// Get the update password mutation
const { mutateAsync: updatePasswordMutation } = useUpdatePassword()

const onSubmit = handleSubmit(async (values: UpdatePasswordFormValues) => {
  isLoading.value = true
  errorMessage.value = ''

  try {
    await updatePasswordMutation({
      data: {
        oldPassword: values.oldPassword,
        password: values.password,
      },
    })
    isSuccessful.value = true
    showSuccessToast('Passord oppdatert', 'Du vil bli logget ut for å sikre din konto.')
    authStore.logout()
  } catch (error: unknown) {
    const message = showErrorToast('Feil ved oppdatering av passord', error)
    errorMessage.value = message
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
        <h1 class="text-3xl font-bold">Oppdater Passord</h1>
        <p class="text-sm text-gray-500 mt-2" v-if="!isSuccessful">
          Oppdater passordet ditt
        </p>
      </div>

      <!-- Success message -->
      <div v-if="isSuccessful" class="text-center">
        <div class="mx-auto bg-green-100 p-2 rounded-full w-12 h-12 flex items-center justify-center mb-4">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-green-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
          </svg>
        </div>
        <h2 class="text-xl font-semibold text-gray-900 mb-2">Passord Oppdatert!</h2>
        <p class="text-gray-600 mb-4">Ditt passord har blitt oppdatert. For å sikre din konto, må du logge inn på nytt med ditt nye passord.</p>

        <Button class="w-full bg-blue-600 text-white hover:bg-blue-700" @click="goToLogin">
          Gå til innlogging
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
