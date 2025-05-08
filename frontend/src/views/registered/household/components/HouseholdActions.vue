<script setup lang="ts">
import { ExternalLink, Trash } from 'lucide-vue-next'

defineEmits<{
  (e: 'leave'): void
  (e: 'delete'): void
}>()
</script>

<template>
  <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
    <h2 class="text-xl font-semibold text-gray-800 mb-4">Husstandshandlinger</h2>
    <div class="space-y-3">
      <button
        @click="$emit('leave')"
        class="w-full bg-white border border-gray-300 text-gray-700 py-2 px-4 rounded-md flex items-center justify-center hover:bg-gray-50"
      >
        <ExternalLink class="h-4 w-4 mr-2" />
        Forlat husstand
      </button>

      <button
        @click="$emit('delete')"
        class="w-full bg-red-100 text-red-600 py-2 px-4 rounded-md flex items-center justify-center hover:bg-red-200"
      >
        <Trash class="h-4 w-4 mr-2" />
        Slett husstand
      </button>
    </div>
  </div>
</template>
