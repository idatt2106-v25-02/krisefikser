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

