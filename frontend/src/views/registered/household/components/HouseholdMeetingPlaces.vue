<script setup lang="ts">
import { ref } from 'vue'
import { Button } from '@/components/ui/button'
import { AlertCircle, Map as MapIcon } from 'lucide-vue-next'
import Dialog from '@/components/ui/dialog/Dialog.vue'
import DialogContent from '@/components/ui/dialog/DialogContent.vue'
import DialogHeader from '@/components/ui/dialog/DialogHeader.vue'
import DialogTitle from '@/components/ui/dialog/DialogTitle.vue'
import DialogDescription from '@/components/ui/dialog/DialogDescription.vue'
import HouseholdMeetingMap from '@/components/household/HouseholdMeetingMap.vue'

interface MeetingPlace {
  id: string
  name: string
  address: string
  latitude: number
  longitude: number
  description?: string
  type: 'primary' | 'secondary'
  targetDays: number
}

const props = defineProps<{
  meetingPlaces: MeetingPlace[]
  householdLatitude: number
  householdLongitude: number
}>()

const emit = defineEmits<{
  (e: 'meetingPlaceSelected', place: MeetingPlace): void
}>()

const isMapDialogOpen = ref(false)
const mapRef = ref<InstanceType<typeof HouseholdMeetingMap> | null>(null)
const selectedMeetingPlace = ref<MeetingPlace | null>(null)

function handleMeetingPlaceSelected(place: MeetingPlace) {
  selectedMeetingPlace.value = place
  emit('meetingPlaceSelected', place)
}

function viewMeetingPlace(placeId: string) {
  isMapDialogOpen.value = true
  const place = props.meetingPlaces.find(p => p.id === placeId)
  if (place) {
    selectedMeetingPlace.value = place
  }
}
</script>

<template>
  <div class="bg-white rounded-lg shadow-sm border border-gray-200">
    <div class="p-5 border-b">
      <div class="flex justify-between items-center">
        <h2 class="text-xl font-semibold text-gray-800">Møteplasser</h2>
        <Button
          variant="outline"
          size="sm"
          class="flex items-center gap-1"
          @click="isMapDialogOpen = true"
        >
          <MapIcon class="h-4 w-4" />
          <span>Vis kart</span>
        </Button>
      </div>
    </div>

    <div class="divide-y divide-gray-100">
      <div v-for="place in meetingPlaces" :key="place.id" class="p-4">
        <div class="flex items-start">
          <div class="flex-shrink-0 mr-3">
            <div
              :class="[
                'h-10 w-10 rounded-full flex items-center justify-center',
                place.type === 'primary' ? 'bg-red-500' : 'bg-orange-400',
              ]"
            >
              <AlertCircle class="h-5 w-5 text-white" />
            </div>
          </div>
          <div class="flex-1">
            <h4 class="font-medium text-gray-800">{{ place.name }}</h4>
            <p class="text-sm text-gray-600">{{ place.address }}</p>
            <button
              class="mt-2 text-sm text-blue-600 hover:text-blue-800 flex items-center"
              @click="viewMeetingPlace(place.id)"
            >
              <MapIcon class="h-3 w-3 mr-1" />
              Vis i kart
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Meeting Places Map Dialog -->
    <Dialog v-model:open="isMapDialogOpen" class="meeting-map-dialog">
      <DialogContent class="sm:max-w-5xl h-auto">
        <DialogHeader>
          <DialogTitle>Møteplasser ved krise</DialogTitle>
          <DialogDescription>
            Kartet viser dine møteplasser ved krisesituasjoner. Klikk på markørene for mer informasjon
            og veibeskrivelse.
          </DialogDescription>
        </DialogHeader>

        <div class="py-6 px-2">
          <HouseholdMeetingMap
            ref="mapRef"
            :meeting-places="meetingPlaces.map(place => ({
              ...place,
              position: [place.latitude, place.longitude],
            }))"
            :household-position="[householdLatitude, householdLongitude]"
            @meeting-place-selected="handleMeetingPlaceSelected"
            class="min-h-[625px]"
          />

          <div class="mt-4 flex gap-3">
            <div
              v-for="place in meetingPlaces"
              :key="place.id"
              :class="[
                'px-3 py-2 rounded-md text-sm cursor-pointer border flex-1',
                place.type === 'primary'
                  ? 'bg-red-50 border-red-200 text-red-800'
                  : 'bg-orange-50 border-orange-200 text-orange-800',
              ]"
              @click="viewMeetingPlace(place.id)"
            >
              <div class="font-medium">{{ place.name.split(':')[0] }}</div>
            </div>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  </div>
</template>

<style scoped>
:deep(.meeting-map-dialog) {
  max-width: 1250px;
  width: 95vw;
}
</style>
