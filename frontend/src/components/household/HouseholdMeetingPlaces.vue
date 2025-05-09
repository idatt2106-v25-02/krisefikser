<script setup lang="ts">
import { computed, ref } from 'vue'
import { useGetMeetingPoints } from '@/api/generated/meeting-points/meeting-points'
import { AlertCircle, Map as MapIcon } from 'lucide-vue-next'
import Dialog from '@/components/ui/dialog/Dialog.vue'
import DialogContent from '@/components/ui/dialog/DialogContent.vue'
import DialogHeader from '@/components/ui/dialog/DialogHeader.vue'
import DialogTitle from '@/components/ui/dialog/DialogTitle.vue'
import DialogDescription from '@/components/ui/dialog/DialogDescription.vue'
import HouseholdMeetingMap from '@/components/household/HouseholdMeetingMap.vue'

// Define interface for MeetingPlace (only fields from backend)
interface MeetingPlace {
  id?: string
  name?: string
  description?: string
  latitude?: number
  longitude?: number
  householdId?: string
}

const props = defineProps<{
  householdId: string
  householdLatitude: number
  householdLongitude: number
}>()

// Only fetch if householdId is present
const { data: meetingPlaces, refetch: _refetch } = useGetMeetingPoints(props.householdId, {
  query: { refetchOnWindowFocus: true, refetchInterval: 10000 },
})

const refreshMeetingPlaces = () => {
  _refetch()
}

const isMapDialogOpen = ref(false)
const mapRef = ref<InstanceType<typeof HouseholdMeetingMap> | null>(null)
const selectedMeetingPlace = ref<MeetingPlace | null>(null)

const meetingPlacesList = computed(() =>
  Array.isArray(meetingPlaces.value) ? meetingPlaces.value : [],
)

function handleMeetingPlaceSelected(place: MeetingPlace) {
  selectedMeetingPlace.value = place
}

function viewMeetingPlace(placeId: string) {
  const place = meetingPlacesList.value.find((p) => p.id === placeId)
  if (place) {
    selectedMeetingPlace.value = place
    isMapDialogOpen.value = true
  }
}

defineExpose({
  refreshMeetingPlaces,
})
</script>

<template>
  <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mb-6">
    <div v-if="!props.householdId" class="text-red-600 font-bold text-center py-4">
      Feil: householdId mangler. Kan ikke vise møteplasser.
    </div>
    <template v-else>
      <div class="flex justify-between items-center mb-4">
        <h2 class="text-xl font-semibold text-gray-800">Møteplasser</h2>
        <router-link
          to="/kart"
          class="inline-flex items-center gap-1 px-3 py-2 rounded border border-blue-500 text-blue-600 hover:bg-blue-50 transition text-sm"
        >
          <MapIcon class="h-4 w-4" />
          <span>Vis kart</span>
        </router-link>
      </div>
      <div v-if="meetingPlacesList.length === 0" class="text-gray-500 text-center py-4">
        Ingen møteplasser registrert.
      </div>
      <div v-else class="divide-y divide-gray-100">
        <div v-for="place in meetingPlacesList" :key="place.id ?? ''" class="p-4">
          <div class="flex items-start">
            <div class="flex-shrink-0 mr-3">
              <div class="h-10 w-10 rounded-full flex items-center justify-center bg-blue-500">
                <AlertCircle class="h-5 w-5 text-white" />
              </div>
            </div>
            <div class="flex-1">
              <h4 class="font-medium text-gray-800">{{ place.name ?? 'Uten navn' }}</h4>
              <p class="text-sm text-gray-600">
                {{ place.description ?? 'Ingen beskrivelse' }}
              </p>
            </div>
          </div>
        </div>
      </div>
      <!-- Meeting Places Map Dialog -->
      <Dialog v-model:open="isMapDialogOpen" class="max-w-[1250px] w-[95vw]">
        <DialogContent class="sm:max-w-5xl h-auto">
          <DialogHeader>
            <DialogTitle>Møteplasser ved krise</DialogTitle>
            <DialogDescription>
              Kartet viser dine møteplasser ved krisesituasjoner. Klikk på markørene for mer
              informasjon og veibeskrivelse.
            </DialogDescription>
          </DialogHeader>
          <div class="py-6 px-2">
            <HouseholdMeetingMap
              ref="mapRef"
              :meeting-places="
                meetingPlacesList.map((place) => ({
                  ...place,
                  position: [place.latitude, place.longitude],
                })) as any[]
              "
              :household-position="[householdLatitude, householdLongitude]"
              @meeting-place-selected="handleMeetingPlaceSelected"
              class="min-h-[625px]"
            />
            <div class="mt-4 flex gap-3">
              <div
                v-for="place in meetingPlacesList"
                :key="String(place.id)"
                class="px-3 py-2 rounded-md text-sm cursor-pointer border flex-1 bg-blue-50 border-blue-200 text-blue-800"
                @click="viewMeetingPlace(String(place.id))"
              >
                <div class="font-medium">{{ (place.name ?? '').split(':')[0] }}</div>
              </div>
            </div>
          </div>
        </DialogContent>
      </Dialog>
    </template>
  </div>
</template>
