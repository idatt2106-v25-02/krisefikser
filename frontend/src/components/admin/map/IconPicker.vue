<script lang="ts" setup>
import { onMounted, ref } from 'vue'
import { getIconNames } from '@/utils/icons'

interface Props {
  modelValue: string | undefined
}

withDefaults(defineProps<Props>(), {
  modelValue: '',
})

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
}>()

const icons = ref<string[]>([])

onMounted(() => {
  icons.value = getIconNames()
})
</script>

<template>
  <div class="grid grid-cols-6 gap-2 p-4 max-h-96 overflow-y-auto">
    <button
      v-for="iconName in icons"
      :key="iconName"
      :class="{
        'border-blue-500 bg-blue-50': modelValue === `/icons/${iconName}.svg`,
        'border-gray-200 hover:border-gray-300': modelValue !== `/icons/${iconName}.svg`,
      }"
      class="p-2 rounded-lg border"
      @click="emit('update:modelValue', `/icons/${iconName}.svg`)"
    >
      <img :alt="iconName" :src="`/icons/${iconName}.svg`" class="w-6 h-6 mx-auto" />
    </button>
  </div>
</template>
