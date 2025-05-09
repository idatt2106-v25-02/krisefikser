<script setup lang="ts">
import NewMapComponent from '@/components/map/NewMapComponent.vue'
</script>

<template>
  <div class="flex flex-col h-[calc(100vh-64px)] overflow-hidden">
    <div class="relative flex-grow overflow-hidden">
      <NewMapComponent />
    </div>
  </div>
</template>
