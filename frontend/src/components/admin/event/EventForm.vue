<!-- EventFormComponent.vue -->
<script lang="ts" setup>
import { onMounted, ref, watch } from 'vue'
import type { EventResponse as Event } from '@/api/generated/model'
import {
  EventResponseLevel as EventLevel,
  EventResponseStatus as EventStatus,
} from '@/api/generated/model'
import router from '@/router'

const props = defineProps<{
  modelValue: Partial<Event>
  title?: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: Partial<Event>): void
  (e: 'submit'): void
  (e: 'cancel'): void
  (e: 'start-map-selection'): void
}>()

// Create a local copy that doesn't update the original
const localValue = ref<Partial<Event>>({})

// Only update localValue when props change
onMounted(() => {
  localValue.value = JSON.parse(JSON.stringify(props.modelValue))
})

watch(
  () => props.modelValue,
  (newVal) => {
    // Use deep copy to break reactivity chain
    localValue.value = JSON.parse(JSON.stringify(newVal))
  },
  { deep: true },
)

// Don't watch localValue changes to avoid two-way binding loops

function handleSubmit() {
  emit('update:modelValue', JSON.parse(JSON.stringify(localValue.value)))
  emit('submit')
  router.push({ name: 'event-detail' })
}

function handleCancel() {
  // Reset without emitting update
  localValue.value = JSON.parse(JSON.stringify(props.modelValue))
  emit('cancel')
}

function handleStartMapSelection() {
  emit('start-map-selection')
}
</script>

<template>
  <div class="space-y-4">
    <h3 v-if="title" class="text-lg font-semibold">{{ title }}</h3>

    <!-- Form fields bound to localValue only -->
    <div class="space-y-2">
      <label class="text-sm font-medium">Navn</label>
      <input
        v-model="localValue.title"
        class="w-full px-3 py-2 border rounded-lg"
        placeholder="Navn på hendelse"
        type="text"
      />
    </div>

    <!-- Rest of form fields similar to above -->
    <!-- ... -->

    <div class="space-y-2">
      <label class="text-sm font-medium">Beskrivelse</label>
      <textarea
        v-model="localValue.description"
        class="w-full px-3 py-2 border rounded-lg"
        placeholder="Beskriv hendelsen"
        rows="3"
      ></textarea>
    </div>

    <div class="space-y-2">
      <label class="text-sm font-medium">Radius (meter)</label>
      <input
        v-model="localValue.radius"
        class="w-full px-3 py-2 border rounded-lg"
        placeholder="Radius i meter"
        type="number"
      />
    </div>

    <div class="space-y-2">
      <label class="text-sm font-medium">Alvorlighetsgrad</label>
      <select v-model="localValue.level" class="w-full px-3 py-2 border rounded-lg">
        <option :value="EventLevel.GREEN">Lav</option>
        <option :value="EventLevel.YELLOW">Middels</option>
        <option :value="EventLevel.RED">Høy</option>
      </select>
    </div>

    <div class="space-y-2">
      <label class="text-sm font-medium">Status</label>
      <select v-model="localValue.status" class="w-full px-3 py-2 border rounded-lg">
        <option :value="EventStatus.UPCOMING">Kommende</option>
        <option :value="EventStatus.ONGOING">Pågående</option>
        <option :value="EventStatus.FINISHED">Avsluttet</option>
      </select>
    </div>

    <div class="space-y-2">
      <label class="text-sm font-medium">Start tidspunkt</label>
      <input
        v-model="localValue.startTime"
        class="w-full px-3 py-2 border rounded-lg"
        type="datetime-local"
      />
    </div>

    <div class="space-y-2">
      <label class="text-sm font-medium">Slutt tidspunkt (valgfritt)</label>
      <input
        v-model="localValue.endTime"
        class="w-full px-3 py-2 border rounded-lg"
        type="datetime-local"
      />
    </div>

    <div class="space-y-2">
      <label class="text-sm font-medium">Plassering</label>
      <div class="flex space-x-2">
        <input
          v-model="localValue.latitude"
          class="w-1/2 px-3 py-2 border rounded-lg"
          placeholder="Breddegrad"
          readonly
          step="0.0001"
          type="number"
        />
        <input
          v-model="localValue.longitude"
          class="w-1/2 px-3 py-2 border rounded-lg"
          placeholder="Lengdegrad"
          readonly
          step="0.0001"
          type="number"
        />
      </div>
      <button
        class="w-full mt-2 px-4 py-2 bg-primary/10 text-primary border border-primary rounded-lg hover:bg-primary/20"
        @click="handleStartMapSelection"
      >
        Velg plassering på kartet
      </button>
    </div>

    <div class="flex justify-end space-x-2">
      <button class="px-4 py-2 text-gray-600 hover:text-gray-800" @click="handleCancel">
        Avbryt
      </button>
      <button
        class="px-4 py-2 bg-primary text-white rounded-lg hover:bg-primary/90"
        @click="handleSubmit"
      >
        Lagre
      </button>
    </div>
  </div>
</template>
