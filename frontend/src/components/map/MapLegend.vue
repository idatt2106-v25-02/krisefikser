<script setup lang="ts">
// Define props
const props = defineProps<{
  userLocationAvailable: boolean;
  showUserLocation: boolean;
  userInCrisisZone: boolean;
  isAddingMeetingPoint: boolean;
  hasActiveHousehold: boolean;
}>();

// Define emits
const emit = defineEmits(['toggleUserLocation', 'toggleMeetingPointCreation']);

// Toggle user location
function toggleUserLocation() {
  emit('toggleUserLocation', !props.showUserLocation);
}

// Toggle meeting point creation
function toggleMeetingPointCreation() {
  emit('toggleMeetingPointCreation', !props.isAddingMeetingPoint);
}
</script>

<template>
  <div class="absolute top-5 right-5 z-10 flex flex-col gap-2.5">
    <div class="bg-white p-2.5 rounded-md shadow-md max-w-[280px]">
      <h3 class="mt-0 mb-2.5 text-base font-bold">Tegnforklaring</h3>

      <div class="mb-2">
        <div class="font-semibold mb-1">Lokasjon:</div>
        <div class="flex items-center mb-1">
          <div class="w-5 h-5 mr-2.5 bg-blue-500 rounded-full flex items-center justify-center">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-3 w-3 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
            </svg>
          </div>
          <span>Min posisjon</span>
        </div>
        <div class="flex items-center mb-1">
          <div class="w-5 h-5 mr-2.5 bg-red-500 rounded-full flex items-center justify-center">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-3 w-3 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" />
            </svg>
          </div>
          <span>Hjem</span>
        </div>
      </div>

      <div class="mb-2">
        <div class="font-semibold mb-1">Hendelser:</div>
        <div class="flex items-center mb-1">
          <div class="w-5 h-5 mr-2.5 bg-[#4CAF50] rounded-full opacity-60"></div>
          <span>Grønt nivå (Informasjon)</span>
        </div>
        <div class="flex items-center mb-1">
          <div class="w-5 h-5 mr-2.5 bg-[#FFC107] rounded-full opacity-60"></div>
          <span>Gult nivå (Advarsel)</span>
        </div>
        <div class="flex items-center mb-1">
          <div class="w-5 h-5 mr-2.5 bg-[#F44336] rounded-full opacity-60"></div>
          <span>Rødt nivå (Fare)</span>
        </div>
      </div>

      <div class="text-xs text-gray-600 mt-2">
        <div>Hendelsens gjennomsiktighet indikerer status:</div>
        <div>Lav = Kommende, Høy = Pågående, Svak = Avsluttet</div>
      </div>
    </div>

    <button
      v-if="userLocationAvailable"
      class="py-2.5 px-4 bg-[#4CAF50] text-white border-none rounded-md cursor-pointer font-bold hover:bg-[#45a049]"
      @click="toggleUserLocation"
    >
      {{ showUserLocation ? 'Skjul min posisjon' : 'Vis min posisjon' }}
    </button>

    <button
      v-if="hasActiveHousehold"
      class="py-2.5 px-4 bg-blue-500 text-white border-none rounded-md cursor-pointer font-bold hover:bg-blue-600"
      @click="toggleMeetingPointCreation"
      :class="{ 'bg-blue-600': isAddingMeetingPoint }"
    >
      {{ isAddingMeetingPoint ? 'Klikk på kartet' : 'Legg til møtepunkt' }}
    </button>

    <div v-if="userInCrisisZone" class="bg-[#F44336] text-white p-2.5 rounded-md font-bold text-center animate-blink">
      ⚠️ Advarsel: Du er i en krisesone!
    </div>
  </div>
</template>
<style scoped>

@keyframes blink {
  0% { opacity: 1; }
  50% { opacity: 0.5; }
  100% { opacity: 1; }
}

.animate-blink {
  animation: blink 1s infinite;
}
</style>

