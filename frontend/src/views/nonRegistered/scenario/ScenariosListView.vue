<script setup lang="ts">
import { useGetAllScenarios } from '@/api/generated/scenario/scenario'
import { useRouter } from 'vue-router'
import { ref, onMounted } from 'vue'
import {
  LightbulbIcon,
  AlertTriangle,
  Info,
  HeartPulse,
  ArrowRight
} from 'lucide-vue-next'

const { data: scenarios, isLoading, error } = useGetAllScenarios()
const router = useRouter()

const goToScenario = (id: string) => {
  router.push(`/scenario/${id}`)
}

// Choose icon based on scenario title
const getScenarioIcon = (title: string) => {
  if (!title) return AlertTriangle

  const titleLower = title.toLowerCase()
  if (titleLower.includes('strømbrudd')) return LightbulbIcon
  if (titleLower.includes('flom') || titleLower.includes('oversvømmelse')) return Info
  if (titleLower.includes('pandemi') || titleLower.includes('sykdom')) return HeartPulse

  return AlertTriangle // Default icon
}

// Function to get a formatted preview of the content
const getContentPreview = (content: string) => {
  if (!content) return '';

  // Strip out any markdown or HTML for clean preview
  const cleanContent = content.replace(/### |##|#|\*\*/g, '');

  // Get first 250 characters
  return cleanContent.length > 250
    ? cleanContent.substring(0, 250) + '...'
    : cleanContent;
}

// Reference to the scenarios container for scrolling
const scenariosContainer = ref(null);
const showScrollIndicator = ref(false);

// Check if scrolling is needed
onMounted(() => {
  if (scenariosContainer.value) {
    const checkScroll = () => {
      if (scenariosContainer.value) {
        const { scrollHeight, clientHeight } = scenariosContainer.value;
        showScrollIndicator.value = scrollHeight > clientHeight;
      }
    };

    // Check initial state
    setTimeout(checkScroll, 500);

    // Add resize listener
    window.addEventListener('resize', checkScroll);
  }
});
</script>

<template>
  <div class="relative">
    <!-- Background decoration - wave pattern -->
    <div class="absolute top-0 left-0 w-full h-64 bg-blue-50 -z-10 overflow-hidden">
      <div class="absolute bottom-0 left-0 right-0">
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1440 320" class="w-full h-auto">
          <path fill="#ffffff" fill-opacity="1" d="M0,128L48,144C96,160,192,192,288,186.7C384,181,480,139,576,138.7C672,139,768,181,864,170.7C960,160,1056,96,1152,85.3C1248,75,1344,117,1392,138.7L1440,160L1440,320L1392,320C1344,320,1248,320,1152,320C1056,320,960,320,864,320C768,320,672,320,576,320C480,320,384,320,288,320C192,320,96,320,48,320L0,320Z"></path>
        </svg>
      </div>
    </div>

    <div class="container mx-auto px-4 py-12">
      <div class="text-center mb-12">
        <h1 class="text-3xl font-bold text-gray-800 mb-4">Alle krisescenarioer</h1>
        <p class="text-gray-600 max-w-3xl mx-auto">
          Bla gjennom alle scenarioer for å lære mer om ulike krisesituasjoner og hvordan du kan forberede deg.
        </p>
      </div>

      <div v-if="isLoading" class="text-center py-8">
        <div class="inline-block animate-spin rounded-full h-8 w-8 border-t-2 border-b-2 border-blue-500 mb-4"></div>
        <p class="text-gray-600">Laster scenarioer...</p>
      </div>

      <div v-else-if="error" class="text-center py-8">
        <p class="text-gray-600">Kunne ikke laste scenarioer. Prøv igjen senere.</p>
      </div>


      <!-- Scenarios list with max height and scrolling on desktop -->
      <div
        ref="scenariosContainer"
        v-else-if="scenarios?.length"
        class="grid md:grid-cols-2 lg:grid-cols-3 gap-6 md:gap-8 md:max-h-[800px] md:overflow-y-auto md:pr-2 md:pb-4 scrollbar-thin scrollbar-thumb-blue-500/50 scrollbar-track-blue-100/70"
      >
        <div
          v-for="(scenario) in scenarios"
          :key="scenario.id"
          class="bg-white rounded-lg shadow-md overflow-hidden cursor-pointer hover:shadow-lg transition relative min-h-[180px] flex flex-col"
          @click="goToScenario(scenario.id)"
        >
          <!-- Blue corner decoration - same for all cards -->
          <div class="absolute top-0 right-0 w-16 h-16 rounded-bl-full -mt-1 -mr-1 overflow-hidden z-0 bg-blue-50/70">
            <div class="absolute top-2.5 right-2.5">
              <component :is="getScenarioIcon(scenario.title)" class="h-6 w-6 text-blue-500" />
            </div>
          </div>

         <div class="p-6 relative z-10 flex-grow flex flex-col">
            <h2 class="font-semibold text-gray-800 text-lg mb-2">{{ scenario.title }}</h2>
            <p class="text-gray-500 text-s mb-4 flex-grow overflow-hidden">
              <span class="line-clamp-4" v-html="getContentPreview(scenario.content)"></span>
            </p>
            <div class="flex items-center text-blue-600 font-medium hover:underline group mt-auto">
              Les mer
              <ArrowRight class="h-5 w-5 ml-1 transform transition group-hover:translate-x-1" />
            </div>
          </div>
        </div>
      </div>

      <div v-else class="text-center py-8">
        <p class="text-gray-600">Ingen scenarioer tilgjengelig for øyeblikket.</p>
      </div>
    </div>
  </div>
</template>

<style>
@layer utilities {
  .line-clamp-4 {
    display: -webkit-box;
    -webkit-line-clamp: 4;
    -webkit-box-orient: vertical;
  }
}
</style>
