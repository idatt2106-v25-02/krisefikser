<script lang="ts" setup>
import { ref } from 'vue'
import type { EventResponse as Event } from '@/api/generated/model'
import {
  EventResponseLevel as EventLevel,
  EventResponseStatus as EventStatus,
} from '@/api/generated/model'
import {
  useCreateEvent,
  useDeleteEvent,
  useGetAllEvents,
  useUpdateEvent,
} from '@/api/generated/event/event.ts'
import { useGetAllMapPoints } from '@/api/generated/map-point/map-point.ts'
import { useAuthStore } from '@/stores/auth/useAuthStore.ts'
import EventForm from './EventForm.vue'
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog'
import { useRouter } from 'vue-router'

const authStore = useAuthStore()
const router = useRouter()
// Use staleTime to prevent too frequent refetches
const { data: events, refetch: refetchEvents } = useGetAllEvents({
  query: {
    staleTime: 1000, // 1 second stale time to prevent immediate refetches
  },
})
const { refetch: refetchMapPoints } = useGetAllMapPoints({
  query: {
    staleTime: 1000,
  },
})

const createEventMutation = useCreateEvent({
  mutation: {
    onSuccess: () => {
      refetchEvents()
      refetchMapPoints()
    },
  },
})

const updateEventMutation = useUpdateEvent({
  mutation: {
    onSuccess: () => {
      refetchEvents()
      refetchMapPoints()
    },
  },
})

const deleteEventMutation = useDeleteEvent({
  mutation: {
    onSuccess: () => {
      refetchEvents()
      refetchMapPoints()
    },
  },
})

const newEvent = ref({
  title: '',
  description: '',
  radius: 500,
  latitude: 63.4305,
  longitude: 10.3951,
  level: EventLevel.GREEN,
  startTime: new Date().toISOString(),
  endTime: undefined,
  status: EventStatus.UPCOMING,
})
const editingEvent = ref<Event | null>(null)
const isDialogOpen = ref(false)
const isMapSelectionMode = ref(false)

const emit = defineEmits<{
  (e: 'map-click', lat: number, lng: number): void
  (e: 'map-selection-mode-change', isActive: boolean): void
}>()

function _handleMapClick(lat: number, lng: number) {
  if (!isMapSelectionMode.value) return

  if (editingEvent.value) {
    editingEvent.value.latitude = lat
    editingEvent.value.longitude = lng
  } else {
    newEvent.value.latitude = lat
    newEvent.value.longitude = lng
  }
  isMapSelectionMode.value = false
  emit('map-selection-mode-change', false)
}

function handleStartMapSelection() {
  isMapSelectionMode.value = true
  emit('map-selection-mode-change', true)
}

async function handleAddEvent() {
  if (!authStore.isAdmin) {
    console.error('User must have ADMIN role to create an event')
    return
  }

  if (!newEvent.value.title || !newEvent.value.latitude || !newEvent.value.longitude) {
    console.error('Missing required event fields')
    return
  }

  try {
    const response = await createEventMutation.mutateAsync({
      data: {
        title: newEvent.value.title,
        description: newEvent.value.description,
        radius: newEvent.value.radius,
        latitude: newEvent.value.latitude,
        longitude: newEvent.value.longitude,
        level: newEvent.value.level,
        startTime: newEvent.value.startTime,
        endTime: newEvent.value.endTime,
        status: newEvent.value.status,
      },
    })

    // Reset form
    newEvent.value = {
      title: '',
      description: '',
      radius: 500,
      latitude: 63.4305,
      longitude: 10.3951,
      level: EventLevel.GREEN,
      startTime: new Date().toISOString(),
      endTime: undefined,
      status: EventStatus.UPCOMING,
    }

    // Route to the new event's detail page
    if (response.id) {
      router.push(`/kriser/${response.id}`)
    }
  } catch (error) {
    console.error('Error creating event:', error)
  }
}

async function handleUpdateEvent() {
  if (!authStore.isAdmin) {
    console.error('User must have ADMIN role to update an event')
    return
  }

  if (!editingEvent.value?.id) {
    console.error('ID is required')
    return
  }

  try {
    await updateEventMutation.mutateAsync({
      id: editingEvent.value.id,
      data: {
        title: editingEvent.value.title,
        description: editingEvent.value.description,
        radius: editingEvent.value.radius,
        latitude: editingEvent.value.latitude,
        longitude: editingEvent.value.longitude,
        level: editingEvent.value.level,
        startTime: editingEvent.value.startTime,
        endTime: editingEvent.value.endTime,
        status: editingEvent.value.status,
      },
    })

    editingEvent.value = null
    isDialogOpen.value = false
  } catch (error) {
    console.error('Error updating event:', error)
  }
}

async function handleDeleteEvent(id: number) {
  try {
    await deleteEventMutation.mutateAsync({ id })
  } catch (error) {
    console.error('Error deleting event:', error)
  }
}

function handleEditClick(event: Event | undefined) {
  if (!event) return
  editingEvent.value = { ...event }
  isDialogOpen.value = true
}

function handleDialogCancel() {
  editingEvent.value = null
  isDialogOpen.value = false
}

// Modify the @cancel handler in the template to use a local function that breaks reactivity
function resetNewEvent() {
  newEvent.value = {
    title: '',
    description: '',
    radius: 500,
    latitude: 63.4305,
    longitude: 10.3951,
    level: EventLevel.GREEN,
    startTime: new Date().toISOString(),
    endTime: undefined,
    status: EventStatus.UPCOMING,
  }
}
</script>

<template>
  <div class="space-y-4">
    <!-- Add Form -->
    <EventForm
      v-model="newEvent"
      title="Legg til ny krisehendelse"
      @cancel="resetNewEvent"
      @submit="handleAddEvent"
      @start-map-selection="handleStartMapSelection"
    />

    <!-- List of Events -->
    <div class="mt-6 space-y-4">
      <h3 class="text-lg font-semibold">Eksisterende hendelser</h3>
      <div v-for="event in events" :key="event.id" class="border rounded-lg p-4">
        <div class="flex justify-between items-start">
          <div>
            <h4 class="font-medium">{{ event.title || 'Ukjent tittel' }}</h4>
            <p class="text-sm text-gray-600">{{ event.description || 'Ingen beskrivelse' }}</p>
            <p class="text-sm text-gray-600">
              {{ event.latitude?.toFixed(4) }}, {{ event.longitude?.toFixed(4) }}
            </p>
          </div>
          <div class="flex space-x-2">
            <button class="text-primary hover:text-primary/80" @click="handleEditClick(event)">
              Rediger
            </button>
            <button
              v-if="event.id"
              class="text-red-500 hover:text-red-600"
              @click="handleDeleteEvent(event.id)"
            >
              Slett
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Edit Dialog -->
    <Dialog v-model:open="isDialogOpen">
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Rediger krisehendelse</DialogTitle>
        </DialogHeader>
        <EventForm
          v-if="editingEvent"
          v-model="editingEvent"
          title=""
          @cancel="handleDialogCancel"
          @submit="handleUpdateEvent"
          @start-map-selection="handleStartMapSelection"
        />
      </DialogContent>
    </Dialog>
  </div>
</template>
