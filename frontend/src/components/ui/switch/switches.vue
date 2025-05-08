<script lang="ts" setup>
const props = defineProps<{
  modelValue: boolean
}>()

const emit = defineEmits<{
  'update:model-value': [value: boolean]
}>()

const handleClick = (event: MouseEvent) => {
  const newValue = !props.modelValue
  emit('update:model-value', newValue)
}
</script>

<template>
  <button
    :aria-checked="modelValue"
    :class="[
      'relative inline-flex h-6 w-11 flex-shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2',
      modelValue ? 'bg-blue-600' : 'bg-gray-200',
    ]"
    role="switch"
    type="button"
    @click="handleClick"
  >
    <span
      :class="[
        'pointer-events-none inline-block h-5 w-5 transform rounded-full bg-white shadow ring-0 transition duration-200 ease-in-out',
        modelValue ? 'translate-x-5' : 'translate-x-0',
      ]"
      aria-hidden="true"
    />
  </button>
</template>

<script lang="ts">
export const Switch = {}
</script>
