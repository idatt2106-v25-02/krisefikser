<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getIconNames } from '@/utils/icons.ts'

interface Props {
  modelValue: string | undefined
}

withDefaults(defineProps<Props>(), {
  modelValue: ''
})

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
}>()

const icons = ref<string[]>([])

onMounted(() => {
  icons.value = getIconNames()
  console.log(icons.value)
  console.log(icons)
})
</script>

<template>
  <div class="grid grid-cols-6 gap-2 p-4 max-h-96 overflow-y-auto">
    <button
      v-for="iconName in icons"
      :key="iconName"
      @click="emit('update:modelValue', `/icons/${iconName}.svg`)"
      class="p-2 rounded-lg border"
      :class="{
        'border-blue-500 bg-blue-50': modelValue === `/icons/${iconName}.svg`,
        'border-gray-200 hover:border-gray-300': modelValue !== `/icons/${iconName}.svg`
      }"
    >
      <img
        :src="`/icons/${iconName}.svg`"
        :alt="iconName"
        class="w-6 h-6 mx-auto"
      />
    </button>
  </div>
</template>
