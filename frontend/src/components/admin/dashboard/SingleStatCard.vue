<script setup lang="ts">
import { type Component } from 'vue'
import { RouterLink as Link } from 'vue-router'

interface Props {
  icon: Component
  title: string
  value: number
  to: string
  iconBgColor?: string
  iconColor?: string
  borderColor?: string
}

defineProps<Props>()
</script>

<template>
  <Link
    :to="to"
    class="bg-white rounded-lg shadow p-6 transition-all hover:shadow-md cursor-pointer transform hover:-translate-y-1 border-t-4"
    :class="borderColor || 'border-blue-200'"
  >
    <div class="flex items-center">
      <div :class="`${iconBgColor || 'bg-blue-100'} p-3 rounded-full`">
        <component :is="icon" class="h-6 w-6" :class="iconColor || 'text-blue-500'" />
      </div>
      <div class="ml-4">
        <h3 class="text-gray-500 text-sm">{{ title }}</h3>
        <p class="text-2xl font-bold text-gray-800">{{ value }}</p>
      </div>
    </div>
  </Link>
</template>
