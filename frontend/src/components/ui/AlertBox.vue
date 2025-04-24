<script lang="ts">
export default {
  name: 'AlertBox',
  props: {
    type: {
      type: String as () => 'info' | 'warning' | 'success' | 'error',
      default: 'info', // info, warning, success, error
      validator: (value: string) => ['info', 'warning', 'success', 'error'].includes(value)
    },
    iconComponent: {
      type: Object,
      required: true
    }
  },
  computed: {
    backgroundClass(): string {
      const types: Record<'info' | 'warning' | 'success' | 'error', string> = {
        info: 'bg-blue-50',
        warning: 'bg-yellow-50',
        success: 'bg-green-50',
        error: 'bg-red-50'
      };
      return types[this.type];
    },
    textClass(): string {
      const types: Record<'info' | 'warning' | 'success' | 'error', string> = {
        info: 'text-blue-800',
        warning: 'text-yellow-800',
        success: 'text-green-800',
        error: 'text-red-800'
      };
      return types[this.type];
    },
    iconClass(): string {
      const types: Record<'info' | 'warning' | 'success' | 'error', string> = {
        info: 'text-blue-600',
        warning: 'text-yellow-600',
        success: 'text-green-600',
        error: 'text-red-600'
      };
      return types[this.type];
    }
  }
}
</script>
<template>
  <div :class="[backgroundClass, 'p-4 rounded-md']">
    <div class="flex items-start">
      <component
        :is="iconComponent"
        class="h-5 w-5 mt-0.5 mr-2 flex-shrink-0"
        :class="iconClass"
      />
      <p :class="textClass">
        <slot></slot>
      </p>
    </div>
  </div>
</template>
