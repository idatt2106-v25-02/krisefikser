<script setup lang="ts">
import { useToast } from '@/components/ui/toast/use-toast'
import Toast from './toast.vue'

const { toasts } = useToast()
</script>

<template>
  <div
    class="fixed top-0 z-[100] flex max-h-screen w-full flex-col-reverse gap-2 p-4 sm:bottom-0 sm:right-0 sm:top-auto sm:flex-col md:max-w-[420px]"
  >
    <TransitionGroup name="toast">
      <Toast v-for="toast in toasts" :key="toast.id" :class="toast.class" :variant="toast.variant">
        <div class="flex w-full flex-1 items-start gap-4">
          <div class="flex-1 space-y-1">
            <div v-if="toast.title" class="text-sm font-semibold">
              {{ toast.title }}
            </div>
            <div v-if="toast.description" class="text-sm opacity-90">
              {{ toast.description }}
            </div>
          </div>
        </div>
        <template #close>
          <button
            @click="toast.dismiss"
            class="absolute right-2 top-2 rounded-md p-1 text-foreground/50 opacity-0 transition-opacity hover:text-foreground focus:opacity-100 focus:outline-none focus:ring-2 group-hover:opacity-100 group-[.destructive]:text-red-300 group-[.destructive]:hover:text-red-50 group-[.destructive]:focus:ring-red-400 group-[.destructive]:focus:ring-offset-red-600"
          >
            <span class="sr-only">Close</span>
            <XIcon class="h-4 w-4" />
          </button>
        </template>
      </Toast>
    </TransitionGroup>
  </div>
</template>

<style>
.toast-enter-active,
.toast-leave-active {
  transition: all 0.3s ease;
}

.toast-enter-from {
  transform: translateX(100%);
  opacity: 0;
}

.toast-leave-to {
  transform: translateX(100%);
  opacity: 0;
}
</style>
