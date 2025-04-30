<!-- EditEventCard.vue -->
<script setup lang="ts">
import { XCircle, Save } from 'lucide-vue-next'

// Import shadcn components
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardFooter, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { ref, watch } from 'vue'

// Define props and interface
interface Event {
  id: string
  title: string
  description: string
  level: string
  location: string
  timestamp: string
  active: boolean
}

const props = defineProps<{
  event: Event | null
}>()

const localEvent = ref<Event | null>(null)

watch(
  () => props.event,
  (newEvent) => {
    if (newEvent) {
      localEvent.value = { ...newEvent }
    } else {
      localEvent.value = null
    }
  },
  { immediate: true },
)

// Define emits
const emit = defineEmits(['save', 'cancel'])

// Convert ISO string to datetime-local format for input
const formatDateTimeForInput = (isoString: string) => {
  const date = new Date(isoString)
  return date.toISOString().slice(0, 16) // Format: YYYY-MM-DDThh:mm
}

// Handle save
const handleSave = () => {
  emit('save', props.event)
}

// Handle cancel
const handleCancel = () => {
  emit('cancel')
}
</script>

<template>
  <Card v-if="localEvent" class="mb-6">
    <CardHeader>
      <CardTitle>Rediger hendelse</CardTitle>
    </CardHeader>
    <CardContent>
      <form class="space-y-4" @submit.prevent="handleSave">
        <div class="space-y-2">
          <Label for="title">Tittel</Label>
          <Input id="title" v-model="localEvent.title" placeholder="Hendelsestitel" />
        </div>

        <div class="space-y-2">
          <Label for="description">Beskrivelse</Label>
          <Textarea
            id="description"
            v-model="localEvent.description"
            placeholder="Beskriv hendelsen"
          />
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div class="space-y-2">
            <Label for="level">Nivå</Label>
            <select
              id="level"
              v-model="localEvent.level"
              class="w-full border rounded-lg px-3 py-2 text-gray-700"
            >
              <option value="rød">Rød</option>
              <option value="gul">Gul</option>
              <option value="grønn">Grønn</option>
            </select>
          </div>

          <div class="space-y-2">
            <Label for="status">Status</Label>
            <select
              id="status"
              v-model="localEvent.active"
              class="w-full border rounded-lg px-3 py-2 text-gray-700"
            >
              <option :value="true">Aktiv</option>
              <option :value="false">Inaktiv</option>
            </select>
          </div>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div class="space-y-2">
            <Label for="location">Plassering</Label>
            <Input id="location" v-model="localEvent.location" placeholder="Sted" />
          </div>

          <div class="space-y-2">
            <Label for="timestamp">Tidspunkt</Label>
            <Input
              id="timestamp"
              type="datetime-local"
              v-model="localEvent.timestamp"
              :value="formatDateTimeForInput(localEvent.timestamp)"
            />
          </div>
        </div>
      </form>
    </CardContent>
    <CardFooter class="flex justify-end space-x-2">
      <Button variant="outline" @click="handleCancel">
        <XCircle class="h-4 w-4 mr-1" />
        Avbryt
      </Button>
      <Button @click="handleSave">
        <Save class="h-4 w-4 mr-1" />
        Lagre
      </Button>
    </CardFooter>
  </Card>
</template>
