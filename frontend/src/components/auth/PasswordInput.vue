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
}

withDefaults(defineProps<Props>(), {
  label: 'Passord',
  placeholder: '********',
  showToggle: true,
  showIcon: true,
})

// Stores the show password state
const showPassword = ref(false)

// Toggle the visibility of the password
function toggleShowPassword() {
  showPassword.value = !showPassword.value
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
  </FormItem>
</template>
