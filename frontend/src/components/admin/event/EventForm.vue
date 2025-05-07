<!-- EventFormComponent.vue -->
<script setup lang="ts">
import { ref, watch } from 'vue'
import type { EventResponse as Event } from '@/api/generated/model'
import {
  EventResponseLevel as EventLevel,
  EventResponseStatus as EventStatus,
} from '@/api/generated/model'

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

const localValue = ref<Partial<Event>>({ ...props.modelValue })

watch(
  () => props.modelValue,
  (newValue) => {
    localValue.value = { ...newValue }
  },
  { deep: true },
)

watch(
  localValue,
  (newValue) => {
    emit('update:modelValue', newValue)
  },
  { deep: true },
)

function handleSubmit() {
  emit('submit')
}

function handleCancel() {
  emit('cancel')
}

function handleStartMapSelection() {
  emit('start-map-selection')
}
</script>

<template>
  <div class="space-y-4">
    <h3 v-if="title" class="text-lg font-semibold">{{ title }}</h3>

    <div class="space-y-2">
      <label class="text-sm font-medium">Navn</label>
      <input
        v-model="localValue.title"
        type="text"
        class="w-full px-3 py-2 border rounded-lg"
        placeholder="Navn på hendelse"
      />
    </div>

    <div class="space-y-2">
      <label class="text-sm font-medium">Beskrivelse</label>
      <textarea
        v-model="localValue.description"
        class="w-full px-3 py-2 border rounded-lg"
        rows="3"
        placeholder="Beskriv hendelsen"
      ></textarea>
    </div>

    <div class="space-y-2">
      <label class="text-sm font-medium">Radius (meter)</label>
      <input
        v-model="localValue.radius"
        type="number"
        class="w-full px-3 py-2 border rounded-lg"
        placeholder="Radius i meter"
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
        type="datetime-local"
        class="w-full px-3 py-2 border rounded-lg"
      />
    </div>

    <div class="space-y-2">
      <label class="text-sm font-medium">Slutt tidspunkt (valgfritt)</label>
      <input
        v-model="localValue.endTime"
        type="datetime-local"
        class="w-full px-3 py-2 border rounded-lg"
      />
    </div>

    <div class="space-y-2">
      <label class="text-sm font-medium">Plassering</label>
      <div class="flex space-x-2">
        <input
          v-model="localValue.latitude"
          type="number"
          step="0.0001"
          class="w-1/2 px-3 py-2 border rounded-lg"
          placeholder="Breddegrad"
          readonly
        />
        <input
          v-model="localValue.longitude"
          type="number"
          step="0.0001"
          class="w-1/2 px-3 py-2 border rounded-lg"
          placeholder="Lengdegrad"
          readonly
        />
      </div>
      <button
        @click="handleStartMapSelection"
        class="w-full mt-2 px-4 py-2 bg-primary/10 text-primary border border-primary rounded-lg hover:bg-primary/20"
      >
        Velg plassering på kartet
      </button>
    </div>

    <div class="flex justify-end space-x-2">
      <button @click="handleCancel" class="px-4 py-2 text-gray-600 hover:text-gray-800">
        Avbryt
      </button>
      <button
        @click="handleSubmit"
        class="px-4 py-2 bg-primary text-white rounded-lg hover:bg-primary/90"
      >
        Lagre
      </button>
    </div>
  </div>
</template>
