<script lang="ts" setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useGetArticleById } from '@/api/generated/article/article.ts'
import { ArrowLeft } from 'lucide-vue-next'
import { Button } from '@/components/ui/button'

const route = useRoute()
const router = useRouter()
const articleId = computed(() => Number(route.params.id))

const { data: article, isLoading, error } = useGetArticleById(articleId)

const arrayToDate = (dateArray?: number[]) => {
  if (!dateArray || !Array.isArray(dateArray) || dateArray.length < 3) return null
  // Note: Month in JavaScript is 0-based, so we subtract 1 from the month
  return new Date(
    dateArray[0],
    dateArray[1] - 1,
    dateArray[2],
    dateArray[3] || 0,
    dateArray[4] || 0,
    dateArray[5] || 0,
  )
}

const formatDate = (dateArray?: number[]) => {
  if (!dateArray || !Array.isArray(dateArray)) return ''

  const date = arrayToDate(dateArray)
  if (!date || isNaN(date.getTime())) return ''

  try {
    return date.toLocaleDateString('nb-NO', {
      day: 'numeric',
      month: 'long',
      year: 'numeric',
    })
  } catch (e) {
    console.error('Error formatting date:', e)
    // Fallback to English if Norwegian locale is not supported
    return date.toLocaleDateString('en', {
      day: 'numeric',
      month: 'long',
      year: 'numeric',
    })
  }
}

const goBackToNews = () => {
  router.push('/nyheter')
}
</script>

<template>
  <div class="bg-gray-50 min-h-screen">
    <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
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

      <div class="flex items-center mb-6">
        <button
          class="text-blue-600 hover:text-blue-800 transition-colors font-medium flex items-center group"
          @click="goBackToNews"
        >
          <ArrowLeft class="h-5 w-5 mr-2 group-hover:translate-x-[-3px] transition-transform" />
          <span>Tilbake til nyheter</span>
        </button>
      </div>

      <!-- Loading state -->
      <div
        v-if="isLoading"
        class="bg-white rounded-lg shadow-lg p-8 flex items-center justify-center"
      >
        <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500"></div>
      </div>

      <!-- Error state -->
      <div v-else-if="error" class="bg-white rounded-lg shadow-lg p-8 border-l-4 border-red-500">
        <div class="flex items-start">
          <div class="flex-shrink-0 mt-0.5">
            <svg
              class="h-6 w-6 text-red-500"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
              xmlns="http://www.w3.org/2000/svg"
            >
              <path
                d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
              />
            </svg>
          </div>
          <div class="ml-3">
            <h3 class="text-lg font-medium text-red-800">Feil ved lasting av artikkel</h3>
            <p class="mt-2 text-red-700">
              Beklager, men noe gikk galt. Vennligst prøv igjen senere.
            </p>
          </div>
        </div>
      </div>

      <!-- Article Content -->
      <div v-else-if="article" class="bg-white rounded-lg shadow-lg p-8 relative">
        <!-- Decorative element in top corner -->
        <div
          class="absolute top-0 right-0 w-32 h-32 bg-blue-50/70 rounded-bl-full -mt-2 -mr-2 overflow-hidden z-0"
        >
          <div class="absolute top-5 right-5">
            <svg
              class="h-10 w-10 text-blue-500"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
              xmlns="http://www.w3.org/2000/svg"
            >
              <path
                d="M19 20H5a2 2 0 01-2-2V6a2 2 0 012-2h10a2 2 0 012 2v1m2 13a2 2 0 01-2-2V7m2 13a2 2 0 002-2V9a2 2 0 00-2-2h-2m-4-3H9M7 16h6M7 8h6v4H7V8z"
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
              />
            </svg>
          </div>
        </div>

        <div class="relative z-10">
          <div
            class="inline-flex items-center px-2.5 py-0.5 rounded-full bg-blue-100 text-blue-800 text-sm mb-4"
          >
            {{ formatDate(article.createdAt) }}
          </div>

          <h1 class="text-3xl sm:text-4xl font-bold text-gray-800 mb-6">{{ article.title }}</h1>

          <div v-if="article.imageUrl" class="mb-8">
            <img
              :src="article.imageUrl"
              alt="Artikkelillustrasjon"
              class="rounded-lg w-full h-[400px] object-cover shadow-md"
            />
          </div>

          <div class="prose prose-lg max-w-none text-gray-700 leading-relaxed">
            <p>{{ article.text }}</p>
          </div>
        </div>
      </div>

      <!-- Empty state -->
      <div
        v-else
        class="bg-white rounded-lg shadow-lg p-8 flex flex-col items-center justify-center py-16"
      >
        <svg
          class="h-16 w-16 text-gray-400 mb-4"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
          xmlns="http://www.w3.org/2000/svg"
        >
          <path
            d="M9.172 16.172a4 4 0 015.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
          />
        </svg>
        <h2 class="text-xl font-medium text-gray-800 mb-2">Artikkel ikke funnet</h2>
        <p class="text-gray-600 text-center mb-6">
          Beklager, men artikkelen du leter etter eksisterer ikke.
        </p>
        <Button @click="goBackToNews">Tilbake til oversikten</Button>
      </div>
    </div>
  </div>
</template>
