<script setup lang="ts">
import { ref } from 'vue'

type AdminTab = 'map-point-types' | 'map-points' | 'events'

const props = defineProps<{
  activeTab: AdminTab
}>()

const emit = defineEmits<{
  (e: 'update:activeTab', tab: AdminTab): void
}>()

const tabs: { id: AdminTab; label: string }[] = [
  { id: 'map-point-types', label: 'Kartpunkttyper' },
  { id: 'map-points', label: 'Kartpunkter' },
  { id: 'events', label: 'Hendelser' },
]
</script>

<template>
  <div class="w-128 bg-white border-r border-gray-200 p-4 overflow-y-auto">
    <h2 class="text-2xl font-bold mb-6">Kartstyring</h2>

    <!-- Tab Navigation -->
    <div class="flex space-x-2 mb-6">
      <button
        v-for="tab in tabs"
        :key="tab.id"
        @click="emit('update:activeTab', tab.id)"
        :class="[
          'px-4 py-2 rounded-lg',
          activeTab === tab.id
            ? 'bg-primary text-white'
            : 'bg-gray-100 text-gray-700 hover:bg-gray-200',
        ]"
      >
        {{ tab.label }}
      </button>
    </div>

    <slot></slot>
  </div>
</template>
