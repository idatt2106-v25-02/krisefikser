<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import type { MapPointType } from '@/api/generated/model'
import IconPicker from './IconPicker.vue'

const props = defineProps<{
  modelValue: Partial<MapPointType>
  title?: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: Partial<MapPointType>): void
  (e: 'submit'): void
  (e: 'cancel'): void
}>()

const formData = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

watch(() => props.modelValue, (newValue) => {
  formData.value = { ...newValue }
}, { deep: true })

function handleSubmit() {
  emit('submit')
}

function handleCancel() {
  emit('cancel')
}
</script>

<template>
  <div class="space-y-4">
    <h2 v-if="title" class="text-xl font-semibold">{{ title }}</h2>

    <div class="space-y-2">
      <label class="block text-sm font-medium">Tittel</label>
      <input
        v-model="formData.title"
        type="text"
        class="w-full rounded-md border p-2"
        placeholder="Skriv inn tittel"
      />
    </div>

    <div class="space-y-2">
      <label class="block text-sm font-medium">Beskrivelse</label>
      <textarea
        v-model="formData.description"
        class="w-full rounded-md border p-2"
        placeholder="Skriv inn beskrivelse"
      />
    </div>

    <div class="space-y-2">
      <label class="block text-sm font-medium">Ikon</label>
      <IconPicker v-model="formData.iconUrl" />
    </div>

    <div class="space-y-2">
      <label class="block text-sm font-medium">Åpningstid</label>
      <input
        v-model="formData.openingTime"
        type="text"
        class="w-full rounded-md border p-2"
        placeholder="Skriv inn åpningstid"
      />
    </div>

    <div class="flex justify-end space-x-2">
      <button
        @click="handleCancel"
        class="px-4 py-2 text-gray-600 hover:text-gray-800"
      >
        Avbryt
      </button>
      <button
        @click="handleSubmit"
        class="px-4 py-2 bg-primary text-white rounded-md hover:bg-primary/90"
      >
        Lagre
      </button>
    </div>
  </div>
</template>
