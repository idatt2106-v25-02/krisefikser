<!-- eslint-disable vue/multi-word-component-names -->
<script setup lang="ts">
import { computed } from 'vue'
import { cva } from 'class-variance-authority'
import { XIcon } from 'lucide-vue-next'
import { cn } from '@/lib/utils'

const props = defineProps<{
  class?: string
  variant?: 'default' | 'destructive' | 'warning'
}>()

const toastVariants = cva(
  'group pointer-events-auto relative flex w-full items-center justify-between space-x-4 overflow-hidden rounded-md border p-6 pr-8 shadow-lg transition-all data-[swipe=cancel]:translate-x-0 data-[swipe=end]:translate-x-[var(--radix-toast-swipe-end-x)] data-[swipe=move]:translate-x-[var(--radix-toast-swipe-move-x)] data-[swipe=move]:transition-none data-[state=open]:animate-in data-[state=closed]:animate-out data-[swipe=end]:animate-out data-[state=closed]:fade-out-80 data-[state=closed]:slide-out-to-right-full data-[state=open]:slide-in-from-top-full',
  {
    variants: {
      variant: {
        default: 'border bg-background',
        destructive: 'destructive group border-destructive bg-destructive text-white',
        warning: 'border-yellow-500 bg-yellow-50 text-yellow-800',
      },
    },
    defaultVariants: {
      variant: 'default',
    },
  },
)

const variantClass = computed(() => {
  return toastVariants({ variant: props.variant })
})
</script>

<template>
  <div :class="cn(variantClass, props.class)">
    <slot />
    <slot name="close">
      <button
        class="absolute right-2 top-2 rounded-md p-1 text-foreground/50 opacity-0 transition-opacity hover:text-foreground focus:opacity-100 focus:outline-none focus:ring-2 group-hover:opacity-100 group-[.destructive]:text-red-300 group-[.destructive]:hover:text-red-50 group-[.destructive]:focus:ring-red-400 group-[.destructive]:focus:ring-offset-red-600"
      >
        <XIcon class="h-4 w-4" />
      </button>
    </slot>
  </div>
</template>
