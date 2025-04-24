<script setup lang="ts">
import { ref } from 'vue'
import { FormControl, FormItem, FormLabel, FormMessage } from '@/components/ui/form'
import { Input } from '@/components/ui/input'

interface Props {
  name: string;
  label?: string;
  placeholder?: string;
  componentField: any;
  showToggle?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  label: 'Password',
  placeholder: '********',
  showToggle: true
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
    <FormLabel v-if="label" class="block text-sm font-medium text-gray-700 mb-1">{{ label }}</FormLabel>
    <FormControl>
      <div class="relative">
        <Input
          :type="showPassword ? 'text' : 'password'"
          :placeholder="placeholder"
          class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 pr-10"
          v-bind="componentField"
        />
        <button
          v-if="showToggle"
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
</template>
