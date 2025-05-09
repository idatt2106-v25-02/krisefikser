<script setup lang="ts">
import { computed, ref } from 'vue'
import { useForm } from 'vee-validate'
import { toTypedSchema } from '@vee-validate/zod'
import * as z from 'zod'
import { useAuthStore } from '@/stores/auth/useAuthStore.ts'
import { Mail, AlertCircle } from 'lucide-vue-next'
import { useRoute, useRouter } from 'vue-router'
import { toast } from 'vue-sonner'

// UI components
import { Button } from '@/components/ui/button'
import { FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form'
import { Input } from '@/components/ui/input'
import PasswordInput from '@/components/auth/PasswordInput.vue'
import { Alert, AlertDescription } from '@/components/ui/alert'

// === Store logic ===
const authStore = useAuthStore()
const route = useRoute()
const router = useRouter()

// Get redirect path from route query if available
const redirectPath = computed(() => (route.query.redirect as string) || '/dashboard')

// === Schema logic ===
const formSchema = computed(() =>
  toTypedSchema(
    z.object({
      identifier: z.string().email('Ugyldig e-post').min(5, 'E-post er for kort'),
      password: z.string().min(1, 'Passord er påkrevd'),
    }),
  ),
)

// === Form logic ===
const { handleSubmit, meta, resetForm, setFieldError } = useForm({
  validationSchema: formSchema,
})

// Loading state
const isLoading = ref(false)
// Track login attempts to allow resubmission
const hasAttemptedLogin = ref(false)
// Form level error message
const formError = ref('')

// Define error type for reuse
type ApiError = {
  response?: { data?: { message?: string }; status?: number }
}

// Function to provide specific error messages based on error responses
const getLoginErrorMessage = (error: ApiError) => {
  const message = 'Kunne ikke logge inn. Vennligst prøv igjen.'

  const errorMessage = error?.response?.data?.message || ''
  const statusCode = error?.response?.status

  // Check for specific error types and provide user-friendly messages
  if (
    errorMessage.includes('Bad credentials') ||
    errorMessage.includes('incorrect password') ||
    errorMessage.includes('wrong password') ||
    statusCode === 401
  ) {
    return 'Feil e-post eller passord. Vennligst prøv igjen.'
  }

  if (statusCode === 404) {
    return 'E-postadressen er ikke registrert. Vennligst registrer deg eller sjekk om du har skrevet riktig e-post.'
  }

  if (errorMessage.includes('locked')) {
    // Extract timestamp if available
    const lockTimeMatch = errorMessage.match(/locked until (.+)/i)
    if (lockTimeMatch && lockTimeMatch[1]) {
      try {
        // Parse the datetime from the error message
        const lockTime = new Date(lockTimeMatch[1])
        const now = new Date()

        // Calculate minutes remaining
        const minutesRemaining = Math.ceil((lockTime.getTime() - now.getTime()) / 60000)

        if (minutesRemaining > 0) {
          return `Kontoen er låst på grunn av for mange innloggingsforsøk. Vennligst prøv igjen om ${minutesRemaining} minutt${minutesRemaining === 1 ? '' : 'er'}.`
        }
      } catch (_e) {
        // If parsing fails, just use the generic message
        console.error('Error parsing lock time:', _e)
        return 'Kontoen er låst på grunn av for mange innloggingsforsøk. Vennligst prøv igjen senere.'
      }
    }
    return 'Kontoen er låst på grunn av for mange innloggingsforsøk. Vennligst prøv igjen senere.'
  }

  if (errorMessage.includes('disabled')) {
    return 'Kontoen er deaktivert. Vennligst kontakt administrator for hjelp.'
  }

  if (statusCode === 429) {
    return 'For mange innloggingsforsøk. Vennligst vent litt før du prøver igjen.'
  }

  return errorMessage || message
}

// Submit handler
const onSubmit = handleSubmit(async (values) => {
  if (isLoading.value) return

  isLoading.value = true
  hasAttemptedLogin.value = true
  // Clear any previous errors
  formError.value = ''

  try {
    // Map identifier to email for backend compatibility
    const loginData = {
      email: values.identifier,
      password: values.password,
    }
    try {
      await authStore.login(loginData)
      toast('Suksess', {
        description: 'Du er nå logget inn',
      })
    } catch (error: unknown) {
      const errorMessage = getLoginErrorMessage(error as ApiError)
      formError.value = errorMessage

      // Set field-specific errors when appropriate
      if (errorMessage.includes('Feil e-post eller passord')) {
        setFieldError('password', 'Feil passord')
      }

      if (errorMessage.includes('ikke registrert')) {
        setFieldError('identifier', 'E-postadressen er ikke registrert')
      }

      toast('Innloggingsfeil', {
        description: errorMessage,
      })
    }

    // Redirect to the intended page after successful login
    await router.push(redirectPath.value)
  } catch (error: unknown) {
    const errorMessage = getLoginErrorMessage(error as ApiError)
    formError.value = errorMessage

    toast('Innloggingsfeil', {
      description: errorMessage,
    })

    // Clear password field but maintain identifier
    resetForm({
      values: {
        identifier: values.identifier,
        password: '',
      },
      touched: { identifier: true, password: false },
      errors: {}, // Clear all validation errors
    })

    // Re-enable the form for submission
    hasAttemptedLogin.value = false
  } finally {
    isLoading.value = false
  }
})
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-white">
    <form
      @submit="onSubmit"
      class="w-full max-w-sm p-8 border border-gray-200 rounded-xl shadow-sm bg-white space-y-5"
    >
      <h1 class="text-3xl font-bold text-center">Innlogging</h1>

      <!-- Form-level error alert -->
      <Alert v-if="formError" variant="destructive" class="bg-red-50 border-red-300 text-red-700">
        <AlertCircle class="h-4 w-4" />
        <AlertDescription class="ml-2">{{ formError }}</AlertDescription>
      </Alert>

      <!-- Email Field -->
      <FormField v-slot="{ componentField }" name="identifier">
        <FormItem>
          <FormLabel class="block text-sm font-medium text-gray-700 mb-1">E-post</FormLabel>
          <FormControl>
            <div class="relative">
              <Mail
                class="absolute left-2 top-1/2 transform -translate-y-1/2 text-gray-500 h-4 w-4"
              />
              <Input
                type="email"
                placeholder="navn@eksempel.no"
                class="w-full px-3 py-2 pl-8 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                v-bind="componentField"
                autocomplete="username"
              />
            </div>
          </FormControl>
          <FormMessage class="text-sm text-red-500" />
        </FormItem>
      </FormField>

      <!-- Password Field using component -->
      <FormField v-slot="{ componentField }" name="password">
        <PasswordInput
          name="password"
          label="Passord"
          placeholder="********"
          :componentField="componentField"
          :showToggle="true"
          :showIcon="true"
          autocomplete="current-password"
        />
      </FormField>

      <!-- Submit Button -->
      <Button
        type="submit"
        :disabled="((!meta.valid || !meta.dirty) && !hasAttemptedLogin) || isLoading"
        class="w-full hover:cursor-pointer bg-blue-600 hover:bg-blue-700 disabled:bg-gray-400 disabled:cursor-not-allowed text-white py-2 rounded-md text-sm font-medium"
      >
        <template v-if="isLoading">Logger inn...</template>
        <template v-else>Logg inn</template>
      </Button>

      <!-- Bottom links -->
      <div class="text-sm text-center space-y-2">
        <div>
          <span class="text-gray-600">Har du ikke en konto?</span>
          <a href="/registrer" class="ml-1 text-blue-600 hover:underline">Registrer deg</a>
        </div>

        <a href="/glemt-passord" class="block text-blue-500 hover:underline">
          Glemt passordet ditt?
        </a>
      </div>
    </form>
  </div>
</template>
