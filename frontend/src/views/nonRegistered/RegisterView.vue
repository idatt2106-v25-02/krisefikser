<script setup lang="ts">
import { onMounted, ref, onUnmounted } from 'vue'
import { useForm } from 'vee-validate'
import { toTypedSchema } from '@vee-validate/zod'
import * as z from 'zod'
import { User, Mail } from 'lucide-vue-next'
import { useAuthStore } from '@/stores/useAuthStore'
import { useToast } from '@/components/ui/toast/use-toast'

import { Button } from '@/components/ui/button'
import { FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form'
import { Input } from '@/components/ui/input'
import PasswordInput from '@/components/auth/PasswordInput.vue'
import PrivacyPolicyView from './PrivacyPolicyView.vue'
import { Dialog, DialogContent } from '@/components/ui/dialog'
import { useRouter } from 'vue-router';
const router = useRouter();

// Schema for the registration form
const rawSchema = z
  .object({
    firstName: z.string().min(2, 'Fornavn må være minst 2 tegn'),
    lastName: z.string().min(2, 'Etternavn må være minst 2 tegn'),
    email: z.string().email('Ugyldig e-post').min(5, 'E-post må være minst 5 tegn'),
    householdCode: z
      .string()
      .refine((val) => val === '' || (val.length === 5 && /^[a-zA-Z]+$/.test(val)), {
        message: 'Husholdningskode må være nøyaktig 5 bokstaver (ingen tall)',
      })
      .optional(),
    password: z
      .string()
      .min(8, 'Passord må være minst 8 tegn')
      .max(50, 'Passord kan være maks 50 tegn')
      .regex(/[A-Z]/, 'Må inneholde minst én stor bokstav')
      .regex(/[a-z]/, 'Må inneholde minst én liten bokstav')
      .regex(/[0-9]/, 'Må inneholde minst ett tall')
      .regex(/[^A-Za-z0-9]/, 'Må inneholde minst ett spesialtegn'),
    confirmPassword: z.string(),
    acceptedPrivacyPolicy: z.literal(true, {
      errorMap: () => ({ message: 'Du må godta personvernerklæringen' }),
    }),
    //turnstileToken: z.string().min(1, 'Vennligst fullfør CAPTCHA-verifiseringen')
  })
  .refine((data) => data.password === data.confirmPassword, {
    message: 'Passordene stemmer ikke overens',
    path: ['confirmPassword'],
  })

// Set up consts for submit button deactivation
const { handleSubmit, meta } = useForm({
  validationSchema: toTypedSchema(rawSchema),
})

// Get auth store and toast
const authStore = useAuthStore()
const { toast } = useToast()

// Loading state
const isLoading = ref(false)

// Function to parse error messages and provide specific user feedback
const getErrorMessage = (error: { response?: { data?: { message?: string }; status?: number } }) => {
  // Default message
  const message = 'Kunne ikke registrere. Vennligst prøv igjen.';

  // Extract error message from response if available
  const errorMessage = error?.response?.data?.message || '';

  if (error?.response?.status === 429) {
    return 'For mange forsøk. Vennligst vent litt før du prøver igjen.';
  }

  if (error?.response?.status === 409) {
    return 'E-postadressen er allerede registrert. Vennligst bruk en annen e-post eller prøv å logge inn.';
  }

  if (error?.response?.status === 500) {
    return 'Det oppstod en serverfeil. Vennligst prøv igjen senere.';
  }

  return errorMessage || message;
}

const onSubmit = handleSubmit(async (values) => {
  if (isLoading.value) return

  isLoading.value = true
  try {
    const { confirmPassword, acceptedPrivacyPolicy, ...registrationData } = values
    await authStore.register(registrationData)
    toast({
      title: 'Suksess',
      description: 'Kontoen din er opprettet og du er nå logget inn',
      variant: 'default',
    })
    await router.push('/dashboard');
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
  } catch (error: any) {
    toast({
      title: 'Registreringsfeil',
      description: getErrorMessage(error),
      variant: 'destructive',
    })
  } finally {
    isLoading.value = false
  }
})

// Dialog state
const isPrivacyPolicyOpen = ref(false)

// Captcha logic
const captchaToken = ref('')
const turnstileWidgetId = ref<string>('')

// Initialise Cloudflare Turnstile
onMounted(() => {
  turnstileWidgetId.value = turnstile.render('#turnstile', {
    sitekey: '0x4AAAAAABSTiPNZwrBLQkgr',
    callback: (token: string) => {
      captchaToken.value = token
      toast({
        title: 'Success',
        description: 'Captcha token received',
        variant: 'default'
      })
    },
    'error-callback': () => {
      toast({
        title: 'Error',
        description: 'Captcha token error',
        variant: 'destructive'
      })
      captchaToken.value = ''
    },
    'expired-callback': () => {
      toast({
        title: 'Warning',
        description: 'Captcha token expired',
        variant: 'warning'
      })
      captchaToken.value = ''
    }
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
      @submit="onSubmit"
      class="w-full max-w-sm p-8 border border-gray-200 rounded-xl shadow-sm bg-white space-y-5"
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
                type="email"
                placeholder="navn@eksempel.no"
                class="w-full px-3 py-2 pl-8 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
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
          name="password"
          label="Passord"
          placeholder="********"
          :componentField="componentField"
          :showToggle="true"
          :showIcon="true"
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
                id="acceptedPrivacyPolicy"
                class="mt-1"
              />
            </FormControl>
            <label for="acceptedPrivacyPolicy" class="text-sm text-gray-700">
              Jeg godtar
              <button
                type="button"
                @click="isPrivacyPolicyOpen = true"
                class="text-blue-600 hover:underline"
              >
                personvernerklæringen
              </button>
            </label>
          </div>
          <FormMessage class="text-sm text-red-500" />
        </FormItem>
      </FormField>

      <div id="turnstile"></div>

      <!-- Cloudflare Turnstile -->
      <!-- <FormField v-slot="{ componentField }" name="turnstileToken">
        <FormItem>
          <FormLabel class="block text-sm font-medium text-gray-700 mb-1">Bekreft at du er et menneske</FormLabel>
          <FormControl>
            <div class="flex justify-center">
              <div class="relative w-full flex justify-center">
                <Shield class="absolute left-2 top-0 text-gray-500 h-4 w-4" />
                <VueTurnstile
                  site-key="0x4AAAAAABSTiPNZwrBLQkgr"
                  v-model="turnstileToken"
                  theme="light"
                  @success="onTurnstileSuccess"
                  @error="onTurnstileError"
                  @expire="onTurnstileExpire"
                />
              </div>
            </div>
          </FormControl>
          <FormMessage class="text-sm text-red-500" />
        </FormItem>
      </FormField> -->

      <!-- Privacy Policy Dialog -->
      <Dialog :open="isPrivacyPolicyOpen" @update:open="isPrivacyPolicyOpen = $event">
        <DialogContent class="max-w-3xl max-h-[80vh] overflow-y-auto">
          <PrivacyPolicyView />
        </DialogContent>
      </Dialog>

      <!-- Submit button -->
      <Button
        type="submit"
        :disabled="!meta.valid || isLoading"
        class="w-full bg-blue-600 hover:bg-blue-700 disabled:bg-gray-400 disabled:cursor-not-allowed text-white py-2 rounded-md text-sm font-medium"
      >
        <template v-if="isLoading">Oppretter konto...</template>
        <template v-else>Registrer deg</template>
      </Button>

      <!-- Conditional CTAs below -->
      <div class="text-sm text-center space-y-2">
        <div>
          <span class="text-gray-600">Har du allerede en konto?</span>
          <a href="/logg-inn" class="ml-1 text-blue-600 hover:underline">Logg inn</a>
        </div>
      </div>
    </form>
  </div>
</template>
