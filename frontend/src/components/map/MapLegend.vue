<script setup lang="ts">
// Define props
const props = defineProps<{
  userLocationAvailable: boolean;
  showUserLocation: boolean;
  userInCrisisZone: boolean;
}>();

// Define emits
const emit = defineEmits(['toggleUserLocation']);

// Toggle user location
function toggleUserLocation() {
  emit('toggleUserLocation', !props.showUserLocation);
}
</script>

<template>
  <div class="map-controls">
    <div class="legend">
      <h3>Legend</h3>
      <div class="legend-item">
        <div class="legend-icon shelter-icon"></div>
        <span>Emergency Shelter</span>
      </div>
      <div class="legend-item">
        <div class="legend-icon crisis-low"></div>
        <span>Low Severity (Level 1)</span>
      </div>
      <div class="legend-item">
        <div class="legend-icon crisis-medium"></div>
        <span>Medium Severity (Level 2)</span>
      </div>
      <div class="legend-item">
        <div class="legend-icon crisis-high"></div>
        <span>High Severity (Level 3)</span>
      </div>
    </div>
    
    <button 
      v-if="userLocationAvailable" 
      class="location-btn" 
      @click="toggleUserLocation"
    >
      {{ showUserLocation ? 'Hide My Location' : 'Show My Location' }}
    </button>
    
    <div v-if="userInCrisisZone" class="crisis-alert">
      ⚠️ Warning: You are in a crisis zone!
    </div>
  </div>
</template>

<style scoped>
.map-controls {
  position: absolute;
  top: 20px;
  right: 20px;
  z-index: 10;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.legend {
  background-color: white;
  padding: 10px;
  border-radius: 5px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  max-width: 250px;
}

.legend h3 {
  margin-top: 0;
  margin-bottom: 10px;
  font-size: 16px;
}

.legend-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.legend-icon {
  width: 20px;
  height: 20px;
  margin-right: 10px;
}

.shelter-icon {
  background-image: url('data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIyNCIgaGVpZ2h0PSIyNCIgdmlld0JveD0iMCAwIDI0IDI0IiBmaWxsPSJub25lIiBzdHJva2U9IiMwMDAwMDAiIHN0cm9rZS13aWR0aD0iMiIgc3Ryb2tlLWxpbmVjYXA9InJvdW5kIiBzdHJva2UtbGluZWpvaW49InJvdW5kIj48cGF0aCBkPSJNMyA5bDkgLTcgOSA3djExYTIgMiAwIDAgMSAtMiAyaC0xNGEyIDIgMCAwIDEgLTIgLTJ6Ij48L3BhdGg+PHBvbHlsaW5lIHBvaW50cz0iOSAyMiA5IDEyIDE1IDEyIDE1IDIyIj48L3BvbHlsaW5lPjwvc3ZnPg==');
  background-size: contain;
  background-repeat: no-repeat;
}

.crisis-low {
  background-color: #FFC107;
  border-radius: 50%;
}

.crisis-medium {
  background-color: #FF9800;
  border-radius: 50%;
}

.crisis-high {
  background-color: #F44336;
  border-radius: 50%;
}

.location-btn {
  padding: 10px 15px;
  background-color: #4CAF50;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-weight: bold;
}

.location-btn:hover {
  background-color: #45a049;
}

.crisis-alert {
  background-color: #F44336;
  color: white;
  padding: 10px;
  border-radius: 5px;
  font-weight: bold;
  text-align: center;
  animation: blink 1s infinite;
}

@keyframes blink {
  0% { opacity: 1; }
  50% { opacity: 0.5; }
  100% { opacity: 1; }
}
</style> 