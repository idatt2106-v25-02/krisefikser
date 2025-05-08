<!-- NewsSection.vue -->
<script lang="ts">
export default {
  name: 'NewsSection',
}
</script>

<script lang="ts" setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useGetAllArticles } from '@/api/generated/article/article'
import { Newspaper, AlertTriangle, Calendar, ArrowRight } from 'lucide-vue-next'

const router = useRouter()
const { data: articles, isLoading, error } = useGetAllArticles()

const ARTICLES_PER_PAGE = 6
const currentPage = ref(1)

const sortedArticles = computed(() => {
  if (!articles?.value) return []
  const articleArray = Array.isArray(articles.value) ? articles.value : [articles.value]
  return [...articleArray].sort((a, b) => {
    const dateA = new Date(a.createdAt).getTime()
    const dateB = new Date(b.createdAt).getTime()
    return dateB - dateA
  })
})

const totalPages = computed(() => {
  return Math.ceil(sortedArticles.value.length / ARTICLES_PER_PAGE)
})

const paginatedArticles = computed(() => {
  const start = (currentPage.value - 1) * ARTICLES_PER_PAGE
  return sortedArticles.value.slice(start, start + ARTICLES_PER_PAGE)
})

const formatDate = (dateArray?: string) => {
  if (!dateArray || !Array.isArray(dateArray)) return ''

  const date = new Date(dateArray)
  if (!date || isNaN(date.getTime())) return ''

  try {
    return date.toLocaleDateString('nb-NO', {
      day: 'numeric',
      month: 'long',
      year: 'numeric',
    })
  } catch {
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
  const cleanText = text.replace(/### |##|#|\*\*/g, '')

  return cleanText.length > 200 ? cleanText.substring(0, 200) + '...' : cleanText
}

const navigateToArticle = (id?: number) => {
  if (id !== undefined) {
    router.push({ name: 'article', params: { id } })
  }
}

// Choose icon based on article title/content
const getArticleIcon = (title: string = '') => {
  const titleLower = title.toLowerCase()

  if (titleLower.includes('varsling') || titleLower.includes('advarsel')) return AlertTriangle
  if (titleLower.includes('dato') || titleLower.includes('hendelse')) return Calendar

  return Newspaper // Default icon
}

// Reference to the articles container for scrolling
const articlesContainer = ref(null)
const showScrollIndicator = ref(false)

// Check if scrolling is needed
onMounted(() => {
  if (articlesContainer.value) {
    const checkScroll = () => {
      if (articlesContainer.value) {
        const { scrollHeight, clientHeight } = articlesContainer.value
        showScrollIndicator.value = scrollHeight > clientHeight
      }
    }

    // Check initial state
    setTimeout(checkScroll, 500)

    // Add resize listener
    window.addEventListener('resize', checkScroll)
  }
})
</script>

<template>
  <div class="relative">
    <!-- Background decoration - wave pattern -->
    <div class="absolute top-0 left-0 w-full h-64 bg-blue-50 -z-10 overflow-hidden">
      <div class="absolute bottom-0 left-0 right-0">
        <svg class="w-full h-auto" viewBox="0 0 1440 320" xmlns="http://www.w3.org/2000/svg">
          <path
            d="M0,128L48,144C96,160,192,192,288,186.7C384,181,480,139,576,138.7C672,139,768,181,864,170.7C960,160,1056,96,1152,85.3C1248,75,1344,117,1392,138.7L1440,160L1440,320L1392,320C1344,320,1248,320,1152,320C1056,320,960,320,864,320C768,320,672,320,576,320C480,320,384,320,288,320C192,320,96,320,48,320L0,320Z"
            fill="#ffffff"
            fill-opacity="1"
          ></path>
        </svg>
      </div>
    </div>

    <div class="container mx-auto px-4 py-12">
      <div class="text-center mb-12">
        <h1 class="text-3xl font-bold text-gray-800 mb-4">Nyheter</h1>
        <p class="text-gray-600 max-w-3xl mx-auto">
          Hold deg oppdatert med de siste nyhetene og viktige hendelser.
        </p>
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

      <!-- Articles list with max height and scrolling on desktop -->
      <div
        v-else-if="sortedArticles.length"
        ref="articlesContainer"
        class="grid md:grid-cols-2 lg:grid-cols-3 gap-6 md:gap-8 md:max-h-[800px] md:overflow-y-auto md:pr-2 md:pb-4 scrollbar-thin scrollbar-thumb-blue-500/50 scrollbar-track-blue-100/70"
      >
        <div
          v-for="article in paginatedArticles"
          :key="article.id"
          class="bg-white rounded-lg shadow-md overflow-hidden cursor-pointer hover:shadow-lg transition relative min-h-[180px] flex flex-col"
          @click="navigateToArticle(article.id)"
        >
          <!-- Blue corner decoration - same for all cards -->
          <div
            class="absolute top-0 right-0 w-16 h-16 rounded-bl-full -mt-1 -mr-1 overflow-hidden z-0 bg-blue-50/70"
          >
            <div class="absolute top-2.5 right-2.5">
              <component :is="getArticleIcon(article.title)" class="h-6 w-6 text-blue-500" />
            </div>
          </div>

          <div class="p-6 relative z-10 flex-grow flex flex-col">
            <div class="text-sm text-gray-500 mb-2">{{ formatDate(article.createdAt) }}</div>
            <h2 class="font-semibold text-gray-800 text-lg mb-2">{{ article.title }}</h2>
            <p class="text-gray-500 text-s mb-4 flex-grow overflow-hidden">
              <span class="line-clamp-4">{{ getExcerpt(article.text) }}</span>
            </p>
            <div class="flex items-center text-blue-600 font-medium hover:underline group mt-auto">
              Les mer
              <ArrowRight class="h-5 w-5 ml-1 transform transition group-hover:translate-x-1" />
            </div>
          </div>
        </div>
      </div>

      <div v-else class="text-center py-8">
        <p class="text-gray-600">Ingen nyheter tilgjengelig for øyeblikket.</p>
      </div>

      <!-- Pagination -->
      <div
        v-if="sortedArticles.length > ARTICLES_PER_PAGE"
        class="flex justify-center items-center space-x-4 mt-8"
      >
        <button
          :class="[
            currentPage === 1
              ? 'bg-gray-100 text-gray-400'
              : 'bg-white text-gray-700 hover:bg-gray-50 hover:text-blue-600 border border-gray-200',
          ]"
          :disabled="currentPage === 1"
          class="inline-flex items-center px-4 py-2 text-sm font-medium transition-colors rounded-md disabled:opacity-50 disabled:cursor-not-allowed"
          @click="currentPage--"
        >
          <svg
            class="h-4 w-4 mr-1"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
            xmlns="http://www.w3.org/2000/svg"
          >
            <path
              d="M15 19l-7-7 7-7"
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
            />
          </svg>
          Forrige
        </button>
        <div class="flex items-center gap-1 text-sm">
          <span class="font-medium text-gray-700">Side</span>
          <span class="px-3 py-1 rounded-md bg-blue-50 text-blue-700 font-medium">
            {{ currentPage }}
          </span>
          <span class="font-medium text-gray-700">av {{ totalPages }}</span>
        </div>
        <button
          :class="[
            currentPage === totalPages
              ? 'bg-gray-100 text-gray-400'
              : 'bg-white text-gray-700 hover:bg-gray-50 hover:text-blue-600 border border-gray-200',
          ]"
          :disabled="currentPage === totalPages"
          class="inline-flex items-center px-4 py-2 text-sm font-medium transition-colors rounded-md disabled:opacity-50 disabled:cursor-not-allowed"
          @click="currentPage++"
        >
          Neste
          <svg
            class="h-4 w-4 ml-1"
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
        </button>
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
    overflow: hidden;
  }

  .line-clamp-1 {
    display: -webkit-box;
    -webkit-line-clamp: 1;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }
}
</style>
