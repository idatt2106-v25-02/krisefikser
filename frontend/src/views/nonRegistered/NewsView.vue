<template>
  <div class="max-w-3xl mx-auto px-4 py-8">
    <h1 class="text-4xl font-bold mb-8 text-gray-800">Nyheter</h1>

    <div class="space-y-8">
      <article
        v-for="article in sortedArticles"
        :key="article.id"
        class="border-b border-gray-200 pb-6 cursor-pointer hover:bg-gray-50 transition-colors p-4 rounded"
        @click="navigateToArticle(article.id)"
      >
        <div class="text-sm text-gray-500 mb-2">{{ formatDate(article.date) }}</div>
        <h2 class="text-2xl font-semibold mb-3 text-gray-700">{{ article.title }}</h2>
        <p class="text-gray-600 mb-3">
          {{ article.excerpt }}
        </p>
        <router-link
          :to="{ name: 'article', params: { id: article.id }}"
          class="text-blue-600 hover:text-blue-800"
        >
          Les mer
        </router-link>
      </article>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

interface NewsArticle {
  id: string;
  date: string;
  title: string;
  excerpt: string;
}

const articles: NewsArticle[] = [
  {
    id: '1',
    date: '2025-01-15',
    title: 'Lansering av KriseFikser studentprosjekt',
    excerpt: 'Vi er glade for å kunne presentere KriseFikser, et innovativt studentprosjekt ved NTNU. Plattformen er designet for å hjelpe norske husholdninger med beredskapsplanlegging og krisehåndtering.'
  },
  {
    id: '2',
    date: '2025-01-10',
    title: 'Samarbeid med beredskapsetaten',
    excerpt: 'Som del av vårt studentprosjekt har vi etablert dialog med beredskapsetaten for å sikre at vår løsning følger gjeldende retningslinjer og beste praksis for beredskapsplanlegging.'
  },
  {
    id: '3',
    date: '2025-01-05',
    title: 'Teknologivalg og utvikling',
    excerpt: 'Vi har valgt moderne webutviklingsteknologier for å bygge KriseFikser. Vue.js og Tailwind CSS gir oss muligheten til å skape en brukervennlig og responsiv løsning.'
  },
  {
    id: '4',
    date: '2025-01-01',
    title: 'Prosjektstart og målsetting',
    excerpt: 'KriseFikser starter som et ambisiøst studentprosjekt med mål om å gjøre beredskap mer tilgjengelig for alle. Vi ser frem til å dele vår reise med dere.'
  }
]

const sortedArticles = computed(() => {
  return [...articles].sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime())
})

const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('no-NO', {
    day: 'numeric',
    month: 'long',
    year: 'numeric'
  })
}

const navigateToArticle = (id: string) => {
  router.push({ name: 'article', params: { id } })
}
</script>

<style scoped>
.news-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 1rem;
}

.news-list {
  margin-top: 2rem;
}

.news-item {
  margin-bottom: 2rem;
  padding-bottom: 1.5rem;
  border-bottom: 1px solid #eaeaea;
}

.news-date {
  color: #666;
  font-size: 0.9rem;
  margin-bottom: 0.5rem;
}

h2 {
  margin: 0.5rem 0;
  font-size: 1.5rem;
}

.read-more {
  display: inline-block;
  margin-top: 0.5rem;
  color: #0066cc;
  text-decoration: none;
}

.read-more:hover {
  text-decoration: underline;
}
</style>
