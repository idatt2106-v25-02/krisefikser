<!-- CrisisScenariosSection.vue -->
<script setup lang="ts">
import { computed } from 'vue'
import { useGetAllScenarios } from '@/api/generated/scenario/scenario'
import { useRouter } from 'vue-router'

const { data: scenarios, isLoading, error } = useGetAllScenarios()
const router = useRouter()

const latestScenarios = computed(() => {
  if (!scenarios?.value) return []
  // Sort by createdAt if available, else by title
  return [...scenarios.value]
    .sort((a, b) => {
      if (a.createdAt && b.createdAt) {
        return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
      }
      return a.title.localeCompare(b.title)
    })
    .slice(0, 3)
})

const goToAllScenarios = () => {
  router.push('/scenarioer')
}

const goToScenario = (id) => {
  router.push(`/scenario/${id}`)
}
</script>

<template>
  <section class="mb-16">
    <!-- Title centered -->
    <div class="text-center mb-4">
      <h2 class="text-3xl font-bold text-gray-800">Krisescenarioer</h2>
    </div>

    <!-- Description centered and "Se alle" button on same line -->
    <div class="relative mb-8">
      <!-- Centered paragraph -->
      <p class="text-gray-600 max-w-3xl mx-auto text-center">
        Lær om ulike krisesituasjoner og hvordan du best kan forberede deg.
      </p>

      <!-- Absolutely positioned button -->
      <a @click="goToAllScenarios"
         class="text-blue-600 font-medium hover:underline inline-flex items-center cursor-pointer whitespace-nowrap absolute right-0 top-0">
        Se alle scenarioer
        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 ml-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
        </svg>
      </a>
    </div>

    <div v-if="isLoading" class="text-center py-8">
      <div class="inline-block animate-spin rounded-full h-8 w-8 border-t-2 border-b-2 border-blue-500 mb-4"></div>
      <p class="text-gray-600">Laster scenarioer...</p>
    </div>

    <div v-else-if="error" class="text-center py-8">
      <p class="text-gray-600">Kunne ikke laste scenarioer. Prøv igjen senere.</p>
    </div>

    <div v-else-if="latestScenarios.length" class="grid md:grid-cols-3 gap-8">
      <div
        v-for="scenario in latestScenarios"
        :key="scenario.id"
        class="bg-white p-6 rounded-lg shadow-md transition hover:shadow-lg cursor-pointer"
        @click="goToScenario(scenario.id)"
      >
        <h3 class="text-xl font-semibold text-gray-800 mb-2">{{ scenario.title }}</h3>
        <div class="text-gray-600 mb-4 line-clamp-4" v-html="scenario.content?.slice(0, 120)"></div>
        <!-- Using same arrow style as in the info section -->
        <router-link :to="`/scenario/${scenario.id}`" class="text-blue-600 font-medium hover:underline inline-flex items-center">
          Les mer
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 ml-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
          </svg>
        </router-link>
      </div>
    </div>

    <div v-else class="text-center py-8">
      <p class="text-gray-600">Ingen scenarioer tilgjengelig for øyeblikket.</p>
    </div>
  </section>
</template>

<style scoped>
.line-clamp-4 {
  display: -webkit-box;
  -webkit-line-clamp: 4;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
