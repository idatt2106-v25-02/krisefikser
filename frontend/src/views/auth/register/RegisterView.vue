<script lang="ts" setup>
import { nextTick, onMounted, onUnmounted, ref } from 'vue'
import { useForm } from 'vee-validate'
import { toTypedSchema } from '@vee-validate/zod'
import * as z from 'zod'
import { Mail, User } from 'lucide-vue-next'
import { useAuthStore } from '@/stores/auth/useAuthStore'
import { toast } from 'vue-sonner'
import { Button } from '@/components/ui/button'
import { FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form'
import { Input } from '@/components/ui/input'
import PasswordInput from '@/components/auth/PasswordInput.vue'
import { useRouter } from 'vue-router'
import { createPasswordConfirmationSchema, type ApiError } from '@/utils/validation/passwordSchemas'
import { showSuccessToast, showErrorToast } from '@/utils/error/errorHandling'

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

const router = useRouter()

// Schema for the registration form
const passwordFields = createPasswordConfirmationSchema()

// Define the schema shape explicitly
const schemaShape = {
  firstName: z
    .string({ required_error: 'Fornavn er påkrevd' })
    .min(2, 'Fornavn må være minst 2 tegn'),
  lastName: z
    .string({ required_error: 'Etternavn er påkrevd' })
    .min(2, 'Etternavn må være minst 2 tegn'),
  email: z
    .string({ required_error: 'E-post er påkrevd' })
    .email('Ugyldig e-post')
    .min(5, 'E-post må være minst 5 tegn'),
  householdCode: z
    .string()
    .refine((val) => val === '' || (val.length === 5 && /^[a-zA-Z]+$/.test(val)), {
      message: 'Husholdningskode må være nøyaktig 5 bokstaver (ingen tall)',
    })
    .optional(),
  password: passwordFields.password,
  confirmPassword: passwordFields.confirmPassword,
  acceptedPrivacyPolicy: z.literal(true, {
    errorMap: () => ({ message: 'Du må godta personvernerklæringen' }),
  }),
}

const rawSchema = z.object(schemaShape)

type RegistrationFormValues = z.infer<typeof rawSchema>

// Set up consts for submit button deactivation
const { handleSubmit, meta, setFieldError } = useForm<RegistrationFormValues>({
  validationSchema: toTypedSchema(rawSchema),
})

// Get auth store
const authStore = useAuthStore()

// Loading state
const isLoading = ref(false)
const emailInputRef = ref<HTMLInputElement | null>(null)

// Function to parse error messages and provide specific user feedback
const handleRegistrationError = (error: unknown) => {
  // Reset the captcha on failed registration
  resetTurnstile()

  const apiError = error as ApiError
  if (apiError.response?.status === 409) {
    // Set error directly on the email field
    setFieldError(
      'email',
      'E-postadressen er allerede registrert. Vennligst bruk en annen e-post eller prøv å logge inn.',
    )
    // Use nextTick to ensure DOM is updated before focusing
    nextTick(() => {
      emailInputRef.value?.focus()
    })
  }

  showErrorToast('Registreringsfeil', error)
}

// Reset Turnstile function to rerender captcha
function resetTurnstile() {
  captchaToken.value = ''
  // Reset the turnstile widget
  if (turnstileWidgetId.value) {
    turnstile.reset(turnstileWidgetId.value)
  }
}

const onSubmit = handleSubmit(async (values: RegistrationFormValues) => {
  if (isLoading.value) return

  isLoading.value = true
  try {
    const { confirmPassword, acceptedPrivacyPolicy, ...registrationData } = values
    await authStore.register({
      ...registrationData,
      turnstileToken: captchaToken.value,
    })
    showSuccessToast('Suksess')
    await router.push('/bekreft-e-post')
  } catch (error: unknown) {
    handleRegistrationError(error)
  } finally {
    isLoading.value = false
  }
})

// Captcha logic
const captchaToken = ref('')
const turnstileWidgetId = ref<string>('')

// Initialise Cloudflare Turnstile
onMounted(() => {
  turnstileWidgetId.value = turnstile.render('#turnstile', {
    sitekey: '0x4AAAAAABSTiPNZwrBLQkgr',
    callback: (token: string) => {
      captchaToken.value = token
      toast('Success', {
        description: 'Captcha token hentet',
      })
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
  <div class="min-h-screen flex items-center justify-center bg-white py-12">
    <form
      class="w-full max-w-sm p-8 border border-gray-200 rounded-xl shadow-sm bg-white space-y-5"
      @submit="onSubmit"
    >
      <h1 class="text-3xl font-bold text-center">Registrer deg</h1>

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
                class="w-full px-3 py-2 pl-8 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="Ola"
                type="text"
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
                class="w-full px-3 py-2 pl-8 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="Nordmann"
                type="text"
                v-bind="componentField"
              />
            </div>
          </FormControl>
          <FormMessage class="text-sm text-red-500" />
        </FormItem>
      </FormField>

      <!-- Email -->
      <FormField v-slot="{ componentField }" name="email">
        <FormItem>
          <FormLabel class="block text-sm font-medium text-gray-700 mb-1">E-post</FormLabel>
          <FormControl>
            <div class="relative">
              <Mail
                class="absolute left-2 top-1/2 transform -translate-y-1/2 text-gray-500 h-4 w-4"
              />
              <Input
                ref="emailInputRef"
                class="w-full px-3 py-2 pl-8 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="navn@eksempel.no"
                type="email"
                v-bind="componentField"
              />
            </div>
          </FormControl>
          <FormMessage class="text-sm text-red-500" />
        </FormItem>
      </FormField>

      <!-- Password field using component -->
      <FormField v-slot="{ componentField }" name="password">
        <PasswordInput
          :componentField="componentField"
          :showComplexityRequirements="true"
          :showIcon="true"
          :showToggle="true"
          label="Passord"
          name="password"
          placeholder="********"
        />
      </FormField>

      <!-- Confirm Password using component -->
      <FormField v-slot="{ componentField }" name="confirmPassword">
        <PasswordInput
          :componentField="componentField"
          :showIcon="true"
          :showToggle="true"
          label="Bekreft passord"
          name="confirmPassword"
          placeholder="********"
        />
      </FormField>
      <!-- Accept Privacy Policy -->
      <FormField v-slot="{ value, handleChange }" name="acceptedPrivacyPolicy" type="checkbox">
        <FormItem>
          <div class="flex items-start space-x-2">
            <FormControl>
              <input
                id="acceptedPrivacyPolicy"
                :aria-checked="value"
                :checked="value"
                aria-label="Godta personvernerklæringen"
                class="mt-1 h-4 w-4 rounded border-gray-300 text-blue-600 focus:ring-blue-500"
                role="checkbox"
                tabindex="0"
                type="checkbox"
                @change="handleChange(($event.target as HTMLInputElement)?.checked ?? false)"
                @keydown.enter.prevent="handleChange(!value)"
                @keydown.space.prevent="handleChange(!value)"
              />
            </FormControl>
            <label
              class="text-sm text-gray-700 cursor-pointer select-none"
              for="acceptedPrivacyPolicy"
            >
              Jeg godtar
              <router-link
                aria-label="Åpne personvernerklæringen"
                class="text-blue-600 hover:underline focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 rounded-sm px-1"
                tabindex="0"
                to="/personvern"
                target="_blank"
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
        :disabled="!meta.valid || isLoading || !captchaToken"
        class="w-full bg-blue-600 hover:bg-blue-700 disabled:bg-gray-400 disabled:cursor-not-allowed text-white py-2 rounded-md text-sm font-medium"
        type="submit"
      >
        <template v-if="isLoading">Oppretter konto...</template>
        <template v-else>Registrer deg</template>
      </Button>

      <!-- Conditional CTAs below -->
      <div class="text-sm text-center space-y-2">
        <div>
          <span class="text-gray-600">Har du allerede en konto?</span>
          <a class="ml-1 text-blue-600 hover:underline" href="/logg-inn">Logg inn</a>
        </div>
      </div>
    </form>
  </div>
</template>
