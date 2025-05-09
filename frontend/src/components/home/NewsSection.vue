<!-- NewsSection.vue -->
<script lang="ts">
export default {
  name: 'NewsSection',
}
</script>

<script lang="ts" setup>
import { computed } from 'vue'
import { useGetAllArticles } from '@/api/generated/article/article'
import { useRouter } from 'vue-router'
import { arrayToDate } from '@/api/Utils'

const { data: articles, isLoading, error } = useGetAllArticles()
const router = useRouter()

const latestArticles = computed(() => {
  if (!articles?.value) return []

  // Sort by date and get the 3 most recent articles
  return [...articles.value]
    .sort((a, b) => {
      const dateA = arrayToDate(a.createdAt)?.getTime() || 0
      const dateB = arrayToDate(b.createdAt)?.getTime() || 0
      return dateB - dateA
    })
    .slice(0, 3)
})

const formatDate = (isoString?: string) => {
  if (!isoString) return ''

  const date = arrayToDate(isoString)
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
          class="text-blue-600 font-medium hover:underline inline-flex items-center cursor-pointer whitespace-nowrap"
          @click="goToAllNews"
        >
          Se alle nyheter
          <svg
            class="h-5 w-5 ml-1"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
            xmlns="http://www.w3.org/2000/svg"
          >
            <path
              d="M9 5l7 7-7 7"
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
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
          :style="`background-image: url('${article.imageUrl}')`"
          class="h-48 bg-cover bg-center"
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
              class="h-5 w-5 ml-1"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
              xmlns="http://www.w3.org/2000/svg"
            >
              <path
                d="M9 5l7 7-7 7"
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
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
