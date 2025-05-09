<script setup lang="ts">
import { Map, AlertTriangle, BookOpen, FileText } from 'lucide-vue-next'
import SingleStatCard from './SingleStatCard.vue'

interface Props {
  eventsCount: number
  mapPointsCount: number
  scenariosCount: number
  articlesCount: number
}

defineProps<Props>()
</script>

<template>
  <div class="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
    <SingleStatCard
      :icon="AlertTriangle"
      title="Aktive hendelser"
      :value="eventsCount"
      to="/admin/kart"
      icon-bg-color="bg-orange-100"
      icon-color="text-orange-500"
      border-color="border-orange-200"
    />

    <SingleStatCard
      :icon="Map"
      title="Kart-elementer"
      :value="mapPointsCount"
      to="/admin/kart"
      icon-bg-color="bg-blue-100"
      icon-color="text-blue-500"
      border-color="border-blue-200"
    />

    <SingleStatCard
      :icon="BookOpen"
      title="Scenarioer"
      :value="scenariosCount"
      to="/admin/scenarios"
      icon-bg-color="bg-blue-100"
      icon-color="text-blue-600"
      border-color="border-blue-200"
    />

    <SingleStatCard
      :icon="FileText"
      title="Artikler"
      :value="articlesCount"
      to="/admin/artikler"
      icon-bg-color="bg-blue-100"
      icon-color="text-blue-600"
      border-color="border-blue-200"
    />
  </div>
</template>
