<script setup lang="ts">
import { ref } from 'vue'
import type { Event } from '@/api/generated/model'
import { EventLevel, EventStatus } from '@/api/generated/model'
import { useCreateEvent, useGetAllEvents, useUpdateEvent, useDeleteEvent } from '@/api/generated/event/event'
import { useAuthStore } from '@/stores/useAuthStore'

const authStore = useAuthStore()
const { data: events, refetch: refetchEvents } = useGetAllEvents()

const newEvent = ref<Partial<Event>>({
  title: '',
  description: '',
  radius: 500,
  latitude: 63.4305,
  longitude: 10.3951,
  level: EventLevel.GREEN,
  startTime: new Date().toISOString(),
  status: EventStatus.UPCOMING,
})
const editingEvent = ref<Event | null>(null)

const { mutate: createEvent } = useCreateEvent()
const { mutate: updateEvent } = useUpdateEvent()
const { mutate: deleteEvent } = useDeleteEvent()

const emit = defineEmits<{
  (e: 'map-click', lat: number, lng: number): void
}>()

function handleMapClick(lat: number, lng: number) {
  newEvent.value.latitude = lat
  newEvent.value.longitude = lng
  emit('map-click', lat, lng)
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
    await createEvent({
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
    refetchEvents()
    newEvent.value = {
      title: '',
      description: '',
      radius: 500,
      latitude: 63.4305,
      longitude: 10.3951,
      level: EventLevel.GREEN,
      startTime: new Date().toISOString(),
      status: EventStatus.UPCOMING,
    }
  } catch (error) {
    console.error('Error creating event:', error)
  }
}

async function handleUpdateEvent() {
  if (!editingEvent.value?.id) return

  try {
    await updateEvent({
      id: editingEvent.value.id,
      data: editingEvent.value,
    })
    refetchEvents()
    editingEvent.value = null
  } catch (error) {
    console.error('Error updating event:', error)
  }
}

async function handleDeleteEvent(id: number) {
  try {
    await deleteEvent({ id })
    refetchEvents()
  } catch (error) {
    console.error('Error deleting event:', error)
  }
}
</script>

<template>
  <div class="space-y-4">
    <div class="space-y-2">
      <label class="text-sm font-medium">Navn</label>
      <input
        v-model="newEvent.title"
        type="text"
        class="w-full px-3 py-2 border rounded-lg"
        placeholder="Navn på hendelse"
      />
    </div>

    <div class="space-y-2">
      <label class="text-sm font-medium">Beskrivelse</label>
      <textarea
        v-model="newEvent.description"
        class="w-full px-3 py-2 border rounded-lg"
        rows="3"
        placeholder="Beskriv hendelsen"
      ></textarea>
    </div>

    <div class="space-y-2">
      <label class="text-sm font-medium">Radius (meter)</label>
      <input
        v-model="newEvent.radius"
        type="number"
        class="w-full px-3 py-2 border rounded-lg"
        placeholder="Radius i meter"
      />
    </div>

    <div class="space-y-2">
      <label class="text-sm font-medium">Alvorlighetsgrad</label>
      <select v-model="newEvent.level" class="w-full px-3 py-2 border rounded-lg">
        <option :value="EventLevel.GREEN">Lav</option>
        <option :value="EventLevel.YELLOW">Middels</option>
        <option :value="EventLevel.RED">Høy</option>
      </select>
    </div>

    <div class="space-y-2">
      <label class="text-sm font-medium">Status</label>
      <select v-model="newEvent.status" class="w-full px-3 py-2 border rounded-lg">
        <option :value="EventStatus.UPCOMING">Kommende</option>
        <option :value="EventStatus.ONGOING">Pågående</option>
        <option :value="EventStatus.FINISHED">Avsluttet</option>
      </select>
    </div>

    <div class="space-y-2">
      <label class="text-sm font-medium">Start tidspunkt</label>
      <input
        v-model="newEvent.startTime"
        type="datetime-local"
        class="w-full px-3 py-2 border rounded-lg"
      />
    </div>

    <div class="space-y-2">
      <label class="text-sm font-medium">Slutt tidspunkt (valgfritt)</label>
      <input
        v-model="newEvent.endTime"
        type="datetime-local"
        class="w-full px-3 py-2 border rounded-lg"
      />
    </div>

    <div class="space-y-2">
      <label class="text-sm font-medium">Plassering</label>
      <div class="flex space-x-2">
        <input
          :value="newEvent.latitude?.toFixed(4)"
          readonly
          type="text"
          class="w-1/2 px-3 py-2 border rounded-lg bg-gray-50"
          placeholder="Breddegrad"
        />
        <input
          :value="newEvent.longitude?.toFixed(4)"
          readonly
          type="text"
          class="w-1/2 px-3 py-2 border rounded-lg bg-gray-50"
          placeholder="Lengdegrad"
        />
      </div>
      <p class="text-sm text-gray-500">Klikk på kartet for å velge plassering</p>
    </div>

    <button
      @click="handleAddEvent"
      class="w-full bg-primary text-white px-4 py-2 rounded-lg hover:bg-primary/90"
    >
      Legg til krisehendelse
    </button>

    <!-- List of Events -->
    <div class="mt-6 space-y-4">
      <h3 class="text-lg font-semibold">Eksisterende hendelser</h3>
      <div v-for="event in events" :key="event?.id" class="border rounded-lg p-4">
        <div class="flex justify-between items-start">
          <div>
            <h4 class="font-medium">{{ event?.title || 'Ukjent tittel' }}</h4>
            <p class="text-sm text-gray-600">{{ event?.description || 'Ingen beskrivelse' }}</p>
            <p class="text-sm text-gray-600">
              {{ event?.latitude?.toFixed(4) }}, {{ event?.longitude?.toFixed(4) }}
            </p>
          </div>
          <div class="flex space-x-2">
            <button
              @click="editingEvent = { ...event }"
              class="text-primary hover:text-primary/80"
            >
              Rediger
            </button>
            <button
              v-if="event?.id"
              @click="handleDeleteEvent(event.id)"
              class="text-red-500 hover:text-red-600"
            >
              Slett
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
