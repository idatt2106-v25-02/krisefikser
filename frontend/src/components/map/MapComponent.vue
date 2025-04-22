<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import 'leaflet/dist/leaflet.css';
import L from 'leaflet';

// Fix for the marker icon issue in Leaflet
import { Marker } from 'leaflet';
import markerIcon2x from 'leaflet/dist/images/marker-icon-2x.png';
import markerIcon from 'leaflet/dist/images/marker-icon.png';
import markerShadow from 'leaflet/dist/images/marker-shadow.png';

// Trondheim center coordinates
const TRONDHEIM_CENTER: [number, number] = [63.4305, 10.3951];

// Map instance ref
const mapInstance = ref<L.Map | null>(null);

// Define props
const props = defineProps<{
  initialZoom?: number;
}>();

// Define emits
const emit = defineEmits(['map-created']);

// Fix Leaflet default icon issue
// biome-ignore lint/suspicious/noExplicitAny: <explanation>
// biome-ignore lint/complexity/useArrowFunction: <explanation>
(Marker.prototype as any)._getIconUrl = function () {
  return '';
};

Marker.mergeOptions({
  icon: L.icon({
    iconUrl: markerIcon,
    iconRetinaUrl: markerIcon2x,
    shadowUrl: markerShadow,
    iconSize: [25, 41],
    iconAnchor: [12, 41],
    popupAnchor: [1, -34],
    tooltipAnchor: [16, -28],
    shadowSize: [41, 41]
  })
});

// Initialize map
onMounted(() => {
  try {
    // Create map
    const map = L.map('map').setView(TRONDHEIM_CENTER, props.initialZoom || 13);
    mapInstance.value = map;
    
    // Add OpenStreetMap tiles
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
    }).addTo(map);
    
    // Emit map created event
    emit('map-created', map);
  } catch (error) {
    console.error('Error initializing map', error);
  }
});

// Cleanup
onUnmounted(() => {
  if (mapInstance.value) {
    mapInstance.value.remove();
  }
});

// Define exposed properties
defineExpose({
  mapInstance
});
</script>

<template>
  <div id="map" class="w-full h-full z-[1]"></div>
</template>

<style scoped>
/* No styles needed as we're using Tailwind classes */
</style> 