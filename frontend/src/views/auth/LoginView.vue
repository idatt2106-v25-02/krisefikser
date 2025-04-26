<script setup lang="ts">
import { ref, computed } from 'vue'
import { useForm } from 'vee-validate'
import { toTypedSchema } from '@vee-validate/zod'
import * as z from 'zod'
import { useAuthModeStore } from '@/stores/useAuthModeStore'
import { useAuthStore } from '@/stores/useAuthStore'
import { User, Mail, Lock, Eye, EyeOff } from 'lucide-vue-next'
import { useToast } from '@/components/ui/toast/use-toast'
import { useRoute, useRouter } from 'vue-router'

// UI components
import { Button } from '@/components/ui/button'
import { FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form'
import { Input } from '@/components/ui/input'

// === Store logic ===
const authModeStore = useAuthModeStore()
const authStore = useAuthStore()
const { toast } = useToast()
const route = useRoute()
const router = useRouter()

// Get redirect path from route query if available
const redirectPath = computed(() => route.query.redirect as string || '/dashboard')

// Computed value to check if login mode is admin
const isAdmin = computed(() => authModeStore.isAdmin)

// === Schema logic ===
// Dynamically set schema based on isAdmin
const formSchema = computed(() =>
  toTypedSchema(
    z.object({
      email: isAdmin.value
        ? z.string().min(3, 'Brukernavn må være minst 3 tegn')
        : z.string().email('Ugyldig e-post').min(5, 'E-post må være minst 5 tegn'),
      password: z
        .string()
        .min(8, 'Passord må være minst 8 tegn')
        .max(50, 'Passord må være høyst 50 tegn')
        .regex(/[A-Z]/, 'Passord må inkludere minst én stor bokstav')
        .regex(/[a-z]/, 'Passord må inkludere minst én liten bokstav')
        .regex(/[0-9]/, 'Passord må inkludere minst én tall')
        .regex(/[^A-Za-z0-9]/, 'Passord må inkludere minst én spesialtegn'),
    })
  )
)

// === Form logic ===
// useForm returns handleSubmit, meta, and resetForm for tracking state
const { handleSubmit, meta, resetForm } = useForm({
  validationSchema: formSchema,
})

// Loading state
const isLoading = ref(false)

// Submit handler
const onSubmit = handleSubmit(async (values) => {
  if (isLoading.value) return

  isLoading.value = true
  try {
    await authStore.login(values)
    toast({
      title: 'Suksess',
      description: 'Du er nå logget inn',
      variant: 'default',
    })

    // Redirect to the intended page after successful login
    router.push(redirectPath.value)
  } catch (error: any) {
    toast({
      title: 'Feil',
      description: error?.response?.data?.message || 'Kunne ikke logge inn. Vennligst prøv igjen.',
      variant: 'destructive',
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

// Show/hide password
const showPassword = ref(false)
function toggleShowPassword() {
  showPassword.value = !showPassword.value
}
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-white">
    <form
      @submit="onSubmit"
      class="w-full max-w-sm p-8 border border-gray-200 rounded-xl shadow-sm bg-white space-y-5"
    >
      <h1 class="text-3xl font-bold text-center">
        {{ isAdmin ? 'Admin Innlogging' : 'Logg inn' }}
      </h1>

      <!-- Email or Username Field -->
      <FormField v-slot="{ componentField }" name="email">
        <FormItem>
          <FormLabel class="block text-sm font-medium text-gray-700 mb-1">
            {{ isAdmin ? 'Brukernavn' : 'E-post' }}
          </FormLabel>
          <FormControl>
            <div class="relative">
              <User v-if="isAdmin" class="absolute left-2 top-1/2 transform -translate-y-1/2 text-gray-500 h-4 w-4" />
              <Mail v-else class="absolute left-2 top-1/2 transform -translate-y-1/2 text-gray-500 h-4 w-4" />
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

      <!-- Password Field -->
      <FormField v-slot="{ componentField }" name="password">
        <FormItem>
          <FormLabel class="block text-sm font-medium text-gray-700 mb-1">Passord</FormLabel>
          <FormControl>
            <div class="relative">
              <Lock class="absolute left-2 top-1/2 transform -translate-y-1/2 text-gray-500 h-4 w-4" />
              <Input
                :type="showPassword ? 'text' : 'password'"
                placeholder="********"
                class="w-full px-3 py-2 pl-8 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 pr-10"
                v-bind="componentField"
              />
              <button
                type="button"
                @click="toggleShowPassword"
                class="absolute inset-y-0 right-2 flex items-center text-sm text-gray-600 focus:outline-none"
                tabindex="-1"
              >
                <Eye v-if="!showPassword" class="h-4 w-4" />
                <EyeOff v-else class="h-4 w-4" />
              </button>
            </div>
          </FormControl>
          <FormMessage class="text-sm text-red-500" />
        </FormItem>
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
          <a href="/register" class="ml-1 text-blue-600 hover:underline">Registrer deg</a>
        </div>

        <a href="/forgot-password" class="block text-blue-500 hover:underline">
          Glemt passord?
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
