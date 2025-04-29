<script setup lang="ts">
import { ref } from 'vue'
import { Switch } from '@/components/ui/switch'

const props = defineProps<{
  notifications: boolean
  emailUpdates: boolean
  locationSharing: boolean
}>()

const emit = defineEmits<{
  'update:notifications': [value: boolean]
  'update:emailUpdates': [value: boolean]
  'update:locationSharing': [value: boolean]
}>()

const notificationsRef = ref(props.notifications)
const emailUpdatesRef = ref(props.emailUpdates)
const locationSharingRef = ref(props.locationSharing)

const handleToggle = (field: 'notifications' | 'emailUpdates' | 'locationSharing', value: boolean) => {
  console.log(`Toggling ${field} to ${value}`)
  if (field === 'notifications') {
    notificationsRef.value = value
    emit('update:notifications', value)
  } else if (field === 'emailUpdates') {
    emailUpdatesRef.value = value
    emit('update:emailUpdates', value)
  } else if (field === 'locationSharing') {
    locationSharingRef.value = value
    emit('update:locationSharing', value)
  }
}
</script>

<template>
  <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
    <h2 class="text-xl font-semibold text-gray-800 mb-6">Innstillinger</h2>

    <div class="space-y-4">
      <!-- Notifications toggle -->
      <div class="flex items-center justify-between">
        <div>
          <h3 class="text-sm font-medium text-gray-700">Varslinger</h3>
          <p class="text-sm text-gray-500">Motta varslinger om viktige hendelser</p>
        </div>
        <Switch
          :model-value="notificationsRef"
          @update:model-value="(value: boolean) => handleToggle('notifications', value)"
        />
      </div>

      <!-- Email updates toggle -->
      <div class="flex items-center justify-between">
        <div>
          <h3 class="text-sm font-medium text-gray-700">E-postoppdateringer</h3>
          <p class="text-sm text-gray-500">Motta ukentlige oppdateringer på e-post</p>
        </div>
        <Switch
          :model-value="emailUpdatesRef"
          @update:model-value="(value: boolean) => handleToggle('emailUpdates', value)"
        />
      </div>

      <!-- Location sharing toggle -->
      <div class="flex items-center justify-between">
        <div>
          <h3 class="text-sm font-medium text-gray-700">Del lokasjon</h3>
          <p class="text-sm text-gray-500">Tillat appen å bruke din lokasjon</p>
        </div>
        <Switch
          :model-value="locationSharingRef"
          @update:model-value="(value: boolean) => handleToggle('locationSharing', value)"
        />
      </div>
    </div>
  </div>
</template>
