<template>
  <div class="max-w-3xl mx-auto px-4 py-8">
    <router-link
      to="/news"
      class="inline-flex items-center text-blue-600 hover:text-blue-800 mb-6"
    >
      ← Tilbake til nyheter
    </router-link>

    <div v-if="article" class="space-y-6">
      <div class="text-sm text-gray-500">{{ formatDate(article.date) }}</div>
      <h1 class="text-4xl font-bold text-gray-800">{{ article.title }}</h1>
      <div class="prose prose-lg max-w-none text-gray-600">
        <p>{{ article.excerpt }}</p>
        <!-- Her kan vi legge til mer innhold for artikkelen -->
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

const route = useRoute()

interface NewsArticle {
  id: string;
  date: string;
  title: string;
  excerpt: string;
}

// This should ideally come from a store or API
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

const article = computed(() => {
  return articles.find(a => a.id === route.params.id)
})

const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('no-NO', {
    day: 'numeric',
    month: 'long',
    year: 'numeric'
  })
}
</script>
