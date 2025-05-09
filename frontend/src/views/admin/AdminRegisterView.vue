<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue'
import { useForm } from 'vee-validate'
import { toTypedSchema } from '@vee-validate/zod'
import * as z from 'zod'
import { Mail, User } from 'lucide-vue-next'
import { useAuthStore } from '@/stores/auth/useAuthStore.ts'
import { toast } from 'vue-sonner'
import { useRoute, useRouter } from 'vue-router'
import { verifyAdminInviteToken } from '@/api/generated/authentication/authentication'
import { Button } from '@/components/ui/button'
import { FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form'
import { Input } from '@/components/ui/input'
import PasswordInput from '@/components/auth/PasswordInput.vue'

// Declare the global turnstile object
declare const turnstile: {
  render: (
    container: string | HTMLElement,
    options: {
      sitekey: string
      callback?: (token: string) => void
      'error-callback'?: () => void
      'expired-callback'?: () => void
      theme?: 'light' | 'dark' | 'auto'
      size?: 'normal' | 'compact'
      tabindex?: number
      'response-field'?: boolean
      'response-field-name'?: string
    },
  ) => string
  reset: (widgetId?: string) => void
  getResponse: (widgetId?: string) => string
  remove: (widgetId?: string) => void
}

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

// Get token from URL
const token = ref(route.query.token as string)
const userEmail = ref('')
const isLoading = ref(false)

// Verify token and get email on component mount
onMounted(async () => {
  if (!token.value) {
    toast('Feil', {
      description: 'Ingen gyldig invitasjonstoken funnet',
    })
    router.push('/')
    return
  }

  isLoading.value = true
  try {
    const response = await verifyAdminInviteToken({ token: token.value })
    userEmail.value = response.email
  } catch {
    toast('Feil', {
      description: 'Ugyldig eller utløpt invitasjonstoken',
    })
    router.push('/')
  } finally {
    isLoading.value = false
  }
})

// Schema for the registration form
const rawSchema = z
  .object({
    firstName: z
      .string({ required_error: 'Fornavn er påkrevd' })
      .min(2, 'Fornavn må være minst 2 tegn'),
    lastName: z
      .string({ required_error: 'Etternavn er påkrevd' })
      .min(2, 'Etternavn må være minst 2 tegn'),
    password: z
      .string({ required_error: 'Passord er påkrevd' })
      .min(8, 'Passord må være minst 8 tegn')
      .max(50, 'Passord kan være maks 50 tegn')
      .regex(/[A-Z]/, 'Må inneholde minst én stor bokstav')
      .regex(/[a-z]/, 'Må inneholde minst én liten bokstav')
      .regex(/[0-9]/, 'Må inneholde minst ett tall')
      .regex(/[^A-Za-z0-9]/, 'Må inneholde minst ett spesialtegn'),
    confirmPassword: z.string({ required_error: 'Bekreft passord er påkrevd' }),
    acceptedPrivacyPolicy: z.literal(true, {
      errorMap: () => ({ message: 'Du må godta personvernerklæringen' }),
    }),
  })
  .refine((data) => data.password === data.confirmPassword, {
    message: 'Passordene stemmer ikke overens',
    path: ['confirmPassword'],
  })

// Set up form validation
const { handleSubmit, meta } = useForm({
  validationSchema: toTypedSchema(rawSchema),
})

// Function to parse error messages
const getErrorMessage = (error: {
  response?: { data?: { message?: string }; status?: number }
}) => {
  const message = 'Kunne ikke registrere. Vennligst prøv igjen.'
  const errorMessage = error?.response?.data?.message || ''

  if (error?.response?.status === 429) {
    return 'For mange forsøk. Vennligst vent litt før du prøver igjen.'
  }

  if (error?.response?.status === 409) {
    return 'E-postadressen er allerede registrert.'
  }

  if (error?.response?.status === 500) {
    return 'Det oppstod en serverfeil. Vennligst prøv igjen senere.'
  }

  return errorMessage || message
}

// Reset Turnstile function
function resetTurnstile() {
  captchaToken.value = ''
  if (turnstileWidgetId.value) {
    turnstile.reset(turnstileWidgetId.value)
  }
}

const onSubmit = handleSubmit(async (values) => {
  if (isLoading.value) return

  isLoading.value = true
  try {
    const { confirmPassword, acceptedPrivacyPolicy, ...registrationData } = values
    await authStore.registerAdmin({
      ...registrationData,
      email: userEmail.value,
      turnstileToken: captchaToken.value,
    })
    toast('Suksess', {
      description: 'Admin-kontoen din er opprettet og du er nå logget inn',
    })
    await router.push('/logg-inn')
  } catch (error: unknown) {
    resetTurnstile()
    toast('Registreringsfeil', {
      description: getErrorMessage(
        error as { response?: { data?: { message?: string }; status?: number } },
      ),
    })
  } finally {
    isLoading.value = false
  }
})

// Captcha logic
const captchaToken = ref('')
const turnstileWidgetId = ref<string>('')

// Initialize Cloudflare Turnstile
onMounted(() => {
  turnstileWidgetId.value = turnstile.render('#turnstile', {
    sitekey: '0x4AAAAAABSTiPNZwrBLQkgr',
    callback: (token: string) => {
      captchaToken.value = token
    },
    'error-callback': () => {
      toast('Error', {
        description: 'Captcha token feil',
      })
      captchaToken.value = ''
    },
    'expired-callback': () => {
      toast('Warning', {
        description: 'Captcha token har utløpt',
      })
      captchaToken.value = ''
    },
  })
})

// Clean up Turnstile on component unmount
onUnmounted(() => {
  if (turnstileWidgetId.value) {
    turnstile.remove(turnstileWidgetId.value)
  }
})
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-50 py-12">
    <form
      @submit="onSubmit"
      class="w-full max-w-sm p-8 border border-gray-200 rounded-xl shadow-sm bg-white space-y-5"
    >
      <h1 class="text-3xl font-bold text-center">Admin Registrering</h1>

      <!-- First Name -->
      <FormField v-slot="{ componentField }" name="firstName">
        <FormItem>
          <FormLabel class="block text-sm font-medium text-gray-700 mb-1">Fornavn</FormLabel>
          <FormControl>
            <div class="relative">
              <User
                class="absolute left-2 top-1/2 transform -translate-y-1/2 text-gray-500 h-4 w-4"
              />
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
              <User
                class="absolute left-2 top-1/2 transform -translate-y-1/2 text-gray-500 h-4 w-4"
              />
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

      <!-- Read-only Email -->
      <div class="space-y-1">
        <label class="block text-sm font-medium text-gray-700">E-post</label>
        <div class="relative">
          <Mail class="absolute left-2 top-1/2 transform -translate-y-1/2 text-gray-500 h-4 w-4" />
          <input
            type="email"
            :value="userEmail"
            readonly
            class="w-full px-3 py-2 pl-8 bg-gray-100 border border-gray-300 rounded-md shadow-sm"
          />
        </div>
      </div>

      <!-- Password field using component -->
      <FormField v-slot="{ componentField }" name="password">
        <PasswordInput
          name="password"
          label="Passord"
          placeholder="********"
          :componentField="componentField"
          :showToggle="true"
          :showIcon="true"
          :showComplexityRequirements="true"
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

      <!-- Accept Privacy Policy -->
      <FormField v-slot="{ value, handleChange }" name="acceptedPrivacyPolicy" type="checkbox">
        <FormItem>
          <div class="flex items-start space-x-2">
            <FormControl>
              <input
                type="checkbox"
                :checked="value"
                @change="handleChange(($event.target as HTMLInputElement)?.checked ?? false)"
                @keydown.enter.prevent="handleChange(!value)"
                @keydown.space.prevent="handleChange(!value)"
                id="acceptedPrivacyPolicy"
                class="mt-1 h-4 w-4 rounded border-gray-300 text-blue-600 focus:ring-blue-500"
                tabindex="0"
                role="checkbox"
                :aria-checked="value"
                aria-label="Godta personvernerklæringen"
              />
            </FormControl>
            <label
              for="acceptedPrivacyPolicy"
              class="text-sm text-gray-700 cursor-pointer select-none"
            >
              Jeg godtar
              <router-link
                to="/personvern"
                class="text-blue-600 hover:underline focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 rounded-sm px-1"
                tabindex="0"
                aria-label="Åpne personvernerklæringen"
              >
                personvernerklæringen
              </router-link>
            </label>
          </div>
          <FormMessage class="text-sm text-red-500" />
        </FormItem>
      </FormField>

      <!-- Cloudflare Turnstile -->
      <div id="turnstile"></div>

      <!-- Submit button -->
      <Button
        type="submit"
        :disabled="!meta.valid || isLoading || !captchaToken || !userEmail"
        class="w-full bg-blue-600 hover:bg-blue-700 disabled:bg-gray-400 disabled:cursor-not-allowed text-white py-2 rounded-md text-sm font-medium"
      >
        <template v-if="isLoading">Oppretter admin-konto...</template>
        <template v-else>Registrer admin-konto</template>
      </Button>
    </form>
  </div>
</template>
