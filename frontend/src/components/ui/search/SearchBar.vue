<script lang="ts" setup>
import { Search } from 'lucide-vue-next'

interface Props {
  modelValue: string
  placeholder?: string
}

defineProps<Props>()
const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
}>()
</script>

<template>
  <div class="flex items-center rounded-lg bg-gray-100 px-3 py-2 w-full md:w-64">
    <label class="sr-only" for="search-bar-input">Søk</label>
    <Search class="h-4 w-4 text-gray-500" />
    <input
      id="search-bar-input"
      :value="modelValue"
      class="bg-transparent border-0 outline-none ml-2 text-gray-700 w-full"
      :placeholder="placeholder || 'Søk...'"
      type="text"
      aria-label="Søk"
      @input="emit('update:modelValue', ($event.target as HTMLInputElement).value)"
    />
  </div>
</template>
