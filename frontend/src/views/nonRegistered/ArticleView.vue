<template>
  <div class="max-w-3xl mx-auto px-4 py-8">
    <router-link
      to="/news"
      class="inline-flex items-center text-blue-600 hover:text-blue-800 mb-6"
    >
      ← Tilbake til nyheter
    </router-link>

    <div v-if="isLoading" class="text-center py-12">
      <div class="inline-block animate-spin rounded-full h-8 w-8 border-t-2 border-b-2 border-blue-500 mb-4"></div>
      <p class="text-gray-600">Laster artikkel...</p>
    </div>

    <div v-else-if="error" class="text-center py-12">
      <h2 class="text-2xl font-semibold text-gray-700">Feil ved lasting av artikkel</h2>
      <p class="mt-2 text-gray-600">Beklager, men noe gikk galt. Vennligst prøv igjen senere.</p>
    </div>

    <div v-else-if="article?.data" class="space-y-6">
      <div class="text-sm text-gray-500">{{ formatDate(article.data.createdAt) }}</div>
      <h1 class="text-4xl font-bold text-gray-800">{{ article.data.title }}</h1>
      <div class="prose prose-lg max-w-none text-gray-600">
        <p>{{ article.data.text }}</p>
        <img v-if="article.data.imageUrl" :src="article.data.imageUrl" alt="" class="mt-4 rounded-lg max-w-full h-auto">
      </div>
    </div>

    <div v-else class="text-center py-12">
      <h2 class="text-2xl font-semibold text-gray-700">Artikkel ikke funnet</h2>
      <p class="mt-2 text-gray-600">Beklager, men artikkelen du leter etter eksisterer ikke.</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useGetArticleById } from '@/api/generated/krisefikserAPI'

const route = useRoute()
const articleId = computed(() => Number(route.params.id))

const {
  data: article,
  isLoading,
  error
} = useGetArticleById(articleId)

const formatDate = (dateString?: string) => {
  if (!dateString) return ''

  const date = new Date(dateString)
  return date.toLocaleDateString('no-NO', {
    day: 'numeric',
    month: 'long',
    year: 'numeric'
  })
}
</script>
