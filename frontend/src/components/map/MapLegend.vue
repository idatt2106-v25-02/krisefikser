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
  <div class="absolute top-5 right-5 z-10 flex flex-col gap-2.5">
    <div class="bg-white p-2.5 rounded-md shadow-md max-w-[280px]">
      <h3 class="mt-0 mb-2.5 text-base font-bold">Legend</h3>

      <div class="mb-2">
        <div class="font-semibold mb-1">Map Points:</div>
        <div class="flex items-center mb-2">
          <div class="w-5 h-5 mr-2.5 bg-contain bg-no-repeat shelter-icon"></div>
          <span>Emergency Shelter</span>
        </div>
      </div>

      <div class="mb-2">
        <div class="font-semibold mb-1">Events:</div>
        <div class="flex items-center mb-1">
          <div class="w-5 h-5 mr-2.5 bg-[#4CAF50] rounded-full opacity-60"></div>
          <span>Green Level (Informational)</span>
        </div>
        <div class="flex items-center mb-1">
          <div class="w-5 h-5 mr-2.5 bg-[#FFC107] rounded-full opacity-60"></div>
          <span>Yellow Level (Warning)</span>
        </div>
        <div class="flex items-center mb-1">
          <div class="w-5 h-5 mr-2.5 bg-[#F44336] rounded-full opacity-60"></div>
          <span>Red Level (Danger)</span>
        </div>
      </div>

      <div class="text-xs text-gray-600 mt-2">
        <div>Event opacity indicates status:</div>
        <div>Low = Upcoming, High = Ongoing, Faded = Finished</div>
      </div>
    </div>

    <button
      v-if="userLocationAvailable"
      class="py-2.5 px-4 bg-[#4CAF50] text-white border-none rounded-md cursor-pointer font-bold hover:bg-[#45a049]"
      @click="toggleUserLocation"
    >
      {{ showUserLocation ? 'Hide My Location' : 'Show My Location' }}
    </button>

    <div v-if="userInCrisisZone" class="bg-[#F44336] text-white p-2.5 rounded-md font-bold text-center animate-blink">
      ⚠️ Warning: You are in a crisis zone!
    </div>
  </div>
</template>

<style scoped>
.shelter-icon {
  background-image: url('data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIyNCIgaGVpZ2h0PSIyNCIgdmlld0JveD0iMCAwIDI0IDI0IiBmaWxsPSJub25lIiBzdHJva2U9IiMwMDAwMDAiIHN0cm9rZS13aWR0aD0iMiIgc3Ryb2tlLWxpbmVjYXA9InJvdW5kIiBzdHJva2UtbGluZWpvaW49InJvdW5kIj48cGF0aCBkPSJNMyA5bDkgLTcgOSA3djExYTIgMiAwIDAgMSAtMiAyaC0xNGEyIDIgMCAwIDEgLTIgLTJ6Ij48L3BhdGg+PHBvbHlsaW5lIHBvaW50cz0iOSAyMiA5IDEyIDE1IDEyIDE1IDIyIj48L3BvbHlsaW5lPjwvc3ZnPg==');
}

@keyframes blink {
  0% { opacity: 1; }
  50% { opacity: 0.5; }
  100% { opacity: 1; }
}

.animate-blink {
  animation: blink 1s infinite;
}
</style>
