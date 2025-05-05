<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'
import { useGetScenarioById, useGetAllScenarios } from '@/api/generated/scenario/scenario'
import { computed } from 'vue'

import {
  ArrowLeft,
  LightbulbIcon,
  AlertTriangle,
  Info,
  HeartPulse
} from 'lucide-vue-next'

const route = useRoute()
const router = useRouter()
const scenarioId = route.params.id as string

const { data: scenario, isLoading, error } = useGetScenarioById(scenarioId)
const { data: allScenarios } = useGetAllScenarios()

const goBack = () => {
  router.push('/scenarioer')
}
const goToScenario = (id: string) => {
  router.push(`/scenario/${id}`);
};
// Choose icon based on scenario title
const scenarioIcon = computed(() => {
  if (!scenario.value?.title) return AlertTriangle

  const title = scenario.value.title.toLowerCase()
  if (title.includes('strømbrudd')) return LightbulbIcon
  if (title.includes('flom') || title.includes('oversvømmelse')) return Info
  if (title.includes('pandemi') || title.includes('sykdom')) return HeartPulse

  return AlertTriangle // Default icon
})

// Format content to handle markdown style headers
const formattedContent = computed(() => {
  if (!scenario.value?.content) return ''

  // Convert markdown-style headers to styled HTML
  return scenario.value.content
    .replace(/### (.*?)\n/g, '<h3 class="font-bold text-gray-800 mt-8 mb-3">$1</h3>')
    .replace(/\* (.*?)(?:\n|$)/g, '<li class="ml-5 mb-2">$1</li>')
    .replace(/<li/g, '<li class="flex items-start"><span class="inline-block w-2 h-2 rounded-full bg-blue-500 mt-2 mr-2 flex-shrink-0"></span><span')
    .replace(/<\/li>/g, '</span></li>')
})

// Get two random scenarios excluding the current one
const relatedScenarios = computed(() => {
  if (!allScenarios.value || !scenario.value) return []

  // Filter out the current scenario and get two random ones
  const otherScenarios = allScenarios.value.filter(s => s.id !== scenario.value.id)
  if (otherScenarios.length <= 2) return otherScenarios

  // Shuffle array and take first two
  return [...otherScenarios]
    .sort(() => Math.random() - 0.5)
    .slice(0, 2)
})
</script>

<template>
  <div class="container mx-auto px-4 py-12 max-w-4xl">
    <!-- Background decoration - wave pattern -->
    <div class="absolute top-0 left-0 w-full h-64 bg-blue-50 -z-10 overflow-hidden">
      <div class="absolute bottom-0 left-0 right-0">
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1440 320" class="w-full h-auto">
          <path fill="#ffffff" fill-opacity="1" d="M0,128L48,144C96,160,192,192,288,186.7C384,181,480,139,576,138.7C672,139,768,181,864,170.7C960,160,1056,96,1152,85.3C1248,75,1344,117,1392,138.7L1440,160L1440,320L1392,320C1344,320,1248,320,1152,320C1056,320,960,320,864,320C768,320,672,320,576,320C480,320,384,320,288,320C192,320,96,320,48,320L0,320Z"></path>
        </svg>
      </div>
    </div>

    <button class="mb-6 text-blue-600 hover:underline font-medium flex items-center" @click="goBack">
      <ArrowLeft class="h-5 w-5 mr-1" /> Tilbake til alle scenarioer
    </button>

    <div v-if="isLoading" class="text-center py-8">
      <div class="inline-block animate-spin rounded-full h-8 w-8 border-t-2 border-b-2 border-blue-500 mb-4"></div>
      <p class="text-gray-600">Laster scenario...</p>
    </div>

    <div v-else-if="error" class="text-center py-8">
      <p class="text-gray-600">Kunne ikke laste scenario. Prøv igjen senere.</p>
    </div>

    <div v-else-if="scenario" class="bg-white rounded-lg shadow-lg p-8 relative">
      <!-- Decorative element in top corner -->
      <div class="absolute top-0 right-0 w-32 h-32 bg-blue-50 rounded-bl-full -mt-2 -mr-2 overflow-hidden z-0">
        <div class="absolute top-5 right-5">
          <component :is="scenarioIcon" class="h-10 w-10 text-blue-500" />
        </div>
      </div>

      <!-- Scenario content -->
      <div class="relative z-10">
        <h1 class="text-2xl sm:text-3xl font-bold text-gray-800 mb-6 border-b pb-4">{{ scenario.title }}</h1>

        <div class="text-gray-700 leading-relaxed" v-html="formattedContent"></div>
      </div>

      <!-- Tips box at the bottom -->
      <div class="mt-10 bg-green-50 p-5 rounded-lg border border-green-200">
        <h3 class="font-semibold text-green-800 mb-2 flex items-center">
          <Info class="h-5 w-5 mr-2" /> Nyttige råd
        </h3>
        <p class="text-green-700">
          Det er viktig å forberede seg før en krise oppstår. Del denne informasjonen med familie
          og venner for å øke bevisstheten om kriseberedskap i ditt nærmiljø.
        </p>
      </div>
    </div>

    <div v-else class="text-center py-8">
      <p class="text-gray-600">Scenario ikke funnet.</p>
    </div>

    <!-- Related scenarios section -->
    <div v-if="scenario && relatedScenarios.length > 0" class="mt-10">
      <h2 class="text-xl sm:text-2xl font-bold text-gray-800 mb-4">Andre scenarioer</h2>
      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
      <div
          v-for="relatedScenario in relatedScenarios"
          :key="relatedScenario.id"
          class="bg-white p-6 rounded-lg shadow-md hover:shadow-lg transition cursor-pointer"
          @click="goToScenario(relatedScenario.id)"
        >
          <h3 class="font-semibold text-gray-800 mb-2">{{ relatedScenario.title }}</h3>
          <p class="text-gray-600 mb-3 line-clamp-2" v-html="relatedScenario.content?.slice(0, 120)"></p>
          <button class="text-blue-600 font-medium hover:underline inline-flex items-center">
            Les mer
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 ml-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
            </svg>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>


/* Add gentle animation to the icon */
@keyframes pulse-gentle {
  0% { transform: scale(1); }
  50% { transform: scale(1.05); }
  100% { transform: scale(1); }
}

.absolute.top-5.right-5 {
  animation: pulse-gentle 3s infinite ease-in-out;
}

/* Line clamp utility */
.line-clamp-2 {
  overflow: hidden;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}
</style>
