<script lang="ts" setup>
import { ref } from 'vue'
import { uploadImage } from '@/api/images'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'

const props = withDefaults(
  defineProps<{
    modelValue?: string
    folder?: string
    label?: string
  }>(),
  {
    modelValue: '',
    folder: 'krisefikser',
    label: 'Bilde',
  },
)

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
}>()

const isUploading = ref(false)
const uploadError = ref('')

const handleFileChange = async (event: Event) => {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) {
    return
  }

  uploadError.value = ''
  isUploading.value = true
  try {
    const uploaded = await uploadImage(file, props.folder)
    emit('update:modelValue', uploaded.url)
  } catch {
    uploadError.value = 'Kunne ikke laste opp bildet. Proev igjen.'
  } finally {
    isUploading.value = false
    input.value = ''
  }
}
</script>

<template>
  <div class="space-y-2">
    <label class="text-sm font-medium text-gray-700">{{ label }}</label>
    <Input accept="image/*" type="file" @change="handleFileChange" />
    <p v-if="isUploading" class="text-sm text-gray-500">Laster opp bilde...</p>
    <p v-if="uploadError" class="text-sm text-red-600">{{ uploadError }}</p>

    <div v-if="modelValue" class="rounded-md border p-2">
      <img :src="modelValue" alt="Forhaandsvisning" class="h-40 w-full object-cover rounded" />
      <div class="mt-2 flex justify-end">
        <Button type="button" variant="outline" @click="emit('update:modelValue', '')">
          Fjern bilde
        </Button>
      </div>
    </div>
  </div>
</template>
