<!-- NewsSection.vue -->
<script lang="ts">
export default {
  name: 'NewsSection',
}
</script>

<script setup lang="ts">
import { computed } from 'vue'
import { useGetAllArticles } from '@/api/generated/article/article'
import { useRouter } from 'vue-router'

const { data: articles, isLoading, error } = useGetAllArticles()
const router = useRouter()

console.log(articles.value)

const parseIsoString = (isoString?: string): Date | null => {
  if (!isoString) return null
  try {
    return new Date(isoString)
  } catch (e) {
    console.error('Error parsing date string:', isoString, e)
    return null
  }
}

const latestArticles = computed(() => {
  if (!articles?.value) return []

  // Sort by date and get the 3 most recent articles
  return [...articles.value]
    .sort((a, b) => {
      const dateA = parseIsoString(a.createdAt)?.getTime() || 0
      const dateB = parseIsoString(b.createdAt)?.getTime() || 0
      return dateB - dateA
    })
    .slice(0, 3)
})

const formatDate = (isoString?: string) => {
  if (!isoString) return ''

  const date = parseIsoString(isoString)
  if (!date || isNaN(date.getTime())) return ''

  try {
    return date.toLocaleDateString('nb-NO', {
      day: 'numeric',
      month: 'long',
      year: 'numeric',
    })
  } catch (e) {
    // Fallback to English if Norwegian locale is not supported
    return date.toLocaleDateString('en', {
      day: 'numeric',
      month: 'long',
      year: 'numeric',
    })
  }
}

const getExcerpt = (text?: string) => {
  if (!text) return ''
  return text.length > 100 ? text.substring(0, 100) + '...' : text
}

const goToAllNews = () => {
  router.push('/nyheter')
}
</script>

<template>
  <section class="mb-16">
    <!-- Title centered -->
    <div class="text-center mb-4">
      <h2 class="text-3xl font-bold text-gray-800">Aktuelle nyheter</h2>
    </div>

    <!-- Description centered and "Se alle" button on same line -->
    <div class="relative mb-8">
      <!-- Centered paragraph -->
      <p class="text-gray-600 max-w-3xl mx-auto text-center mb-4 md:mb-0">
        Hold deg oppdatert på nyheter og hendelser relatert til beredskap.
      </p>

      <!-- Responsive button -->
      <div class="flex justify-center md:absolute md:right-0 md:top-0">
        <a
          @click="goToAllNews"
          class="text-blue-600 font-medium hover:underline inline-flex items-center cursor-pointer whitespace-nowrap"
        >
          Se alle nyheter
          <svg
            xmlns="http://www.w3.org/2000/svg"
            class="h-5 w-5 ml-1"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M9 5l7 7-7 7"
            />
          </svg>
        </a>
      </div>
    </div>

    <div v-if="isLoading" class="text-center py-8">
      <div
        class="inline-block animate-spin rounded-full h-8 w-8 border-t-2 border-b-2 border-blue-500 mb-4"
      ></div>
      <p class="text-gray-600">Laster nyheter...</p>
    </div>

    <div v-else-if="error" class="text-center py-8">
      <p class="text-gray-600">Kunne ikke laste nyheter. Prøv igjen senere.</p>
    </div>

    <div v-else-if="latestArticles.length" class="grid md:grid-cols-3 gap-8">
      <div
        v-for="article in latestArticles"
        :key="article.id"
        class="bg-white rounded-lg shadow overflow-hidden"
      >
        <div
          v-if="article.imageUrl"
          class="h-48 bg-cover bg-center"
          :style="`background-image: url('${article.imageUrl}')`"
        ></div>
        <div v-else class="h-48 bg-gray-200"></div>
        <div class="p-6">
          <p class="text-sm text-gray-500 mb-2">{{ formatDate(article.createdAt) }}</p>
          <h3 class="text-xl font-semibold text-gray-800 mb-2">{{ article.title }}</h3>
          <p class="text-gray-600 mb-4">{{ getExcerpt(article.text) }}</p>
          <!-- Added the arrow icon to the Les mer link -->
          <router-link
            :to="`/artikkel/${article.id}`"
            class="text-blue-600 font-medium hover:underline inline-flex items-center"
          >
            Les mer
            <svg
              xmlns="http://www.w3.org/2000/svg"
              class="h-5 w-5 ml-1"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M9 5l7 7-7 7"
              />
            </svg>
          </router-link>
        </div>
      </div>
    </div>

    <div v-else class="text-center py-8">
      <p class="text-gray-600">Ingen nyheter tilgjengelig for øyeblikket.</p>
    </div>
  </section>
</template>
