<script setup lang="ts">
import { ref, onMounted } from 'vue'
import Button from '@/components/ui/button/Button.vue'

// Define props
const props = defineProps<{
  userLocationAvailable: boolean
  showUserLocation: boolean
  userInCrisisZone: boolean
  isAddingMeetingPoint: boolean
  hasActiveHousehold: boolean
}>()

// Define emits
const emit = defineEmits(['toggleUserLocation', 'toggleMeetingPointCreation'])

// Toggle user location
function toggleUserLocation() {
  emit('toggleUserLocation', !props.showUserLocation)
}

// Toggle meeting point creation
function toggleMeetingPointCreation() {
  emit('toggleMeetingPointCreation', !props.isAddingMeetingPoint)
}

// Collapsible state
const isLegendOpen = ref(true)

// Set default open/closed based on screen width
onMounted(() => {
  if (window.innerWidth < 768) {
    isLegendOpen.value = false
  }
})

function toggleLegend() {
  isLegendOpen.value = !isLegendOpen.value
}
</script>

<template>
  <div
    data-test="legend-container"
    class="absolute bottom-4 left-4 z-10 transition-all"
  >
    <div v-if="userInCrisisZone" data-test="crisis-warning" class="bg-destructive text-destructive-foreground p-4 rounded-md mb-4">
      <p class="font-medium">Advarsel: Du er i en krisesone!</p>
      <p class="text-sm mt-1">Vær oppmerksom på lokale instruksjoner og hold deg oppdatert.</p>
    </div>
    <div class="absolute top-5 right-5 z-10 flex flex-col gap-2.5">
      <Button
        variant="outlineBlue"
        class="self-end mb-1 min-w-[180px] w-full md:w-auto"
        @click="toggleLegend"
        aria-label="Toggle tegnforklaring"
      >
        <span class="text-xs font-medium">Tegnforklaring</span>
        <svg
          :class="[isLegendOpen ? 'rotate-0' : '-rotate-90', 'transition-transform h-4 w-4']"
          fill="none"
          stroke="currentColor"
          stroke-width="2"
          viewBox="0 0 24 24"
        >
          <path stroke-linecap="round" stroke-linejoin="round" d="M6 9l6 6 6-6" />
        </svg>
      </Button>
      <transition name="fade">
        <div v-if="isLegendOpen" class="bg-white p-2.5 rounded-md shadow-md max-w-[280px]">
          <h3 class="mt-0 mb-2.5 text-base font-bold">Tegnforklaring</h3>
          <div class="mb-2">
            <div class="font-semibold mb-1">Lokasjon:</div>
            <div class="flex items-center mb-1">
              <div class="w-5 h-5 mr-2.5 bg-blue-500 rounded-full flex items-center justify-center">
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  class="h-3 w-3 text-white"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"
                  />
                </svg>
              </div>
              <span>Min posisjon</span>
            </div>
            <div class="flex items-center mb-1">
              <div class="w-5 h-5 mr-2.5 bg-red-500 rounded-full flex items-center justify-center">
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  class="h-3 w-3 text-white"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"
                  />
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
        </div>
      </transition>
      <Button
        v-if="userLocationAvailable"
        variant="secondary"
        class="min-w-[180px] w-full md:w-auto"
        @click="toggleUserLocation"
      >
        {{ showUserLocation ? 'Skjul min posisjon' : 'Vis min posisjon' }}
      </Button>
      <Button
        v-if="hasActiveHousehold"
        variant="default"
        :disabled="isAddingMeetingPoint"
        class="min-w-[180px] w-full md:w-auto"
        @click="toggleMeetingPointCreation"
      >
        {{ isAddingMeetingPoint ? 'Klikk på kartet' : 'Legg til møtepunkt' }}
      </Button>
    </div>
  </div>
</template>

<style scoped>
@keyframes fade {
  0% {
    opacity: 0;
  }
  100% {
    opacity: 1;
  }
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

@keyframes blink {
  0% { opacity: 1; }
  50% { opacity: 0.5; }
  100% { opacity: 1; }
}
</style>
