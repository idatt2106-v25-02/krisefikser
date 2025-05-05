<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'
import { useGetScenarioById } from '@/api/generated/scenario/scenario'
import { computed, onMounted } from 'vue'

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

const goBack = () => {
  router.push('/scenarioer')
}

// Choose icon based on scenario title
const scenarioIcon = computed(() => {
  if (!scenario.value?.title) return AlertTriangle

  const title = scenario.value.title.toLowerCase()
  if (title.includes('strømbrudd')) return LightbulbIcon
  if (title.includes('flom') || title.includes('oversvømmelse')) return Info
  if (title.includes('pandemi') || title.includes('sykdom')) return HeartPulse

  return AlertTriangle // Default icon
})

// Format content to handle rich text formatting
const formattedContent = computed(() => {
  if (!scenario.value?.content) return ''

  // The content is in HTML format from the rich text editor
  // We'll ensure it's properly sanitized but preserves formatting
  let content = scenario.value.content

  // Ensure proper rendering of lists
  content = content.replace(/<li>/g, '<li style="display: list-item; margin: 0.5em 0;">')
  content = content.replace(/<ol>/g, '<ol style="list-style-type: decimal; padding-left: 2em; margin: 1em 0;">')
  content = content.replace(/<ul>/g, '<ul style="list-style-type: disc; padding-left: 2em; margin: 1em 0;">')

  // Handle font sizes
  content = content.replace(/<span class="ql-size-small">/g, '<span style="font-size: 0.75em;">')
  content = content.replace(/<span class="ql-size-large">/g, '<span style="font-size: 1.5em;">')
  content = content.replace(/<span class="ql-size-huge">/g, '<span style="font-size: 2em;">')

  // Handle text colors and backgrounds (generic approach as Quill dynamically generates these classes)
  content = content.replace(/<span class="ql-color-([^"]+)">/g, (match, color) => {
    return `<span style="color: ${color};">`;
  });

  content = content.replace(/<span class="ql-bg-([^"]+)">/g, (match, color) => {
    return `<span style="background-color: ${color};">`;
  });

  return content
})

// Add Quill's stylesheet to ensure proper rendering of Quill-specific classes
onMounted(() => {
  // Check if the stylesheet is already added
  if (!document.getElementById('quill-styles')) {
    const link = document.createElement('link')
    link.id = 'quill-styles'
    link.rel = 'stylesheet'
    link.href = 'https://cdn.quilljs.com/1.3.6/quill.snow.css'
    document.head.appendChild(link)
  }
})
</script><template>
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

        <div class="text-gray-700 leading-relaxed scenario-content" v-html="formattedContent"></div>
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

/* Add styles for the rich text content */
.prose {
  max-width: none;
}

.prose ol {
  list-style-type: decimal;
  padding-left: 1.5em;
  margin: 1em 0;
}

.prose ul {
  list-style-type: disc;
  padding-left: 1.5em;
  margin: 1em 0;
}

.prose li {
  margin: 0.5em 0;
}

.prose h1, .prose h2, .prose h3 {
  margin-top: 1.5em;
  margin-bottom: 0.5em;
  font-weight: 600;
}

.prose p {
  margin: 1em 0;
}

.prose blockquote {
  border-left: 4px solid #e5e7eb;
  padding-left: 1em;
  margin: 1em 0;
  font-style: italic;
  color: #4b5563;
}

/* Styles for scenario content */
.scenario-content {
  line-height: 1.6;
}

.scenario-content ol {
  list-style-type: decimal;
  padding-left: 2em;
  margin: 1em 0;
}

.scenario-content ul {
  list-style-type: disc;
  padding-left: 2em;
  margin: 1em 0;
}

.scenario-content li {
  margin: 0.5em 0;
  display: list-item;
}

.scenario-content h1,
.scenario-content h2,
.scenario-content h3 {
  margin-top: 1.5em;
  margin-bottom: 0.5em;
  font-weight: 600;
}

.scenario-content p {
  margin: 1em 0;
}

.scenario-content blockquote {
  border-left: 4px solid #e5e7eb;
  padding-left: 1em;
  margin: 1em 0;
  font-style: italic;
  color: #4b5563;
}
</style>
