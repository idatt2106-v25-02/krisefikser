<script setup lang="ts">
import { onMounted, watch } from 'vue';
import L from 'leaflet';

// Define props
const props = defineProps<{
  map: any; // Using any to avoid Leaflet type issues
  crisisAreas: Array<{
    id: number;
    name: string;
    center: [number, number];
    radius: number;
    severity: number;
    color: string;
    description: string;
  }>;
}>();

const circles: L.Circle[] = [];

// Add crisis areas to map
function addCrisisAreas() {
  if (!props.map) return;

  // Clear previous circles
  for (const circle of circles) {
    circle.remove();
  }
  circles.length = 0;

  // Add crisis areas
  for (const area of props.crisisAreas) {
    const circle = L.circle(area.center, {
      radius: area.radius,
      color: area.color,
      fillColor: area.color,
      fillOpacity: 0.2
    }).addTo(props.map)
      .bindPopup(`
        <strong>${area.name}</strong><br>
        <span style="color:${area.color}">Severity Level: ${area.severity}</span><br>
        ${area.description}
      `);
    
    circles.push(circle);
  }
}

onMounted(() => {
  if (props.map) {
    addCrisisAreas();
  }
});

watch(() => props.map, (newMap) => {
  if (newMap) {
    addCrisisAreas();
  }
});
</script>

<template>
  <!-- This component doesn't have a visual template as it's just adding layers to the map -->
</template>

<style scoped>
/* No specific styles needed */
</style> 