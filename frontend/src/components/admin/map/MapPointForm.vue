<script lang="ts" setup>
import { ref, watch } from 'vue'
import type {
  MapPointResponse as MapPoint,
  MapPointTypeResponse as MapPointType,
} from '@/api/generated/model'
import { useGetAllMapPointTypes } from '@/api/generated/map-point-type/map-point-type'

const props = defineProps<{
  modelValue: Partial<MapPoint>
  title?: string
}>()

const emit = defineEmits<{
  (e: 'submit'): void
  (e: 'cancel'): void
  (e: 'start-map-selection'): void
  (e: 'update:modelValue', value: Partial<MapPoint>): void
}>()

const { data: mapPointTypes } = useGetAllMapPointTypes<MapPointType[]>()

const localValue = ref<Partial<MapPoint>>({ ...props.modelValue })

watch(
  () => props.modelValue,
  (newValue) => {
    localValue.value = {
      ...localValue.value,
      latitude: newValue.latitude,
      longitude: newValue.longitude,
    }
  },
  { deep: true },
)

function handleSubmit() {
  emit('update:modelValue', JSON.parse(JSON.stringify(localValue.value)))
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
      <label class="text-sm font-medium">Kartpunkttype</label>
      <select v-model="localValue.type" class="w-full px-3 py-2 border rounded-lg">
        <option :value="undefined">Velg type</option>
        <option v-for="type in mapPointTypes" :key="type?.id" :value="type">
          {{ type?.title || 'Ukjent tittel' }}
        </option>
      </select>
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
