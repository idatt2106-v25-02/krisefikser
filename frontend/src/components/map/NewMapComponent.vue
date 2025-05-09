<script setup lang="ts">
import { onMounted } from 'vue'

const props = defineProps<{
  initMap: () => void
}>()

onMounted(() => {
  props.initMap()
})
</script>

<template>
  <div id="map" class="w-full h-full z-[1] overflow-hidden"></div>
</template>
