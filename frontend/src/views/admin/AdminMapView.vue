<script setup lang="ts">
import { ref } from 'vue'
import AdminSidebar from '@/components/admin/AdminSidebar.vue'
import MapPointTypesManager from '@/components/admin/MapPointTypesManager.vue'
import MapPointsManager from '@/components/admin/MapPointsManager.vue'
import EventsManager from '@/components/admin/EventsManager.vue'
import AdminMapContainer from '@/components/admin/AdminMapContainer.vue'
import { useGetAllMapPoints } from '@/api/generated/map-point/map-point'
import { useGetAllEvents } from '@/api/generated/event/event'

// Types
type AdminTab = 'map-point-types' | 'map-points' | 'events'

// Data fetching
const { data: mapPoints } = useGetAllMapPoints()
const { data: events } = useGetAllEvents()

// Form states
const activeTab = ref<AdminTab>('map-point-types')


</script>

<template>
  <div class="flex h-screen">
    <AdminSidebar v-model:activeTab="activeTab">
      <MapPointTypesManager v-if="activeTab === 'map-point-types'" />
      <MapPointsManager
        v-if="activeTab === 'map-points'"
      />
      <EventsManager
        v-if="activeTab === 'events'"
      />
    </AdminSidebar>

    <AdminMapContainer
      :map-points="mapPoints"
      :events="events"
    />
  </div>
</template>
