<script setup lang="ts">
defineProps<{
  preferences: {
    notifications: boolean;
    emailUpdates: boolean;
    locationSharing: boolean;
  }
}>();

const emit = defineEmits(['update:preferences']);

const updatePreference = (preference: string, value: boolean) => {
  emit('update:preferences', { preference, value });
};
</script>

<template>
  <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
    <h2 class="text-xl font-semibold text-gray-800 mb-6">Innstillinger</h2>

    <div class="space-y-4">
      <div class="flex items-center justify-between">
        <div>
          <h3 class="font-medium text-gray-800">Motta varsler</h3>
          <p class="text-sm text-gray-600">Få varsler om viktige hendelser og oppdateringer</p>
        </div>
        <label class="relative inline-flex items-center cursor-pointer">
          <input
            type="checkbox"
            :checked="preferences.notifications"
            @change="updatePreference('notifications', !preferences.notifications)"
            class="sr-only peer"
          >
          <div class="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"></div>
        </label>
      </div>

      <div class="flex items-center justify-between">
        <div>
          <h3 class="font-medium text-gray-800">E-post oppdateringer</h3>
          <p class="text-sm text-gray-600">Motta e-poster om nye funksjoner og oppdateringer</p>
        </div>
        <label class="relative inline-flex items-center cursor-pointer">
          <input
            type="checkbox"
            :checked="preferences.emailUpdates"
            @change="updatePreference('emailUpdates', !preferences.emailUpdates)"
            class="sr-only peer"
          >
          <div class="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"></div>
        </label>
      </div>

      <div class="flex items-center justify-between">
        <div>
          <h3 class="font-medium text-gray-800">Posisjonsdeling</h3>
          <p class="text-sm text-gray-600">Del din posisjon for å få bedre kriseinformasjon</p>
        </div>
        <label class="relative inline-flex items-center cursor-pointer">
          <input
            type="checkbox"
            :checked="preferences.locationSharing"
            @change="updatePreference('locationSharing', !preferences.locationSharing)"
            class="sr-only peer"
          >
          <div class="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"></div>
        </label>
      </div>
    </div>
  </div>
</template>
