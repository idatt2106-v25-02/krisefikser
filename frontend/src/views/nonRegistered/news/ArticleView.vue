<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useGetArticleById } from '@/api/generated/article/article.ts'

const route = useRoute()
const articleId = computed(() => Number(route.params.id))

const { data: article, isLoading, error } = useGetArticleById(articleId)

const arrayToDate = (dateArray?: number[]) => {
  if (!dateArray || !Array.isArray(dateArray) || dateArray.length < 3) return null
  // Note: Month in JavaScript is 0-based, so we subtract 1 from the month
  return new Date(dateArray[0], dateArray[1] - 1, dateArray[2],
    dateArray[3] || 0, dateArray[4] || 0, dateArray[5] || 0)
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
    // Fallback to English if Norwegian locale is not supported
    return date.toLocaleDateString('en', {
      day: 'numeric',
      month: 'long',
      year: 'numeric',
    })
  }
}
</script>

<template>
  <div class="max-w-3xl mx-auto px-4 py-8">
    <router-link
      to="/nyheter"
      class="inline-flex items-center text-blue-600 hover:text-blue-800 mb-6"
    >
      ← Tilbake til nyheter
    </router-link>

    <div v-if="isLoading" class="text-center py-12">
      <div
        class="inline-block animate-spin rounded-full h-8 w-8 border-t-2 border-b-2 border-blue-500 mb-4"
      ></div>
      <p class="text-gray-600">Laster artikkel...</p>
    </div>

    <div v-else-if="error" class="text-center py-12">
      <h2 class="text-2xl font-semibold text-gray-700">Feil ved lasting av artikkel</h2>
      <p class="mt-2 text-gray-600">Beklager, men noe gikk galt. Vennligst prøv igjen senere.</p>
    </div>

    <div v-else-if="article" class="space-y-6">
      <img
        v-if="article.imageUrl"
        :src="article.imageUrl"
        alt=""
        class="w-full h-[400px] object-cover rounded-lg"
      />
      <div class="text-sm text-gray-500">{{ formatDate(article.createdAt) }}</div>
      <h1 class="text-4xl font-bold text-gray-800">{{ article.title }}</h1>
      <div class="prose prose-lg max-w-none text-gray-600">
        <p>{{ article.text }}</p>
      </div>
    </div>

    <div v-else class="text-center py-12">
      <h2 class="text-2xl font-semibold text-gray-700">Artikkel ikke funnet</h2>
      <p class="mt-2 text-gray-600">Beklager, men artikkelen du leter etter eksisterer ikke.</p>
    </div>
  </div>
</template>
