<script lang="ts">
export default {
  name: 'ContentCard',
  props: {
    title: {
      type: String,
      default: '',
    },
    iconComponent: {
      type: [Object, Function], // Allow both Object and Function types
      default: null,
    },
    iconColor: {
      type: String as () => 'blue' | 'yellow' | 'green',
      default: 'blue', // blue, yellow, green
    },
    fullWidth: {
      type: Boolean,
      default: false,
    },
  },
  computed: {
    iconBackgroundClass(): string {
      const colors: Record<'blue' | 'yellow' | 'green', string> = {
        blue: 'bg-blue-50',
        yellow: 'bg-yellow-50',
        green: 'bg-green-50',
      }
      return colors[this.iconColor]
    },
    iconTextClass(): string {
      const colors: Record<'blue' | 'yellow' | 'green', string> = {
        blue: 'text-blue-600',
        yellow: 'text-yellow-600',
        green: 'text-green-600',
      }
      return colors[this.iconColor]
    },
  },
}
</script>
<template>
  <div :class="{ 'mb-8': fullWidth }" class="bg-white rounded-lg shadow-md p-6">
    <div v-if="title || iconComponent" class="flex items-center mb-4">
      <div v-if="iconComponent" :class="[iconBackgroundClass, 'p-2 rounded-full mr-3']">
        <component :is="iconComponent" :class="iconTextClass" class="h-6 w-6" />
      </div>
      <h3 v-if="title" class="text-xl font-semibold text-gray-800">{{ title }}</h3>
    </div>

    <slot></slot>
  </div>
</template>
