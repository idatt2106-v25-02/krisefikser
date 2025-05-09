<script setup lang="ts">
import { ArrowRight } from 'lucide-vue-next'
import { Button } from '@/components/ui/button'
import { RouterLink as Link } from 'vue-router'
import type { EventResponse as Event, EventResponseLevel as EventLevel } from '@/api/generated/model'

interface Props {
  events: Event[]
  isLoading: boolean
}

defineProps<Props>()

function getLevelClass(level?: EventLevel) {
  if (!level) return 'bg-gray-100 text-gray-800 border-gray-200'
  switch (level) {
    case 'RED':
      return 'bg-red-100 text-red-800 border-red-200'
    case 'YELLOW':
      return 'bg-yellow-100 text-yellow-800 border-yellow-200'
    case 'GREEN':
      return 'bg-green-100 text-green-800 border-green-200'
    default:
      return 'bg-gray-100 text-gray-800 border-gray-200'
  }
}

function getBorderClass(level?: EventLevel) {
  if (!level) return ''
  switch (level) {
    case 'RED':
      return 'border-l-4 border-l-red-500'
    case 'YELLOW':
      return 'border-l-4 border-l-yellow-500'
    case 'GREEN':
      return 'border-l-4 border-l-green-500'
    default:
      return ''
  }
}

function formatLocation(event: Event) {
  if (event.latitude && event.longitude) {
    return `${event.latitude.toFixed(4)}, ${event.longitude.toFixed(4)}`
  }
  return 'Ukjent lokasjon'
}
</script>

<template>
  <div class="bg-white rounded-lg shadow mb-6 border-t-2">
    <div class="p-4 border-b flex justify-between items-center">
      <h3 class="text-lg font-medium text-gray-800">Nylige hendelser</h3>
      <Link to="/admin/kart">
        <Button
          variant="link"
          class="text-blue-600 hover:text-blue-800 text-sm p-0 h-auto flex items-center group"
        >
          Se alle
          <ArrowRight class="h-4 w-4 ml-1 group-hover:translate-x-1 transition-transform" />
        </Button>
      </Link>
    </div>
    <div class="p-4">
      <div v-if="isLoading" class="text-center py-4">
        <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500 mx-auto"></div>
      </div>
      <div v-else-if="events.length > 0">
        <div
          v-for="event in events.slice(0, 5)"
          :key="event.id"
          class="mb-4 last:mb-0 p-3 border rounded-lg hover:shadow-sm transition-shadow"
          :class="getBorderClass(event.level)"
        >
          <div class="flex justify-between">
            <h4 class="font-medium">{{ event.title || 'Uten tittel' }}</h4>
            <span
              :class="`px-2 py-1 rounded-full text-xs font-medium ${getLevelClass(event.level)}`"
            >
              {{ event.level || 'UKJENT' }}
            </span>
          </div>
          <p class="text-gray-600 text-sm mt-1">
            {{ event.description || 'Ingen beskrivelse' }}
          </p>
          <div class="flex justify-between mt-2 text-xs text-gray-500">
            <span>{{ formatLocation(event) }}</span>
            <span>{{
              event.startTime
                ? new Date(event.startTime).toLocaleString('no-NO')
                : 'Ingen starttid'
            }}</span>
          </div>
        </div>
      </div>
      <div v-else class="text-center py-4 text-gray-500">Ingen hendelser funnet</div>
    </div>
  </div>
</template>
