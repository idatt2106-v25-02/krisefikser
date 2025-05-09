<!-- CrisisScenariosSection.vue -->
<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useGetAllScenarios } from '@/api/generated/scenario/scenario'
import { useRouter } from 'vue-router'

const { data: scenarios, isLoading, error } = useGetAllScenarios()
const router = useRouter()

const latestScenarios = computed(() => {
  if (!scenarios?.value) return [];
  return [...scenarios.value]
    .sort((a, b) => {
      const aCreatedAt = (a as { createdAt?: string }).createdAt;
      const bCreatedAt = (b as { createdAt?: string }).createdAt;
      if (aCreatedAt && bCreatedAt) {
        return new Date(bCreatedAt).getTime() - new Date(aCreatedAt).getTime();
      }
      return a.title.localeCompare(b.title);
    })
    .slice(0, 3);
});

// Process preview content to preserve formatting
const processPreviewContent = (content: string, maxLength: number = 120) => {
  if (!content) return ''

  // Create a temporary div to parse HTML content
  const tempDiv = document.createElement('div')
  tempDiv.innerHTML = content

  // Get text content for length calculation
  const textContent = tempDiv.textContent || ''

  // If text is shorter than maxLength, just return the original HTML
  if (textContent.length <= maxLength) {
    return content
  }

  // Find a good cutoff point (at a space after maxLength)
  let cutoffIndex = maxLength
  while (cutoffIndex < textContent.length && textContent[cutoffIndex] !== ' ') {
    cutoffIndex++
  }

  // Extract a portion of the HTML that corresponds roughly to the text length
  // This is an approximation - the ratio of HTML length to text length
  const ratio = content.length / textContent.length
  const approximateHtmlCutoff = Math.min(
    Math.round(cutoffIndex * ratio),
    content.length
  )

  // Get truncated HTML and make sure we don't cut in the middle of a tag
  let truncatedHtml = content.substring(0, approximateHtmlCutoff)

  // Add ellipsis
  truncatedHtml += '...'

  // Make sure we close any open tags
  tempDiv.innerHTML = truncatedHtml
  return tempDiv.innerHTML
}

const goToAllScenarios = () => {
  router.push('/scenarioer')
}

const goToScenario = (id: string) => {
  router.push(`/scenario/${id}`)
}

// Add Quill's stylesheet to ensure proper rendering of Quill-specific classes
onMounted(() => {
  // Check if the stylesheet is already added
  if (!document.getElementById('quill-styles-home')) {
    const link = document.createElement('link')
    link.id = 'quill-styles-home'
    link.rel = 'stylesheet'
    link.href = 'https://cdn.quilljs.com/1.3.6/quill.snow.css'
    document.head.appendChild(link)

    // Add additional styles for preview
    const style = document.createElement('style')
    style.id = 'quill-preview-styles'
    style.textContent = `
      /* Ensure text colors and formatting show in previews */
      .scenario-preview .ql-size-small { font-size: 0.75em !important; }
      .scenario-preview .ql-size-large { font-size: 1.5em !important; }
      .scenario-preview .ql-size-huge { font-size: 2em !important; }
      .scenario-preview .ql-color-red { color: red !important; }
      .scenario-preview .ql-color-blue { color: blue !important; }
      .scenario-preview .ql-color-green { color: green !important; }
      .scenario-preview .ql-color-orange { color: orange !important; }
      .scenario-preview .ql-color-purple { color: purple !important; }
      .scenario-preview .ql-background-yellow { background-color: yellow !important; }
      .scenario-preview .ql-align-center { text-align: center !important; }
      .scenario-preview .ql-align-right { text-align: right !important; }
      .scenario-preview strong { font-weight: bold !important; }
      .scenario-preview em { font-style: italic !important; }
      .scenario-preview u { text-decoration: underline !important; }
    `
    document.head.appendChild(style)
  }
})
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
      <p class="text-gray-600 max-w-3xl mx-auto text-center mb-4 md:mb-0">
        Lær om ulike krisesituasjoner og hvordan du best kan forberede deg.
      </p>

      <!-- Responsive button -->
      <div class="flex justify-center md:absolute md:right-0 md:top-0">
        <a @click="goToAllScenarios"
           class="text-blue-600 font-medium hover:underline inline-flex items-center cursor-pointer whitespace-nowrap">
          Se alle scenarioer
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 ml-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
          </svg>
        </a>
      </div>
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
        class="bg-white p-6 rounded-lg shadow-md transition-shadow hover:shadow-lg cursor-pointer"
        @click="goToScenario(scenario.id)"
      >
        <h3 class="text-xl font-semibold text-gray-800 mb-2">{{ scenario.title }}</h3>
        <div
          class="text-gray-600 mb-4 line-clamp-4 scenario-preview [&_h1]:font-bold [&_h2]:font-bold [&_h3]:font-bold [&_h1]:my-2 [&_h2]:my-2 [&_h3]:my-2 [&_p]:my-2 [&_strong]:font-bold [&_em]:italic [&_u]:underline [&_.ql-color-red]:!text-red-500 [&_.ql-color-blue]:!text-blue-500 [&_.ql-color-green]:!text-green-500 [&_.ql-color-orange]:!text-orange-500 [&_.ql-color-purple]:!text-purple-500 [&_.ql-background-yellow]:!bg-yellow-200 [&_.ql-align-center]:!text-center [&_.ql-align-right]:!text-right overflow-hidden"
          v-html="processPreviewContent(scenario.content, 120)"
        ></div>
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
