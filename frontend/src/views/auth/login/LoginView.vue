<script setup lang="ts">
import { computed } from 'vue'
import { Mail, AlertCircle } from 'lucide-vue-next'
import { useRoute } from 'vue-router'

import { Button } from '@/components/ui/button'
import { FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form'
import { Input } from '@/components/ui/input'
import PasswordInput from '@/components/auth/PasswordInput.vue'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { useLoginForm } from '@/utils/auth/useLoginForm'

const route = useRoute()
const redirectPath = computed(() => (route.query.redirect as string) || '/dashboard')
const { isLoading, hasAttemptedLogin, formError, meta, onSubmit } = useLoginForm(redirectPath.value)
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
