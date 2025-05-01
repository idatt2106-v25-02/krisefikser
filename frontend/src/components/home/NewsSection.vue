<script lang="ts">
export default {
  name: 'NewsSection'
}
</script>
<template>
  <section>
    <div class="text-center mb-12">
      <h2 class="text-3xl font-bold text-gray-800 mb-4">Aktuelle nyheter</h2>
      <p class="text-xl text-gray-600 max-w-3xl mx-auto">Hold deg oppdatert på nyheter og hendelser relatert til beredskap.</p>
    </div>

    <div v-if="isLoading" class="text-center py-8">
      <div class="inline-block animate-spin rounded-full h-8 w-8 border-t-2 border-b-2 border-blue-500 mb-4"></div>
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
        <div v-if="article.imageUrl" class="h-48 bg-cover bg-center" :style="`background-image: url('${article.imageUrl}')`"></div>
        <div v-else class="h-48 bg-gray-200"></div>
        <div class="p-6">
          <p class="text-sm text-gray-500 mb-2">{{ formatDate(article.createdAt) }}</p>
          <h3 class="text-xl font-semibold text-gray-800 mb-2">{{ article.title }}</h3>
          <p class="text-gray-600 mb-4">{{ getExcerpt(article.text) }}</p>
          <router-link :to="`/nyheter/${article.id}`" class="text-blue-600 font-medium hover:underline">Les mer</router-link>
        </div>
      </div>
    </div>

    <div v-else class="text-center py-8">
      <p class="text-gray-600">Ingen nyheter tilgjengelig for øyeblikket.</p>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useGetAllArticles } from '@/api/generated/article/article';

const {
  data: articles,
  isLoading,
  error
} = useGetAllArticles()

const latestArticles = computed(() => {
  if (!articles?.value) return []

  // If articles.value.data is an array, use it directly
  const articleData = articles.value
  const articleArray = Array.isArray(articleData) ? articleData : [articleData]

  // Sort by date and get the 3 most recent articles
  return [...articleArray]
    .sort((a, b) => {
      const dateA = a.createdAt ? new Date(a.createdAt).getTime() : 0
      const dateB = b.createdAt ? new Date(b.createdAt).getTime() : 0
      return dateB - dateA
    })
    .slice(0, 3)
})

const formatDate = (dateString?: string) => {
  if (!dateString) return ''

  const date = new Date(dateString)
  return date.toLocaleDateString('no-NO', {
    day: 'numeric',
    month: 'long',
    year: 'numeric'
  })
}

const getExcerpt = (text?: string) => {
  if (!text) return ''
  return text.length > 100 ? text.substring(0, 100) + '...' : text
}
</script>
