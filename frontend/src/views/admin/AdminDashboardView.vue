<!-- AdminDashboard.vue -->
<script setup lang="ts">
import AdminLayout from '@/components/admin/AdminLayout.vue'
import StatsCard from '@/components/admin/dashboard/StatsCard.vue'
import RecentEventsCard from '@/components/admin/dashboard/RecentEventsCard.vue'
import QuickActions from '@/components/admin/dashboard/QuickActions.vue'

import { useGetAllEvents } from '@/api/generated/event/event.ts'
import { useGetAllMapPoints } from '@/api/generated/map-point/map-point.ts'
import { useGetAllScenarios } from '@/api/generated/scenario/scenario.ts'
import { useGetAllArticles } from '@/api/generated/article/article.ts'
import { useAuthStore } from '@/stores/auth/useAuthStore.ts'

const { data: eventsData, isLoading: isLoadingEvents } = useGetAllEvents()
const { data: mapPointsData } = useGetAllMapPoints()
const { data: scenariosData } = useGetAllScenarios()
const { data: articlesData } = useGetAllArticles()

const authStore = useAuthStore()
</script>

<template>
  <AdminLayout>
    <div class="p-6">
      <h2 class="text-2xl font-bold text-gray-800 mb-6">Dashboard oversikt</h2>

      <StatsCard
        :events-count="eventsData?.length ?? 0"
        :map-points-count="mapPointsData?.length ?? 0"
        :scenarios-count="scenariosData?.length ?? 0"
        :articles-count="articlesData?.length ?? 0"
      />

      <RecentEventsCard :events="eventsData ?? []" :is-loading="isLoadingEvents" />

      <QuickActions :is-super-admin="authStore.isSuperAdmin" />
    </div>
  </AdminLayout>
</template>
