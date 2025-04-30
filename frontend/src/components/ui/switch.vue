<script setup lang="ts">
import { onMounted } from 'vue'

const props = defineProps<{
  modelValue: boolean
}>()

const emit = defineEmits<{
  'update:model-value': [value: boolean]
}>()

const handleClick = (event: MouseEvent) => {
  console.log('Switch clicked!', event) // Debug log
  const newValue = !props.modelValue
  console.log('New value:', newValue) // Debug log
  emit('update:model-value', newValue)
}

onMounted(() => {
  console.log('Switch mounted') // Debug log
})
</script>

<template>
  <button
    type="button"
    role="switch"
    :aria-checked="modelValue"
    :class="[
      'relative inline-flex h-6 w-11 flex-shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2',
      modelValue ? 'bg-blue-600' : 'bg-gray-200',
    ]"
    @click="handleClick"
    @mousedown="console.log('mousedown')"
    @mouseup="console.log('mouseup')"
  >
    <span
      aria-hidden="true"
      :class="[
        'pointer-events-none inline-block h-5 w-5 transform rounded-full bg-white shadow ring-0 transition duration-200 ease-in-out',
        modelValue ? 'translate-x-5' : 'translate-x-0',
      ]"
    />
  </button>
</template>
