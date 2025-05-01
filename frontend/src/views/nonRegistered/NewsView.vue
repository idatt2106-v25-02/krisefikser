<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useGetAllArticles } from '@/api/generated/article/article'

const router = useRouter()

const { data: articles, isLoading, error } = useGetAllArticles()

const sortedArticles = computed(() => {
  if (!articles.value) return []

  // If articles.data is an array, use it directly
  const articleArray = Array.isArray(articles.value) ? articles.value : [articles.value]

  return [...articleArray].sort((a, b) => {
    const dateA = a.createdAt ? new Date(a.createdAt).getTime() : 0
    const dateB = b.createdAt ? new Date(b.createdAt).getTime() : 0
    return dateB - dateA
  })
})

const formatDate = (dateString?: string) => {
  if (!dateString) return ''

  const date = new Date(dateString)
  return date.toLocaleDateString('no-NO', {
    day: 'numeric',
    month: 'long',
    year: 'numeric',
  })
}

const getExcerpt = (text?: string) => {
  if (!text) return ''
  return text.length > 150 ? text.substring(0, 150) + '...' : text
}

const navigateToArticle = (id?: number) => {
  if (id !== undefined) {
    router.push({ name: 'article', params: { id } })
  }
}
</script>

<template>
  <div class="max-w-3xl mx-auto px-4 py-8">
    <h1 class="text-4xl font-bold mb-8 text-gray-800">Nyheter</h1>

    <div v-if="isLoading" class="text-center py-12">
      <div
        class="inline-block animate-spin rounded-full h-8 w-8 border-t-2 border-b-2 border-blue-500 mb-4"
      ></div>
      <p class="text-gray-600">Laster nyheter...</p>
    </div>

    <div v-else-if="error" class="text-center py-12">
      <h2 class="text-2xl font-semibold text-gray-700">Feil ved lasting av nyheter</h2>
      <p class="mt-2 text-gray-600">Beklager, men noe gikk galt. Vennligst prøv igjen senere.</p>
    </div>

    <div v-else-if="articles" class="space-y-8">
      <article
        v-for="article in sortedArticles"
        :key="article.id"
        class="border-b border-gray-200 pb-6 cursor-pointer hover:bg-gray-50 transition-colors p-4 rounded"
        @click="navigateToArticle(article.id)"
      >
        <div class="text-sm text-gray-500 mb-2">{{ formatDate(article.createdAt) }}</div>
        <h2 class="text-2xl font-semibold mb-3 text-gray-700">{{ article.title }}</h2>
        <p class="text-gray-600 mb-3">
          {{ getExcerpt(article.text) }}
        </p>
        <router-link
          :to="{ name: 'article', params: { id: article.id } }"
          class="text-blue-600 hover:text-blue-800"
        >
          Les mer
        </router-link>
      </article>
    </div>

    <div v-else class="text-center py-12">
      <h2 class="text-2xl font-semibold text-gray-700">Ingen nyheter funnet</h2>
      <p class="mt-2 text-gray-600">Det er ingen nyheter tilgjengelig for øyeblikket.</p>
    </div>
  </div>
</template>
