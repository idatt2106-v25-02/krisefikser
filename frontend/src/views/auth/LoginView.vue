<script setup lang="ts">
import { ref, computed } from 'vue'
import { useForm } from 'vee-validate'
import { toTypedSchema } from '@vee-validate/zod'
import * as z from 'zod'
import { useAuthModeStore } from '@/stores/useAuthModeStore'

// UI components
import { Button } from '@/components/ui/button'
import { FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form'
import { Input } from '@/components/ui/input'

// === Store logic ===
const authStore = useAuthModeStore()

// Computed value to check if login mode is admin
const isAdmin = computed(() => authStore.isAdmin)

// === Schema logic ===
// Dynamically set schema based on isAdmin
const formSchema = computed(() =>
  toTypedSchema(
    z.object({
      identifier: isAdmin.value
        ? z.string().min(3, 'Username must be at least 3 characters')
        : z.string().email('Invalid email').min(5, 'Email too short'),
      password: z
        .string()
        .min(8, 'Password must be at least 8 characters')
        .max(50, 'Password must be at most 50 characters')
        .regex(/[A-Z]/, 'Password must include at least one uppercase letter')
        .regex(/[a-z]/, 'Password must include at least one lowercase letter')
        .regex(/[0-9]/, 'Password must include at least one number')
        .regex(/[^A-Za-z0-9]/, 'Password must include at least one special character'),
    })
  )
)

// === Form logic ===
// useForm returns handleSubmit, meta, and resetForm for tracking state
const { handleSubmit, meta, resetForm } = useForm({
  validationSchema: formSchema,
})

// Submit handler
const onSubmit = handleSubmit((values) => {
  console.log(isAdmin.value ? 'Admin login' : 'User login', values)
})

// Toggle between user/admin mode and reset the form
function toggleLoginType() {
  authStore.toggle()
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
        {{ isAdmin ? 'Admin Login' : 'Login' }}
      </h1>

      <!-- Email or Username Field -->
      <FormField v-slot="{ componentField }" name="identifier">
        <FormItem>
          <FormLabel class="block text-sm font-medium text-gray-700 mb-1">
            {{ isAdmin ? 'Username' : 'Email' }}
          </FormLabel>
          <FormControl>
            <Input
              :type="isAdmin ? 'text' : 'email'"
              :placeholder="isAdmin ? 'admin_user' : 'name@example.org'"
              class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
              v-bind="componentField"
            />
          </FormControl>
          <FormMessage class="text-sm text-red-500" />
        </FormItem>
      </FormField>

      <!-- Password Field -->
      <FormField v-slot="{ componentField }" name="password">
        <FormItem>
          <FormLabel class="block text-sm font-medium text-gray-700 mb-1">Password</FormLabel>
          <FormControl>
            <div class="relative">
              <Input
                :type="showPassword ? 'text' : 'password'"
                placeholder="********"
                class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 pr-10"
                v-bind="componentField"
              />
              <button
                type="button"
                @click="toggleShowPassword"
                class="absolute inset-y-0 right-2 flex items-center text-sm text-gray-600 focus:outline-none"
                tabindex="-1"
              >
                {{ showPassword ? 'Hide' : 'Show' }}
              </button>
            </div>
          </FormControl>
          <FormMessage class="text-sm text-red-500" />
        </FormItem>
      </FormField>

      <!-- Submit Button (disabled unless form is valid and touched) -->
      <Button
        type="submit"
        :disabled="!meta.valid || !meta.dirty"
        class="w-full bg-blue-600 hover:bg-blue-700 disabled:bg-gray-400 disabled:cursor-not-allowed text-white py-2 rounded-md text-sm font-medium"
      >
        {{ isAdmin ? 'Login as Admin' : 'Login' }}
      </Button>

      <!-- Bottom links -->
      <div class="text-sm text-center space-y-2">
        <div v-if="!isAdmin">
          <span class="text-gray-600">Don't have an account?</span>
          <a href="/register" class="ml-1 text-blue-600 hover:underline">Register</a>
        </div>

        <a href="/forgot-password" class="block text-blue-500 hover:underline">
          Forgot your password?
        </a>

        <Button
          type="button"
          variant="link"
          @click="toggleLoginType"
          class="text-blue-400 hover:text-blue-500 hover:underline transition-colors"
        >
          {{ isAdmin ? 'Switch to user login' : 'Switch to admin login' }}
        </Button>
      </div>
    </form>
  </div>
</template>
