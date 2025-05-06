<script setup lang="ts">
import { ref } from 'vue'
import { FormControl, FormItem, FormLabel, FormMessage } from '@/components/ui/form'
import { Input } from '@/components/ui/input'
import { Eye, EyeOff, Lock } from 'lucide-vue-next'

interface Props {
  name: string
  label?: string
  placeholder?: string
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  componentField: any
  showToggle?: boolean
  showIcon?: boolean
  showComplexityRequirements?: boolean
}

withDefaults(defineProps<Props>(), {
  label: 'Passord',
  placeholder: '********',
  showToggle: true,
  showIcon: true,
  showComplexityRequirements: false,
})

// Stores the show password state
const showPassword = ref(false)

// Toggle the visibility of the password
function toggleShowPassword() {
  showPassword.value = !showPassword.value
}

// Function to check if password meets requirements
// This is for display purposes only, actual validation happens in the schema
const getPasswordStrength = (password: string) => {
  if (!password) return { strength: 0, message: '' }

  let strength = 0
  let message = 'Svakt passord'

  if (password.length >= 8) strength++
  if (password.match(/[A-Z]/)) strength++
  if (password.match(/[a-z]/)) strength++
  if (password.match(/[0-9]/)) strength++
  if (password.match(/[^A-Za-z0-9]/)) strength++

  if (strength === 5) message = 'Sterkt passord'
  else if (strength >= 3) message = 'Middels passord'

  return { strength, message }
}
</script>

<template>
  <FormItem>
    <FormLabel v-if="label" class="block text-sm font-medium text-gray-700 mb-1">{{
      label
    }}</FormLabel>
    <FormControl>
      <div class="relative">
        <Lock
          v-if="showIcon"
          class="absolute left-2 top-1/2 transform -translate-y-1/2 text-gray-500 h-4 w-4"
        />
        <Input
          :type="showPassword ? 'text' : 'password'"
          :placeholder="placeholder"
          :class="[
            'w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 pr-10',
            showIcon ? 'pl-8' : '',
          ]"
          v-bind="componentField"
        />
        <button
          v-if="showToggle"
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

    <!-- Password complexity requirements - shown only during registration -->
    <div v-if="showComplexityRequirements && componentField?.value" class="mt-2">
      <div class="text-xs text-gray-500 mb-1">Passordet må inneholde:</div>
      <ul class="text-xs space-y-1 ml-1">
        <li :class="componentField.value.length >= 8 ? 'text-green-600' : 'text-gray-400'">
          ✓ Minst 8 tegn
        </li>
        <li :class="componentField.value.match(/[A-Z]/) ? 'text-green-600' : 'text-gray-400'">
          ✓ Minst én stor bokstav
        </li>
        <li :class="componentField.value.match(/[a-z]/) ? 'text-green-600' : 'text-gray-400'">
          ✓ Minst én liten bokstav
        </li>
        <li :class="componentField.value.match(/[0-9]/) ? 'text-green-600' : 'text-gray-400'">
          ✓ Minst ett tall
        </li>
        <li :class="componentField.value.match(/[^A-Za-z0-9]/) ? 'text-green-600' : 'text-gray-400'">
          ✓ Minst ett spesialtegn (f.eks. !@#$%)
        </li>
      </ul>

      <!-- Password strength indicator -->
      <div v-if="componentField?.value" class="mt-2">
        <div class="flex items-center justify-between">
          <div class="h-1 flex-1 bg-gray-200 rounded-full overflow-hidden">
            <div
              class="h-full transition-all duration-300"
              :class="{
                'bg-red-500': getPasswordStrength(componentField.value).strength <= 2,
                'bg-yellow-500': getPasswordStrength(componentField.value).strength === 3 || getPasswordStrength(componentField.value).strength === 4,
                'bg-green-500': getPasswordStrength(componentField.value).strength === 5
              }"
              :style="`width: ${(getPasswordStrength(componentField.value).strength / 5) * 100}%`"
            ></div>
          </div>
          <span class="text-xs ml-2" :class="{
            'text-red-500': getPasswordStrength(componentField.value).strength <= 2,
            'text-yellow-500': getPasswordStrength(componentField.value).strength === 3 || getPasswordStrength(componentField.value).strength === 4,
            'text-green-500': getPasswordStrength(componentField.value).strength === 5
          }">
            {{ getPasswordStrength(componentField.value).message }}
          </span>
        </div>
      </div>
    </div>
  </FormItem>
</template>
