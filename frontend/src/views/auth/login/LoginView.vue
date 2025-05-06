<script setup lang="ts">
import { computed, ref } from 'vue'
import { useForm } from 'vee-validate'
import { toTypedSchema } from '@vee-validate/zod'
import * as z from 'zod'
import { useAuthModeStore } from '@/stores/auth/useAuthModeStore.ts'
import { useAuthStore } from '@/stores/auth/useAuthStore.ts'
import { User, Mail } from 'lucide-vue-next'
import { useRoute, useRouter } from 'vue-router'
import { toast } from 'vue-sonner'

// UI components
import { Button } from '@/components/ui/button'
import { FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form'
import { Input } from '@/components/ui/input'
import PasswordInput from '@/components/auth/PasswordInput.vue'

// === Store logic ===
const authModeStore = useAuthModeStore()
const authStore = useAuthStore()
const route = useRoute()
const router = useRouter()

// Get redirect path from route query if available
const redirectPath = computed(() => (route.query.redirect as string) || '/dashboard')

// Computed value to check if login mode is admin
const isAdmin = computed(() => authModeStore.isAdmin)

// === Schema logic ===
// Dynamically set schema based on isAdmin
const formSchema = computed(() =>
  toTypedSchema(
    z.object({
      identifier: isAdmin.value
        ? z.string().min(3, 'Brukernavn må være minst 3 tegn')
        : z.string().email('Ugyldig e-post').min(5, 'E-post er for kort'),
      password: z
        .string()
        .min(8, 'Passord må være minst 8 tegn')
        .max(50, 'Passord kan være maks 50 tegn')
        .regex(/[A-Z]/, 'Passord må inneholde minst én stor bokstav')
        .regex(/[a-z]/, 'Passord må inneholde minst én liten bokstav')
        .regex(/[0-9]/, 'Passord må inneholde minst ett tall')
        .regex(/[^A-Za-z0-9]/, 'Passord må inneholde minst ett spesialtegn'),
    }),
  ),
)

// === Form logic ===
// useForm returns handleSubmit, meta, and resetForm for tracking state
const { handleSubmit, meta, resetForm } = useForm({
  validationSchema: formSchema,
})

// Loading state
const isLoading = ref(false)

// Function to provide specific error messages based on error responses
const getLoginErrorMessage = (error: {
  response?: { data?: { message?: string }; status?: number }
}) => {
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

  if (errorMessage.includes('not found') || errorMessage.includes('no user')) {
    return isAdmin.value
      ? 'Brukernavn finnes ikke. Sjekk om du har skrevet riktig brukernavnet.'
      : 'E-postadressen er ikke registrert. Vennligst registrer deg eller sjekk om du har skrevet riktig e-post.'
  }

  if (errorMessage.includes('locked') || errorMessage.includes('disabled')) {
    return 'Kontoen er låst. Vennligst kontakt administrator for hjelp.'
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
  try {
    // Map identifier to email for backend compatibility
    const loginData = {
      email: values.identifier,
      password: values.password,
    }

    await authStore.login(loginData)
    toast('Suksess', {
      description: 'Du er nå logget inn',
    })

    // Redirect to the intended page after successful login
    await router.push(redirectPath.value)
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
  } catch (error: any) {
    toast('Innloggingsfeil', {
      description: getLoginErrorMessage(error),
    })

    resetForm({
      values: {
        identifier: values.identifier,
        password: '',
      },
    })
  } finally {
    isLoading.value = false
  }
})

// Toggle between user/admin mode and reset the form
function toggleLoginType() {
  authModeStore.toggle()
  resetForm()
}
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-white">
    <form
      @submit="onSubmit"
      class="w-full max-w-sm p-8 border border-gray-200 rounded-xl shadow-sm bg-white space-y-5"
    >
      <h1 class="text-3xl font-bold text-center">
        {{ isAdmin ? 'Admin-innlogging' : 'Innlogging' }}
      </h1>

      <!-- Email or Username Field -->
      <FormField v-slot="{ componentField }" name="identifier">
        <FormItem>
          <FormLabel class="block text-sm font-medium text-gray-700 mb-1">
            {{ isAdmin ? 'Brukernavn' : 'E-post' }}
          </FormLabel>
          <FormControl>
            <div class="relative">
              <User
                v-if="isAdmin"
                class="absolute left-2 top-1/2 transform -translate-y-1/2 text-gray-500 h-4 w-4"
              />
              <Mail
                v-else
                class="absolute left-2 top-1/2 transform -translate-y-1/2 text-gray-500 h-4 w-4"
              />
              <Input
                :type="isAdmin ? 'text' : 'email'"
                :placeholder="isAdmin ? 'admin_bruker' : 'navn@eksempel.no'"
                class="w-full px-3 py-2 pl-8 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                v-bind="componentField"
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
        />
      </FormField>

      <!-- Submit Button (disabled unless form is valid and touched) -->
      <Button
        type="submit"
        :disabled="!meta.valid || !meta.dirty || isLoading"
        class="w-full hover:cursor-pointer bg-blue-600 hover:bg-blue-700 disabled:bg-gray-400 disabled:cursor-not-allowed text-white py-2 rounded-md text-sm font-medium"
      >
        <template v-if="isLoading">Logger inn...</template>
        <template v-else>{{ isAdmin ? 'Logg inn som admin' : 'Logg inn' }}</template>
      </Button>

      <!-- Bottom links -->
      <div class="text-sm text-center space-y-2">
        <div v-if="!isAdmin">
          <span class="text-gray-600">Har du ikke en konto?</span>
          <a href="/registrer" class="ml-1 text-blue-600 hover:underline">Registrer deg</a>
        </div>

        <a href="/glemt-passord" class="block text-blue-500 hover:underline">
          Glemt passordet ditt?
        </a>

        <Button
          type="button"
          variant="link"
          @click="toggleLoginType"
          class="text-blue-400 hover:text-blue-500 hover:underline transition-colors"
        >
          {{ isAdmin ? 'Bytt til brukerinnlogging' : 'Bytt til admininnlogging' }}
        </Button>
      </div>
    </form>
  </div>
</template>
